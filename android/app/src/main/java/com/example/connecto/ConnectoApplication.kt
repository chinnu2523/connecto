package com.example.connecto

import android.app.Application
import android.content.Context
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

/**
 * ConnectoApplication sets up global uncaught exception catching to capture cold-start
 * and runtime crashes before the process dies, persisting them to SharedPreferences
 * for diagnostic display and copying on the subsequent launch.
 */
class ConnectoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        installGlobalCrashCatcher()
    }

    private fun installGlobalCrashCatcher() {
        try {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    throwable.printStackTrace(pw)
                    val fullStackTrace = sw.toString()
                    val lines = fullStackTrace.lines()
                    val topLines = lines.take(16).joinToString("\n")
                    val crashLog = "${throwable.javaClass.name}: ${throwable.message ?: "No message"}\n$topLines"

                    val prefs = getSharedPreferences("connecto_crash_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("last_crash_log", crashLog).commit()
                    Log.e("ConnectoCrashCatcher", "Crash log captured:\n$crashLog")
                } catch (t: Throwable) {
                    Log.e("ConnectoCrashCatcher", "Failed to persist crash log: ${t.message}")
                }
                defaultHandler?.uncaughtException(thread, throwable)
            }
        } catch (t: Throwable) {
            Log.e("ConnectoCrashCatcher", "Failed to install uncaught exception handler: ${t.message}")
        }
    }
}
