package ru.kot.it.goragpsbeacon.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import ru.kot.it.goragpsbeacon.R
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.infrastructure.GoraGPSBeaconApp
import ru.kot.it.goragpsbeacon.infrastructure.RxBus
import ru.kot.it.goragpsbeacon.models.MessageEvent
import ru.kot.it.goragpsbeacon.services.UserLocationService
import ru.kot.it.goragpsbeacon.utils.PrefUtils

class MainActivity : AppCompatActivity() {

    private var isRegisteredUser: Boolean = false
    private val echo = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "pong" -> {
                    UserLocationService.IS_SERVICE_RUNNING = true
                    setupLaunchButton(UserLocationService.IS_SERVICE_RUNNING)
                }
            }
        }
    }

    private var disposableMessage: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLaunchButton(UserLocationService.IS_SERVICE_RUNNING)

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(echo, IntentFilter("pong"))
        LocalBroadcastManager.getInstance(this)
                .sendBroadcastSync(Intent("ping"))

        isRegisteredUser = PrefUtils.getBooleanFromPrefs(GoraGPSBeaconApp.instance!!.getContext(),
                Constants.PREF_IS_LOGGED_IN_KEY, false)

        toggle_tracking.setOnClickListener { view ->
            when (isRegisteredUser) {
                true -> {
                    if (!UserLocationService.IS_SERVICE_RUNNING) {
                        startService(Intent(this, UserLocationService::class.java))
                        UserLocationService.IS_SERVICE_RUNNING = true
                        setupLaunchButton(UserLocationService.IS_SERVICE_RUNNING)
                    } else {
                        stopService(Intent(this, UserLocationService::class.java))
                        UserLocationService.IS_SERVICE_RUNNING = false
                        setupLaunchButton(UserLocationService.IS_SERVICE_RUNNING)
                    }
                }
                false -> {
                    val authIntent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(authIntent, Constants.LOGIN_REQUEST_CODE)
                }
            }
        }

        disposableMessage = RxBus.listen(MessageEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showServiceMessage(it.message)
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

                    if (isRegisteredUser) {
                        startService(Intent(this, UserLocationService::class.java))
                        UserLocationService.IS_SERVICE_RUNNING = true
                        setupLaunchButton(UserLocationService.IS_SERVICE_RUNNING)
                    }
                }
            }
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(echo)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(echo, IntentFilter("pong"))
    }

    override fun onDestroy() {
        disposableMessage?.dispose()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
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

    private fun showServiceMessage(msg: String) {
        Toast.makeText(this, "Message: $msg", Toast.LENGTH_LONG).show()
        Log.d("RxBusMessage", "Message from service: $msg")
    }
}
