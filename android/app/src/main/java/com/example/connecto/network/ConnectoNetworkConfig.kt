package com.example.connecto.network

import com.example.connecto.BuildConfig

object ConnectoNetworkConfig {
    val BASE_URL: String = BuildConfig.BASE_URL
    val WS_BASE_URL: String = BuildConfig.WS_BASE_URL

    const val CLOUD_SERVER_URL: String = "https://connecto.fun"
    const val CLOUD_WS_URL: String = "wss://connecto.fun"

    @Volatile
    var activeBaseUrl: String = BuildConfig.BASE_URL

    @Volatile
    var activeServerMode: String = "cloud_server" // "cloud_server" or "local_server"

    fun getApiUrl(endpoint: String): String {
        val cleanEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        return "$activeBaseUrl$cleanEndpoint"
    }

    fun getWsUrl(endpoint: String): String {
        val cleanEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        val wsBase = if (activeBaseUrl.startsWith("https://")) {
            activeBaseUrl.replaceFirst("https://", "wss://")
        } else if (activeBaseUrl.startsWith("http://")) {
            activeBaseUrl.replaceFirst("http://", "ws://")
        } else {
            BuildConfig.WS_BASE_URL
        }
        return "$wsBase$cleanEndpoint"
    }
}

