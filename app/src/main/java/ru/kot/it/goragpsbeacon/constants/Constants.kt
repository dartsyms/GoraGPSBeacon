package ru.kot.it.goragpsbeacon.constants


object Constants {

    val LOCATION_PERMISSIONS_REQUEST: Int = 99

    val LOCATION_SERVICE_NAME: String = "ru.kot.it.services.goragpsbeacon.UserLocationService"

    val USER_LOCATION_SERVICE: String = "UserLocationTracking"

    val LOCATION_INTERVAL: Long = 1000.toLong()  // In milliseconds

    val GPS_ACCURACY_LEVEL: Float = 10.toFloat() // In meters

    val BASE_URL: String = "gora.online"

    val LOGIN_REQUEST_CODE: Int = 901

    val PREF_COOKIES = "ru.kot.it.goragpsbeacon.COOKIES"
    val PREF_IS_LOGGED_IN_KEY = "ru.kot.it.goragpsbeacon.LOGGED"
    val PREF_METANIM_KEY = "ru.kot.it.goragpsbeacon.METANIM"
    val PREF_USERNAME_KEY = "ru.kot.it.goragpsbeacon.USER"
    val PREF_PASSWORD_KEY = "ru.kot.it.goragpsbeacon.PASS"
}