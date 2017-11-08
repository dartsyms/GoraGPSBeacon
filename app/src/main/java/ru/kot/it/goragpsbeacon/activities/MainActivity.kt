package ru.kot.it.goragpsbeacon.activities

import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import ru.kot.it.goragpsbeacon.R
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.infrastructure.GoraGPSBeaconApp
import ru.kot.it.goragpsbeacon.services.UserLocationService
import ru.kot.it.goragpsbeacon.utils.PrefUtils
import ru.kot.it.goragpsbeacon.utils.ServiceChecker

class MainActivity : AppCompatActivity() {

    private var isRegisteredUser: Boolean = false
    private var serviceIsRunning: Boolean = false
    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.ACTION_SERVER_READY -> {
                    val flag = intent.getBooleanExtra("alive", false)
                    setupLaunchButton(flag)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, IntentFilter(Constants.ACTION_SERVER_READY))

        isRegisteredUser = PrefUtils.getBooleanFromPrefs(GoraGPSBeaconApp.instance!!.getContext(),
                Constants.PREF_IS_LOGGED_IN_KEY, false)
        serviceIsRunning = ServiceChecker.isServiceRunning(UserLocationService.javaClass, this)

        toggle_tracking.setOnClickListener { view ->
            when (isRegisteredUser) {
                true -> {
                    if (!serviceIsRunning) {
                        startService(Intent(this, UserLocationService::class.java))
                        toggle_tracking.setImageResource(R.drawable.ic_pause_black)
                        serviceIsRunning = true
                    } else {
                        stopService(Intent(this, UserLocationService::class.java))
                        toggle_tracking.setImageResource(R.drawable.ic_play_arrow_black)
                        serviceIsRunning = false
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
        if (requestCode == Constants.LOGIN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    isRegisteredUser = data.getBooleanExtra("loggedIn", true)
                    PrefUtils.saveToPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_METANIM_KEY, data.getStringExtra("metanim"))
                    PrefUtils.saveToPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_USERNAME_KEY, data.getStringExtra("user"))
                    PrefUtils.saveToPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_PASSWORD_KEY, data.getStringExtra("password"))
                    val mCookie = data.getStringExtra("cookie") as String
                    mCookie.let {
                        val cookies: HashSet<String> = HashSet()
                        mCookie.split(";").forEach { cookies.add(it) }
                        PrefUtils.saveStringSetToPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_COOKIES_SET, cookies)
                    }

                    Log.d("MainActivity", "Saved in prefs from login: ${mCookie}")
                }

                startService(Intent(this, UserLocationService::class.java))
                serviceIsRunning = true
                toggle_tracking.setImageResource(R.drawable.ic_pause_black)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, IntentFilter(Constants.ACTION_SERVER_READY))
    }

    private fun setupLaunchButton(status: Boolean) {
        when (status) {
            true -> {
                toggle_tracking.setImageResource(R.drawable.ic_pause_black)
            }
            false -> {
                toggle_tracking.setImageResource(R.drawable.ic_play_arrow_black)
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
