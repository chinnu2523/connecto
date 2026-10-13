package com.example.connecto.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Starts ConnectoRealtimeService after device boot, app update, or task removal
 * so real-time calls and notifications continue working when the app is closed.
 */
class ConnectoBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ConnectoBootReceiver"
        const val ACTION_RESTART_SERVICE = "com.connecto.app.RESTART_REALTIME_SERVICE"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "ConnectoBootReceiver received action: $action")
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == ACTION_RESTART_SERVICE
        ) {
            ConnectoRealtimeService.startService(context)
        }
    }
}
