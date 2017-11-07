package ru.kot.it.goragpsbeacon.infrastructure

import android.app.Application
import android.content.Context

class GoraGPSBeaconApp: Application() {

    companion object {
        var instance: GoraGPSBeaconApp? = null
    }

    fun getContext(): Context {
        return instance as Context
    }

    fun getInstance(): Context {
        if (instance == null)
            instance = GoraGPSBeaconApp()

        return instance as Context
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}

