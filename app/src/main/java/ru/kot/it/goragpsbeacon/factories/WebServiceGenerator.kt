package ru.kot.it.goragpsbeacon.factories

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import ru.kot.it.goragpsbeacon.constants.Constants

object WebServiceGenerator {

    private val httpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder().baseUrl(Constants.BASE_URL)

    fun <S> createService(serviceClass: Class<S>, ctx: Context): S {
        /*
         * Implement logics with interceptors
         */

        val httpClient: OkHttpClient = httpClientBuilder.build()
        val retrofit: Retrofit = retrofitBuilder.client(httpClient).build()
        return retrofit.create(serviceClass)
    }

    private object LoggingInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private object AuthenticationInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}