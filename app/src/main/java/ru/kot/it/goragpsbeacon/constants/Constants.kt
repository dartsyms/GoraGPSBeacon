package ru.kot.it.goragpsbeacon.constants


object Constants {

    const val HTTP_HEADER_USER_AGENT = "Android GoraGPSBeacon v1.0.3"

    const val LOCATION_PERMISSIONS_REQUEST: Int = 99

    const val LOCATION_SERVICE_NAME: String = "ru.kot.it.services.goragpsbeacon.UserLocationService"

    const val USER_LOCATION_SERVICE: String = "UserLocationTracking"

    const val LOCATION_INTERVAL: Long = 1000.toLong()  // In milliseconds

    const val GPS_ACCURACY_LEVEL: Float = 10.toFloat() // In meters

    const val BASE_URL: String = "gora.online"

    const val LOGIN_REQUEST_CODE: Int = 901

    const val PREF_COOKIES_SET = "ru.kot.it.goragpsbeacon.COOKIES_SET"
    const val PREF_COOKIES_STRING = "ru.kot.it.goragpsbeacon.COOKIES_STRING"
    const val PREF_IS_LOGGED_IN_KEY = "ru.kot.it.goragpsbeacon.LOGGED"
    const val PREF_METANIM_KEY = "ru.kot.it.goragpsbeacon.METANIM"
    const val PREF_USERNAME_KEY = "ru.kot.it.goragpsbeacon.USER"
    const val PREF_PASSWORD_KEY = "ru.kot.it.goragpsbeacon.PASS"

    const val BUS_SERVICE_STARTED_EVENT = 906
    const val BUS_LOCATION_SENT_EVENT = 902
    const val BUS_LOCATION_SAVED_EVENT = 903
    const val BUS_RELOGIN_EVENT = 904
    const val BUS_DATA_SENDING_SUCCESS = 905

    const val SERVICE_MESSAGE_ACTION = "service"
    const val SERVICE_MESSAGE_NAME = "serviceMessage"
}