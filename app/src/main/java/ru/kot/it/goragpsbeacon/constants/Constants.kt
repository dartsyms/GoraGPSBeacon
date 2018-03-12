package ru.kot.it.goragpsbeacon.constants


object Constants {

    val LOCATION_PERMISSIONS_REQUEST: Int = 99

    val LOCATION_SERVICE_NAME: String = "ru.kot.it.services.goragpsbeacon.UserLocationService"

    val USER_LOCATION_SERVICE: String = "UserLocationTracking"

    val LOCATION_INTERVAL: Long = 1000.toLong()  // In milliseconds

    val GPS_ACCURACY_LEVEL: Float = 10.toFloat() // In meters

    val BASE_URL: String = "gora.online"

    val LOGIN_REQUEST_CODE: Int = 901

    val PREF_COOKIES_SET = "ru.kot.it.goragpsbeacon.COOKIES_SET"
    val PREF_COOKIES_STRING = "ru.kot.it.goragpsbeacon.COOKIES_STRING"
    val PREF_IS_LOGGED_IN_KEY = "ru.kot.it.goragpsbeacon.LOGGED"
    val PREF_METANIM_KEY = "ru.kot.it.goragpsbeacon.METANIM"
    val PREF_USERNAME_KEY = "ru.kot.it.goragpsbeacon.USER"
    val PREF_PASSWORD_KEY = "ru.kot.it.goragpsbeacon.PASS"

    val BUS_SERVICE_STARTED_EVENT = 906
    val BUS_LOCATION_SENT_EVENT = 902
    val BUS_LOCATION_SAVED_EVENT = 903
    val BUS_RELOGIN_EVENT = 904
    val BUS_DATA_SENDING_SUCCESS = 905
}