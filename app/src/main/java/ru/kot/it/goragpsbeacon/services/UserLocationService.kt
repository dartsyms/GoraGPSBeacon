package ru.kot.it.goragpsbeacon.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import ru.kot.it.goragpsbeacon.constants.Constants
import java.util.*


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

        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
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

        val locationListeners = arrayOf(
                ULTLocationListener(LocationManager.GPS_PROVIDER),
                ULTLocationListener(LocationManager.NETWORK_PROVIDER)
        )

        class ULTLocationListener(provider: String) : LocationListener {

            val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                // TODO: Send current location to the server after network availability check (or store them in array)
                location.let {
                    sendData(location!!, Calendar.getInstance().timeInMillis.toString())
                }

            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            private fun sendData(location: Location, timestamp: String) {
                // TODO: Implement via web services with auth check
            }

        }
    }
}