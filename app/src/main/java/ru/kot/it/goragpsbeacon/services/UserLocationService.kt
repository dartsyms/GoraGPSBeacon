package ru.kot.it.goragpsbeacon.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.kot.it.goragpsbeacon.R
import ru.kot.it.goragpsbeacon.activities.MainActivity
import ru.kot.it.goragpsbeacon.apis.WebAPI
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.factories.WebServiceGenerator
import ru.kot.it.goragpsbeacon.infrastructure.GoraGPSBeaconApp
import ru.kot.it.goragpsbeacon.infrastructure.RxBus
import ru.kot.it.goragpsbeacon.models.MessageEvent
import ru.kot.it.goragpsbeacon.models.UserLocation
import ru.kot.it.goragpsbeacon.utils.GPSHelper
import ru.kot.it.goragpsbeacon.utils.NetworkHelper
import ru.kot.it.goragpsbeacon.utils.PrefUtils
import java.util.*
import kotlin.collections.ArrayList


class UserLocationService: Service() {

    private var locationManager: LocationManager? = null
    private val ping = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "ping" -> {
                    LocalBroadcastManager
                            .getInstance(this@UserLocationService)
                            .sendBroadcastSync(Intent("pong"))
                }
            }
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("UserLocationService", "onStartCommand() executed")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notification: Notification = NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Location Service for GoraOnline")
                .setContentTitle("ULTService")
                .build()

        locationManagerSetup()
        startForeground(911, notification)

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(ping, IntentFilter("ping"))
        RxBus.publish(MessageEvent(Constants.BUS_SERVICE_STARTED_EVENT, "Service started via RxBus"))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0 until locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(ping)
    }

    private fun locationManagerSetup() {
        if (locationManager == null)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        when (GPSHelper.checkLocationPermission(this)) {

            GPSHelper.hasGPSProviderEnabled(this) -> {
                try {
                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
                } catch(e: SecurityException) {
                    Log.e(TAG, "Fail to request location update", e)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "GPS provider does not exist", e)
                }
            }

            GPSHelper.hasNetworkProviderEnabled(this) -> {
                try {
                    locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
                } catch (e: SecurityException) {
                    Log.e(TAG, "Fail to request location update", e)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Network provider does not exist", e)
                }
            }
        }
    }

    companion object {
        const val TAG = Constants.USER_LOCATION_SERVICE
        const val INTERVAL = Constants.LOCATION_INTERVAL
        const val DISTANCE = Constants.GPS_ACCURACY_LEVEL

        var IS_SERVICE_RUNNING = false

        val locStore: MutableList<UserLocation> = ArrayList()

        val locationListeners = arrayOf(
                ULTListener(LocationManager.GPS_PROVIDER),
                ULTListener(LocationManager.NETWORK_PROVIDER)
        )

        val webService by lazy {
            WebServiceGenerator.createService(WebAPI::class.java, GoraGPSBeaconApp.instance!!.getContext())
        }

        class ULTListener(provider: String): LocationListener {

            private val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                // Send current location to the server after network availability check (or store them in array)
                when (NetworkHelper.hasNetworkAccess(GoraGPSBeaconApp.instance!!.getContext())) {
                    true -> location?.let {
                        sendDataImmediate(location.latitude.toString(), location.longitude.toString(), Calendar.getInstance().timeInMillis)
                        sendDataDeferred(locStore)
                        RxBus.publish(MessageEvent(Constants.BUS_LOCATION_SENT_EVENT, "Send location data: lat=${location.latitude}, lon=${location.longitude}"))
                        sendServiceMessage("Send location data: lat=${location.latitude}, lon=${location.longitude}")
                    }
                    false -> location?.let {
                        val userLocation = UserLocation(location.latitude.toString(),
                                                        location.longitude.toString(),
                                                        Calendar.getInstance().timeInMillis)
                        locStore.add(userLocation)
                        RxBus.publish(MessageEvent(Constants.BUS_LOCATION_SAVED_EVENT, "Save location data: lat=${location.latitude}, lon=${location.longitude}"))
                        sendServiceMessage("Save location data: lat=${location.latitude}, lon=${location.longitude}")
                    }
                }
            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            private fun sendDataImmediate(latitude: String, longitude: String, timestamp: Long) {
                val call = webService.sendLocation(latitude, longitude, timestamp.toString())
                val userLocation = UserLocation(latitude, longitude, timestamp)
                call.enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(currentCall: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                        response?.let {
                            if (response.isSuccessful) {
                                // do nothing
                                sendServiceMessage("GPS data sending success")
                                RxBus.publish(MessageEvent(Constants.BUS_DATA_SENDING_SUCCESS, "GPS data sending success"))
                            } else {
                                when (response.code()) {
                                    400, 401, 402, 403, 404 -> {
                                        val metanim: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_METANIM_KEY, "")
                                        val user: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_USERNAME_KEY, "")
                                        val password: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_PASSWORD_KEY, "")

                                        RxBus.publish(MessageEvent(Constants.BUS_RELOGIN_EVENT, "Relogin for $metanim/$user"))
                                        sendServiceMessage("Relogin for $metanim/$user")

                                        val authCallResult = webService.login(metanim, user, password).execute()

                                        authCallResult?.let {
                                            if (authCallResult.code() == 200) {
                                                val result = currentCall!!.execute()
                                                when (result.code()) {
                                                    200 -> {}
                                                    else -> locStore.add(userLocation)
                                                }
                                            } else {
                                                locStore.add(userLocation)
                                            }
                                        }
                                    }
                                    500 -> {
                                        Log.d("UserLocationService", response.body().toString())
                                        locStore.add(userLocation)
                                    }
                                    else -> {
                                        // do something with error and save location somewhere
                                        Log.d("UserLocationService", response.body().toString())
                                        locStore.add(userLocation)
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                        Log.d("UserLocationService", "Something goes wrong: ${t?.localizedMessage}")
                    }

                })


            }

            private fun sendDataDeferred(dataList: MutableList<UserLocation>) {
                when (dataList.isEmpty()) {
                    true -> return
                    false -> {
                        dataList.forEach { elem ->
                            sendDataImmediate(elem.latitude?: "", elem.longitude?: "", elem.timestamp?: 0L)
                        }
                        dataList.clear()
                    }
                }
            }

            private fun sendServiceMessage(msg: String) {
                LocalBroadcastManager.getInstance(GoraGPSBeaconApp.instance!!.getContext())
                        .sendBroadcastSync(Intent(Constants.SERVICE_MESSAGE_ACTION)
                                .putExtra(Constants.SERVICE_MESSAGE_NAME, msg))
                Toast.makeText(GoraGPSBeaconApp.instance!!.getContext(), "Message: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }
}