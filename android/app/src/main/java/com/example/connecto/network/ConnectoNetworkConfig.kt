package com.example.connecto.network

import com.example.connecto.BuildConfig

object ConnectoNetworkConfig {
    val BASE_URL: String = BuildConfig.BASE_URL
    val WS_BASE_URL: String = BuildConfig.WS_BASE_URL

    @Volatile
    var activeBaseUrl: String = BuildConfig.BASE_URL

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

