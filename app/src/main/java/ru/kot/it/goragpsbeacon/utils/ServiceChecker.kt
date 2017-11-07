package ru.kot.it.goragpsbeacon.utils

import android.app.ActivityManager
import android.content.Context

object ServiceChecker {

    fun isServiceRunning(serviceClass: Class<Any>, context: Context): Boolean {
        val manager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any { serviceClass.name == it.service.className }
    }

}