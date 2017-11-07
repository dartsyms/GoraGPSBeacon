package ru.kot.it.goragpsbeacon.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import ru.kot.it.goragpsbeacon.apis.WebAPI
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.factories.WebServiceGenerator
import ru.kot.it.goragpsbeacon.infrastructure.GoraGPSBeaconApp
import ru.kot.it.goragpsbeacon.models.UserLocation
import ru.kot.it.goragpsbeacon.utils.GPSHelper
import ru.kot.it.goragpsbeacon.utils.PrefUtils
import java.util.*
import kotlin.collections.ArrayList


class UserLocationService: Service() {

    var locationManager: LocationManager? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        when (GPSHelper.checkLocationPermission(GoraGPSBeaconApp.getContext())) {

            GPSHelper.hasGPSProviderEnabled(GoraGPSBeaconApp.getContext()) -> {
                try {
                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
                } catch(e: SecurityException) {
                    Log.e(TAG, "Fail to request location update", e)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "GPS provider does not exist", e)
                }
            }

            GPSHelper.hasNetworkProviderEnabled(GoraGPSBeaconApp.getContext()) -> {
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

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0..locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }

    companion object {
        val TAG = Constants.USER_LOCATION_SERVICE
        val INTERVAL = Constants.LOCATION_INTERVAL
        val DISTANCE = Constants.GPS_ACCURACY_LEVEL

        val locStore: ArrayList<UserLocation> = ArrayList()

        val locationListeners = arrayOf(
                ULTListener(LocationManager.GPS_PROVIDER),
                ULTListener(LocationManager.NETWORK_PROVIDER)
        )

        val webService by lazy {
            WebServiceGenerator.createService(WebAPI::class.java, GoraGPSBeaconApp.getContext())
        }

        class ULTListener(provider: String) : LocationListener {

            val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                // TODO: Send current location to the server after network availability check (or store them in array)
                location.let {
                    sendData(location!!, Calendar.getInstance().timeInMillis)
                }
            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            private fun sendData(location: Location, timestamp: Long) {
                // TODO: Implement via web services with auth check

                val call = webService.sendLocation(location.latitude.toString(), location.longitude.toString(), timestamp.toString())
                val result = call.execute()
                if (!result.isSuccessful) {
                    when (result.code()) {
                        400, 401, 402, 403, 404 -> {
                            val metanim: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_METANIM_KEY, "")
                            val user: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_USERNAME_KEY, "")
                            val password: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_PASSWORD_KEY, "")
                            webService.login(metanim, user, password)
                        }
                        else -> {
                            // do something with error and save location somewhere
                            Log.d("UserLocationService", result.errorBody().toString())
                            val userLocation = UserLocation(location.latitude.toString(), location.longitude.toString(), timestamp)
                            locStore.add(userLocation)
                        }
                    }
                } else {
                    Log.d("UserLocationService", result.body().toString())
                }
            }

        }
    }
}