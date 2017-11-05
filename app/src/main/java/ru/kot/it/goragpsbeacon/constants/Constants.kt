package ru.kot.it.goragpsbeacon.constants


object Constants {

    val LOCATION_PERMISSIONS_REQUEST: Int = 99

    val CURRENT_LOCATION: String = "MY_CURRENT_LOCATION"

    val INTENT_LOCATION_VALUE: String = "currentLocation"

    val USER_LOCATION_SERVICE: String = "UserLocationTracking"

    val LOCATION_INTERVAL: Long = 1000.toLong()  // In milliseconds

    val GPS_ACCURACY_LEVEL: Float = 10.toFloat() // In meters

    val BASE_URL: String = "gora.online"

    val PREF_COOKIES = "cookiesInSharedPreferences"
    val PREF_IS_LOGGED_IN_KEY = "loggedIn"
    val PREF_METANIM_KEY = "metanim"
    val PREF_USERNAME_KEY = "userName"
    val PREF_PASSWORD_KEY = "userPassword"
}