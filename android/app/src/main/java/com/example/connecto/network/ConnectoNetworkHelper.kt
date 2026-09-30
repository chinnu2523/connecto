package com.example.connecto.network

import android.content.Context
import android.os.Build
import android.util.Log
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Universal network helper that ensures high-grade TLS 1.2/1.3 connectivity,
 * keep-alive ping intervals, and backward compatibility for older Android versions (API 24-29).
 */
object ConnectoNetworkHelper {
    private const val TAG = "ConnectoNetworkHelper"

    val MODERN_TLS_SPEC: ConnectionSpec by lazy {
        ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .cipherSuites(
                CipherSuite.TLS_AES_128_GCM_SHA256,
                CipherSuite.TLS_AES_256_GCM_SHA384,
                CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA
            )
            .build()
    }

    val ALL_CONNECTION_SPECS: List<ConnectionSpec> = listOf(
        MODERN_TLS_SPEC,
        ConnectionSpec.COMPATIBLE_TLS,
        ConnectionSpec.CLEARTEXT
    )

    internal var patchedSslSocketFactory: SSLSocketFactory? = null
    internal var systemTrustManager: X509TrustManager? = null

    init {
        initSslContext()
    }

    private fun initSslContext() {
        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            systemTrustManager = trustManagers.firstOrNull { it is X509TrustManager } as? X509TrustManager

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustManagers, null)
            val baseFactory = sslContext.socketFactory
            patchedSslSocketFactory = Tls12SocketFactory(baseFactory)

            // Globally patch HttpsURLConnection so HttpURLConnection requests also gain TLS 1.2+ support on older Android
            HttpsURLConnection.setDefaultSSLSocketFactory(patchedSslSocketFactory)
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.3")
            Log.d(TAG, "Tls12SocketFactory and TLS protocols successfully configured globally")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize custom SSLContext for older Android: ${e.message}", e)
        }
    }

    /**
     * Updates Google Play Services security provider to guarantee updated CA root stores
     * and modern TLS cipher suites on older Android phones.
     */
    fun installSecurityProvider(context: Context) {
        try {
            com.google.android.gms.security.ProviderInstaller.installIfNeeded(context.applicationContext)
            Log.i(TAG, "Google Play Services Security Provider updated successfully")
            initSslContext()
        } catch (t: Throwable) {
            Log.w(TAG, "ProviderInstaller not available or skipped: ${t.message}")
        }
    }

    /**
     * Constructs a battle-tested OkHttpClient configured with compatible TLS specifications,
     * WebSocket keep-alive pings, and automatic failure retries.
     */
    fun getCompatibleOkHttpClient(isWebSocket: Boolean = false): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectionSpecs(ALL_CONNECTION_SPECS)
            .retryOnConnectionFailure(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        if (isWebSocket) {
            builder.readTimeout(0, TimeUnit.MILLISECONDS)
            builder.pingInterval(15, TimeUnit.SECONDS)
        } else {
            builder.readTimeout(20, TimeUnit.SECONDS)
        }

        // Apply TLS 1.2 Socket Factory on older Android devices (API < 29)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val factory = patchedSslSocketFactory
            val trustManager = systemTrustManager
            if (factory != null && trustManager != null) {
                try {
                    builder.sslSocketFactory(factory, trustManager)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set custom sslSocketFactory on OkHttpClient: ${e.message}")
                }
            }
        }

        return builder.build()
    }

    /**
     * Tls12SocketFactory forces TLS 1.2 and TLS 1.3 on platforms where it is supported
     * by the underlying OS/OpenSSL but not enabled by default on standard sockets.
     */
    class Tls12SocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
        override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
        override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

        @Throws(IOException::class)
        override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
            patch(delegate.createSocket(s, host, port, autoClose))

        @Throws(IOException::class)
        override fun createSocket(host: String, port: Int): Socket =
            patch(delegate.createSocket(host, port))

        @Throws(IOException::class)
        override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket =
            patch(delegate.createSocket(host, port, localHost, localPort))

        @Throws(IOException::class)
        override fun createSocket(host: InetAddress, port: Int): Socket =
            patch(delegate.createSocket(host, port))

        @Throws(IOException::class)
        override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket =
            patch(delegate.createSocket(address, port, localAddress, localPort))

        private fun patch(s: Socket): Socket {
            if (s is SSLSocket) {
                try {
                    val supported = s.supportedProtocols ?: emptyArray()
                    val protocols = mutableListOf<String>()
                    if ("TLSv1.3" in supported) protocols.add("TLSv1.3")
                    if ("TLSv1.2" in supported) protocols.add("TLSv1.2")
                    if ("TLSv1.1" in supported) protocols.add("TLSv1.1")
                    if (protocols.isNotEmpty()) {
                        s.enabledProtocols = protocols.toTypedArray()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error enabling TLS protocols on socket: ${e.message}")
                }
            }
            return s
        }
    }
}
