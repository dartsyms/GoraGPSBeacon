package ru.kot.it.goragpsbeacon.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


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

    fun getHashMapFromPrefs(context: Context, key: String, defaultValue: HashMap<String, String>): HashMap<String, String> {
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        return  try {
            val json = sharedPrefs.getString(key, "")
            val type = object:TypeToken<HashMap<String, String>>(){}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }

    }

    fun saveHashMapToPrefs(context: Context, key: String, value: HashMap<String, String>) {
        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(value)

        Log.d("PrefUtils", "Save cookie string after response: $json")

        editor.putString(key, json)
        editor.commit()
    }

    fun clearAllValuesFromPrefs(context: Context) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

}