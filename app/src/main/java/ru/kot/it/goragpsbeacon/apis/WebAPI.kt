package ru.kot.it.goragpsbeacon.apis

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WebAPI {
    @POST("/system/logon.php")
    fun login(): Call<ResponseBody>

    @GET("/geo")
    fun sendLocation(@Query("latitude") latitude: String,
                     @Query("longitude") longitude: String,
                     @Query("timestamp") time: String): Call<ResponseBody>
}