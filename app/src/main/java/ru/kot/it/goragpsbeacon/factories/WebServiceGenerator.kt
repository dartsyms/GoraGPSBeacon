package ru.kot.it.goragpsbeacon.factories

import android.content.Context
import android.util.Log
import com.alterego.advancedandroidlogger.implementations.DetailedAndroidLogger
import com.alterego.advancedandroidlogger.interfaces.IAndroidLogger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import ru.kot.it.goragpsbeacon.constants.Constants
import ru.kot.it.goragpsbeacon.infrastructure.GoraGPSBeaconApp
import ru.kot.it.goragpsbeacon.utils.PrefUtils
import java.util.*

object WebServiceGenerator {

    private val metanim: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.getContext(), Constants.PREF_METANIM_KEY, "")
    private val requestUrl: String = if (metanim.isNotEmpty()) {
        "https://$metanim." + Constants.BASE_URL
    } else {
        "https://" + Constants.BASE_URL
    }

    private val httpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder().baseUrl(requestUrl)

    fun <S> createService(serviceClass: Class<S>, ctx: Context): S {

        httpClientBuilder.interceptors().add(LoggingInterceptor)
        httpClientBuilder.interceptors().add(SendCookiesInterceptor)
        httpClientBuilder.interceptors().add(ReceiveCookiesInterceptor)


        val httpClient: OkHttpClient = httpClientBuilder.build()
        val retrofit: Retrofit = retrofitBuilder.client(httpClient).build()
        return retrofit.create(serviceClass)
    }

    private object LoggingInterceptor: Interceptor {
        val logger: DetailedAndroidLogger = DetailedAndroidLogger("OkHTTP Details", IAndroidLogger.LoggingLevel.DEBUG)
        override fun intercept(chain: Interceptor.Chain?): Response {
            chain.let {
                val request: Request = chain!!.request()
                val t1: Long = System.nanoTime()
                logger.info(String.format(Locale.getDefault(), "Sending request %s on %s%n%s",
                        request.url(), chain.connection(), request.headers()))

                val response: Response = chain.proceed(request)
                val t2: Long = System.nanoTime()
                logger.info(String.format(Locale.getDefault(), "Received response for %s in %.1fms%n%s",
                        response.request().url(), (t1 - t2) / 1e6f, response.headers()))
                return response
            }
        }
    }


    private object SendCookiesInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response {
            chain.let {
                val builder: Request.Builder = chain!!.request().newBuilder()
                val preferences: HashSet<String> = PrefUtils.getStringSetFromPrefs(GoraGPSBeaconApp.getContext(),
                        Constants.PREF_COOKIES, HashSet<String>())
                for (cookie: String in preferences) {
                    builder.addHeader("Cookie", cookie)
                    Log.v("AddCookiesInterceptor", "Add Header: $cookie")
                }
                return  chain.proceed(builder.build())
            }

        }
    }

    private object ReceiveCookiesInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response {
            chain.let {
                val originalResponse: Response = chain!!.proceed(chain.request())
                if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                    val cookies: HashSet<String> = HashSet<String>()
                    for (header: String in originalResponse.headers("Set-Cookie")) {
                        cookies.add(header)
                    }
                    PrefUtils.saveStringSetToPrefs(GoraGPSBeaconApp.getContext(),
                            Constants.PREF_COOKIES, cookies)
                }
                return originalResponse
            }
        }
    }
}