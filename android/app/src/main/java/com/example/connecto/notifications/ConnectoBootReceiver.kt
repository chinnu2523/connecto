package com.example.connecto.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver for device boot and app update to clear any legacy notifications.
 */
class ConnectoBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ConnectoBootReceiver"
        const val ACTION_RESTART_SERVICE = "com.connecto.app.RESTART_REALTIME_SERVICE"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "ConnectoBootReceiver received action: $action")
        ConnectoRealtimeService.dismissConnectoActiveNotification(context)
    }
}
