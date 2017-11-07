package ru.kot.it.goragpsbeacon.apis

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface WebAPI {
    @Headers("Content-Type:application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("/system/logon.php")
    fun login(@Field("system") metanim: String,
              @Field("username") username: String,
              @Field("password") password: String): Call<ResponseBody>

    @GET("/system/geo.php")
    fun sendLocation(@Query("lat") lat: String,
                     @Query("lng") lng: String,
                     @Query("timestamp") time: String): Call<ResponseBody>
}