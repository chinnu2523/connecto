package com.connecto.store.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Reschedules StoreUpdateJobService on device boot or app update so the background
 * update engine continues running without requiring the user to open Our Store.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootCompletedReceiver", "Received broadcast: $action")
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            StoreUpdateJobService.schedule(context)
            StoreUpdateAlarmReceiver.scheduleNext(context)
        }
    }
}
