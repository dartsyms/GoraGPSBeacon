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
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

object WebServiceGenerator {

    private val metanim: String = PrefUtils.getFromPrefs(GoraGPSBeaconApp.instance!!.getContext(), Constants.PREF_METANIM_KEY, "")
    private val requestUrl: String = if (metanim.isNotEmpty()) {
        "https://$metanim." + Constants.BASE_URL
    } else {
        "https://" + Constants.BASE_URL
    }

    private val httpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .sslSocketFactory(getSSlConfig(GoraGPSBeaconApp.instance!!.getContext()).socketFactory)
    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder().baseUrl(requestUrl)


    fun <S> createService(serviceClass: Class<S>, ctx: Context): S {

        Log.d("WebServiceGenerator", "In createService() before add interceptors")

        httpClientBuilder.interceptors().add(LoggingInterceptor)
        httpClientBuilder.interceptors().add(SendCookiesInterceptor)
        httpClientBuilder.interceptors().add(ReceiveCookiesInterceptor)

        httpClientBuilder.interceptors().add(Interceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder()
                    .header("User-Agent", "Android GoraGPSBeacon v1.0.0")
                    .method(original.method(), original.body())
                    .build()
            chain.proceed(request)
        })

        val httpClient: OkHttpClient = httpClientBuilder.build()
        val retrofit: Retrofit = retrofitBuilder.client(httpClient).build()
        return retrofit.create(serviceClass)
    }

    @Throws(CertificateException::class, IOException::class)
    private fun getSSlConfig(ctx: Context): SSLContext {

        Log.d("WebServiceGenerator", "In getSSLConfig() start")

        // Loading CAs from an InputStream (assets)
        val cf: CertificateFactory? = CertificateFactory.getInstance("X.509")
        var ca: Certificate? = null
        var certIn: InputStream? = null
        try {
            certIn = ctx.resources.assets.open("gora.crt")
            ca = cf?.generateCertificate(certIn)
        } finally {
            certIn?.close()
        }

        // Creating a KeyStore containing our trusted CAs
        val keyStoreType: String = KeyStore.getDefaultType()
        val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        // Creating a TrustManager that trusts the CAs in our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(keyStore)

        // Creating an SSLSocketFactory that uses the TrustManager from above
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        return sslContext
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
                val preferences: HashSet<String> = PrefUtils.getStringSetFromPrefs(GoraGPSBeaconApp.instance!!.getContext(),
                        Constants.PREF_COOKIES_SET, HashSet())
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
                    val cookies: HashSet<String> = HashSet()
                    cookies += originalResponse.headers("Set-Cookie")
                    PrefUtils.saveStringSetToPrefs(GoraGPSBeaconApp.instance!!.getContext(),
                            Constants.PREF_COOKIES_SET, cookies)
                }
                return originalResponse
            }
        }
    }
}