package ru.kot.it.goragpsbeacon.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetworkHelper {

    fun hasNetworkAccess(context: Context): Boolean {
        val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
}