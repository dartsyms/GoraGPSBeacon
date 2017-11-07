package ru.kot.it.goragpsbeacon.activities

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import ru.kot.it.goragpsbeacon.R
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.services.UserLocationService
import ru.kot.it.goragpsbeacon.utils.PrefUtils
import ru.kot.it.goragpsbeacon.utils.ServiceChecker

class MainActivity : AppCompatActivity() {

    private var isRegisteredUser: Boolean = false
    private var serviceIsRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isRegisteredUser = PrefUtils.getBooleanFromPrefs(this, Constants.PREF_IS_LOGGED_IN_KEY, false)
        serviceIsRunning = ServiceChecker.isServiceRunning(UserLocationService.javaClass, this)
        Log.d("MainActivity", "service in activity's onCreate: $serviceIsRunning")

        setupLaunchButton()

        toggle_tracking.setOnClickListener { view ->
            serviceIsRunning = !serviceIsRunning
            when (isRegisteredUser) {
                true -> {
                    if (!serviceIsRunning) {
                        startService(Intent(this, UserLocationService::class.java))
                        setupLaunchButton()
                        serviceIsRunning = !serviceIsRunning
                        Log.d("MainActivity", "service after click on start: $serviceIsRunning")
                    } else {
                        stopService(Intent(this, UserLocationService::class.java))
                        setupLaunchButton()
                        serviceIsRunning = !serviceIsRunning
                        Log.d("MainActivity", "service after click on stop: $serviceIsRunning")
                    }
                }
                false -> {
                    val authIntent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(authIntent, Constants.LOGIN_REQUEST_CODE)
                    Log.d("MainActivity", "service after return from login: $serviceIsRunning")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    isRegisteredUser = true || data.getBooleanExtra("loggedIn", true)
                    PrefUtils.saveToPrefs(this, Constants.PREF_METANIM_KEY, data.getStringExtra("metanim"))
                    PrefUtils.saveToPrefs(this, Constants.PREF_USERNAME_KEY, data.getStringExtra("user"))
                    PrefUtils.saveToPrefs(this, Constants.PREF_PASSWORD_KEY, data.getStringExtra("password"))
                }
            }
        }
    }

    private fun setupLaunchButton() {
        serviceIsRunning = ServiceChecker.isServiceRunning(UserLocationService.javaClass, applicationContext)
        when (serviceIsRunning) {
            true -> {
                toggle_tracking.setImageResource(R.drawable.ic_pause_black)
                Log.d("MainActivity", "service in launchbuttontrue: $serviceIsRunning")
            }
            false -> {
                toggle_tracking.setImageResource(R.drawable.ic_play_arrow_black)
                Log.d("MainActivity", "service in launchbuttonfalse: $serviceIsRunning")
            }
        }
    }

    private fun isAlive(): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any {
            Constants.LOCATION_SERVICE_NAME == it.service.className
        }
    }
}
