package ru.kot.it.goragpsbeacon.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import ru.kot.it.goragpsbeacon.constants.Constants


object GPSHelper {

    fun hasGPSEnabled(context: Context): Boolean {
        val locManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun checkLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val alertBuilder = AlertDialog.Builder(context)
                            .setCancelable(true)
                            .setTitle("Permission necessary")
                            .setMessage("You should allow location services")
                            .setPositiveButton(android.R.string.yes, { dialogInterface, i ->
                                ActivityCompat.requestPermissions(
                                        context,
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        Constants.LOCATION_PERMISSIONS_REQUEST)
                            })
                    val alert: AlertDialog = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            Constants.LOCATION_PERMISSIONS_REQUEST)
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

}