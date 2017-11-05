package ru.kot.it.goragpsbeacon.infrastructure

import android.app.Application
import android.content.Context

class GoraGPSBeaconApp: Application() {

    companion object {

        private var instance: GoraGPSBeaconApp = this.create()

        init {
            instance = this.create()
        }

        private fun create(): GoraGPSBeaconApp = GoraGPSBeaconApp()

        fun getContext(): Context {
            return instance
        }

    }

    override fun onCreate() {
        super.onCreate()
    }

}

