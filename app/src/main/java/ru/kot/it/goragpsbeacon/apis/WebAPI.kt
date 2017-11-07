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

    @GET("/geo")
    fun sendLocation(@Query("latitude") lat: String,
                     @Query("longitude") lon: String,
                     @Query("timestamp") time: String): Call<ResponseBody>
}