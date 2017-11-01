package ru.kot.it.goragpsbeacon.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import ru.kot.it.goragpsbeacon.constants.Constants


class UserLocationService: Service(),
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var callsCount: Int = 0
    var locationIntent: Intent? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("Location Service", "GoogleAPI call")
        buildGoogleAPIClient()
        locationIntent = Intent(Constants.CURRENT_LOCATION)
    }

    protected fun buildGoogleAPIClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Location Service", "Check GoogleAPI Status")
        callsCount = 0
        when {
            mGoogleApiClient == null -> buildGoogleAPIClient()
            !mGoogleApiClient!!.isConnected -> {
                mGoogleApiClient?.connect()
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(Constants.LOCATION_INTERVAL)
                        .setFastestInterval(5000)
            }
        }
        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnected(bundle: Bundle?) {
        when {
            ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> return
            ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED -> return
        }

        val location: Location? = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        when (location) {
            null -> LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            else -> {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
                handleNewLocation(location)
            }
        }
    }


    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(this, "Disconnected. Please reconnect.", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "Connection failed.", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            handleNewLocation(location)
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdate()
    }

    private fun handleNewLocation(location: Location) {
        val mAccuracy: Float = location.accuracy
        if (mAccuracy < Constants.GPS_ACCURACY_LEVEL) {
            if (callsCount == 0) {
                callsCount += 1
                stopLocationUpdate()
            }
        }
        locationIntent?.putExtra(Constants.INTENT_LOCATION_VALUE, location)
        sendBroadcast(locationIntent)
    }

    private fun stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        mGoogleApiClient?.disconnect()
    }


    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {
        Toast.makeText(getBaseContext(), "Gps is turned on!! ", Toast.LENGTH_SHORT).show();
    }

    override fun onProviderDisabled(provider: String?) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
        Toast.makeText(baseContext, "Gps is turned off!! ", Toast.LENGTH_SHORT).show()
    }
}