package ru.kot.it.goragpsbeacon.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ru.kot.it.goragpsbeacon.R
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.infrastructure.GoraGPSBeaconApp
import ru.kot.it.goragpsbeacon.services.UserLocationService
import ru.kot.it.goragpsbeacon.utils.PrefUtils
import ru.kot.it.goragpsbeacon.utils.ServiceChecker

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_tracking.setOnClickListener { view ->
            val isRegisteredUser: Boolean = PrefUtils.getBooleanFromPrefs(GoraGPSBeaconApp.getContext(),
                    Constants.PREF_IS_LOGGED_IN_KEY, false)
            val serviceIsRunning: Boolean = ServiceChecker.isServiceRunning(UserLocationService.javaClass, GoraGPSBeaconApp.getContext())

            when (isRegisteredUser) {
                true -> {
                    val trackingIntent: Intent = Intent(this, UserLocationService::class.java)
                    startService(trackingIntent)
                }
                false -> {
                    val authIntent: Intent = Intent(this, LoginActivity::class.java)
                    startActivity(authIntent)
                }
            }

        }
    }
}
