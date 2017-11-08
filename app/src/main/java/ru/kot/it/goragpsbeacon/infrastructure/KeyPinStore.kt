package ru.kot.it.goragpsbeacon.infrastructure

import java.io.BufferedInputStream
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory


class KeyPinStore @Throws(CertificateException::class, IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
private constructor() {
    val context = SSLContext.getInstance("TLS")

    init {
        // https://developer.android.com/training/articles/security-ssl.html
        // Load CAs from an InputStream (could be from a resource or ByteArrayInputStream or ...)
        val cf = CertificateFactory.getInstance("X.509")
        // gora.crt should be in the Assets directory (tip from here http://littlesvr.ca/grumble/2014/07/21/android-programming-connect-to-an-https-server-with-self-signed-certificate/)
        val caInput = BufferedInputStream(GoraGPSBeaconApp.instance!!.getContext().assets.open("gora.crt"))

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)

        caInput.use { input ->
            val ca: Certificate = cf.generateCertificate(input)
            System.out.println("ca=" + (ca as X509Certificate).subjectDN)
            keyStore.setCertificateEntry("ca", ca)
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(keyStore)

        context.init(null, tmf.trustManagers, null)
    }

    companion object {

        private var instance: KeyPinStore? = null

        @Synchronized
        @Throws(CertificateException::class, IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
        fun createInstance(): KeyPinStore {
            if (instance == null) {
                instance = KeyPinStore()
            }
            return instance as KeyPinStore
        }
    }
}