package ru.kot.it.goragpsbeacon.utils

import android.app.ActivityManager
import android.content.Context

object ServiceChecker {

    fun isServiceRunning(serviceClass: Class<Any>, context: Context): Boolean {
        val manager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }
}