package ru.kot.it.goragpsbeacon.apis

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WebAPI {
    @POST("/auth")
    fun login()

    @GET("/geo")
    fun sendLocation(@Query("latitude") latitude: String,
                     @Query("longitude") longitude: String,
                     @Query("timestamp") time: String)
}