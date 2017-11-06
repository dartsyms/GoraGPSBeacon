package ru.kot.it.goragpsbeacon.activities

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import ru.kot.it.goragpsbeacon.R
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.infrastructure.GoraGPSBeaconApp
import ru.kot.it.goragpsbeacon.utils.NetworkHelper
import ru.kot.it.goragpsbeacon.utils.PrefUtils
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.annotation.TargetApi
import android.text.TextUtils




class LoginActivity: AppCompatActivity() {

    private val TAG: String = "LoginActivity"

    private var mAuthTask: UserLoginTask? = null

    var mCookie: String? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_login)

        password.setOnEditorActionListener({ textView, id, keyEvent ->
            if (id == R.id.login_form || id == EditorInfo.IME_NULL) {
                attemptLogin()
            }
           false
        })

        sign_in_button.setOnClickListener({ view ->
            if (NetworkHelper.hasNetworkAccess(GoraGPSBeaconApp.getContext())) {
                attemptLogin()
            } else {
                Toast.makeText(GoraGPSBeaconApp.getContext(),
                        "No network connection available", Toast.LENGTH_LONG)
                        .show()
            }
        })

        metanim.setText(PrefUtils.getFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_METANIM_KEY, ""))
        username.setText(PrefUtils.getFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_USERNAME_KEY, ""))
        password.setText(PrefUtils.getFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_PASSWORD_KEY, ""))
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event.let {
            if (event!!.action == MotionEvent.ACTION_DOWN) {
                val view: View = currentFocus
                if (view is EditText) {
                    val outRect: Rect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        view.clearFocus()
                        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid metanim, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        metanim.error = null
        username.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val meta = metanim.text.toString()
        val user = username.text.toString()
        val pass = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(pass) && !isPasswordValid(pass)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for non-empty username
        if (!TextUtils.isEmpty(user) && !isUsernameValid(user)) {
            username.error = getString(R.string.error_invalid_username)
            focusView = username
            cancel = true
        }

        // Check for a valid metanim.
        if (TextUtils.isEmpty(meta)) {
            metanim.error = getString(R.string.error_field_required)
            focusView = metanim
            cancel = true
        } else if (!isMetanimValid(meta)) {
            metanim.error = getString(R.string.error_invalid_metanim)
            focusView = metanim
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask(meta, user, pass)
            (mAuthTask as UserLoginTask).execute()
        }
    }

    private fun isMetanimValid(metanim: String): Boolean {
        return metanim.length > 4
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    private fun isUsernameValid(username: String): Boolean {
        return username.length > 3
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate().setDuration(shortAnimTime.toLong())
                    .alpha(if (show) 0f else 1f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.setVisibility(if (show) View.GONE else View.VISIBLE)
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate().setDuration(shortAnimTime.toLong())
                    .alpha(if (show) 1f else 0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.setVisibility(if (show) View.VISIBLE else View.GONE)
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.setVisibility(if (show) View.VISIBLE else View.GONE)
            login_form.setVisibility(if (show) View.GONE else View.VISIBLE)
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mMetanim: String,
                                                   private val mUsername: String,
                                                   private val mPassword: String): AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // Attempt authentication
            val requestURL = "https://$mMetanim.gora.online"
            val postDataParams: HashMap<String, String> = HashMap()
            postDataParams.put("system", mMetanim)
            postDataParams.put("user", mUsername)
            postDataParams.put("password", mPassword)

            performPostCall(requestURL, postDataParams)

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null
            showProgress(false)

            if (success!!) {
                // Pass data back to MainActivity
                val returnIntent = Intent()
                returnIntent.putExtra("metanim", mMetanim)
                returnIntent.putExtra("loggedIn", true)
                returnIntent.putExtra("cookie", mCookie)
                setResult(Activity.RESULT_OK, returnIntent)

                // Saving user credentials on success
                PrefUtils.saveToPrefs(this@LoginActivity, Constants.PREF_METANIM_KEY, mMetanim)
                PrefUtils.saveToPrefs(this@LoginActivity, Constants.PREF_USERNAME_KEY, mUsername)
                PrefUtils.saveToPrefs(this@LoginActivity, Constants.PREF_PASSWORD_KEY, mPassword)
                mCookie.let { PrefUtils.saveToPrefs(this@LoginActivity, Constants.PREF_COOKIES, mCookie!!) }
                PrefUtils.saveBooleanToPrefs(this@LoginActivity, Constants.PREF_IS_LOGGED_IN_KEY, true)

                Log.d("LoginActivity", "Put back the intent with cookies: $mCookie")
                finish()
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    fun performPostCall(requestURL: String, postDataParams: HashMap<String, String>): String {
        val url: URL
        var response = ""
        var contentLength = ""

        try {
            contentLength = Integer.toString(getPostDataString(postDataParams).length)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        try {
            url = URL(requestURL + "/system/logon.php")
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.setRequestProperty("Content-Length", contentLength)
            conn.setRequestProperty("Connection", "close")
            conn.doInput = true
            conn.doOutput = true

            val os = conn.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(getPostDataString(postDataParams))

            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                var line: String
                val COOKIES_HEADER = "Set-Cookie"
                mCookie = conn.getHeaderField(COOKIES_HEADER)
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                br.use {
                    line = it.readLine()
                    response += line
                }
            } else {
                response = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.d(TAG, "Response from performPostCall: " + response)
        return response
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getPostDataString(params: HashMap<String, String>): String {
        val result = StringBuilder()
        var first = true
        for (entry in params.entries) {
            if (first)
                first = false
            else
                result.append("&")

            result.append(URLEncoder.encode(entry.key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(entry.value, "UTF-8"))
        }

        return result.toString()
    }

}