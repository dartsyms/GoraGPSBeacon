package ru.kot.it.goragpsbeacon.activities

import android.app.Activity
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

    var isRegisteredUser: Boolean = false
    var serviceIsRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isRegisteredUser = PrefUtils.getBooleanFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_IS_LOGGED_IN_KEY, false)
        serviceIsRunning = ServiceChecker.isServiceRunning(UserLocationService.javaClass, GoraGPSBeaconApp.getContext())

        when (serviceIsRunning) {
            true -> {
                toggle_tracking.setImageResource(R.drawable.ic_pause_black)
            }
            false -> {
                toggle_tracking.setImageResource(R.drawable.ic_play_arrow_black)
            }
        }

        toggle_tracking.setOnClickListener { view ->
            when (isRegisteredUser) {
                true -> {
                    if (!serviceIsRunning) {
                        startService(Intent(this, UserLocationService::class.java))
                        toggle_tracking.setImageResource(R.drawable.ic_pause_black)
                        serviceIsRunning = true
                    }
                }
                false -> {
                    val authIntent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(authIntent, Constants.LOGIN_REQUEST_CODE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data.let {
                    isRegisteredUser = data!!.getBooleanExtra("loggedIn", true)
                    PrefUtils.saveToPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_METANIM_KEY, data.getStringExtra("metanim"))
                    PrefUtils.saveToPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_USERNAME_KEY, data.getStringExtra("user"))
                    PrefUtils.saveToPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_PASSWORD_KEY, data.getStringExtra("password"))
                }
            }
        }
    }
}
