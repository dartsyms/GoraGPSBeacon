package ru.kot.it.goragpsbeacon.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


object PrefUtils {

    fun saveToPrefs(context: Context, key: String, value: String) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun saveBooleanToPrefs(context: Context, key: String, value: Boolean) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun saveStringSetToPrefs(context: Context, key: String, value: HashSet<String>) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putStringSet(key, value)
        editor.apply()
    }

    fun getFromPrefs(context: Context, key: String, defaultValue: String): String {
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return try {
            sharedPrefs.getString(key, defaultValue)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    fun getBooleanFromPrefs(context: Context, key: String, defaultValue: Boolean): Boolean {
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return try {
            sharedPrefs.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    fun getStringSetFromPrefs(context: Context, key: String, defaultValue: HashSet<String>): HashSet<String> {
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return try {
            sharedPrefs.getStringSet(key, defaultValue) as HashSet<String>
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

}