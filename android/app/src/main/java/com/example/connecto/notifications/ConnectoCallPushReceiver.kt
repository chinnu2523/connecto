package com.example.connecto.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.connecto.voice.IncomingCallActivity
import com.example.connecto.voice.VoiceCallManager

/**
 * Direct BroadcastReceiver for high-priority incoming call push and wake events.
 * Can be triggered directly by system broadcasts, FCM triggers, or local alarm wakeups
 * even if the application UI has been swiped from Recents.
 */
class ConnectoCallPushReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ConnectoCallPushRcvr"
        const val ACTION_INCOMING_CALL = "com.example.connecto.ACTION_INCOMING_CALL"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        Log.d(TAG, "onReceive triggered with action: $action")

        val roomId = intent.getStringExtra("room_id") ?: intent.getStringExtra("call_id") ?: intent.getStringExtra("reference_id") ?: ""
        val callerUser = intent.getStringExtra("caller_username") ?: intent.getStringExtra("caller") ?: intent.getStringExtra("caller_id") ?: "Caller"
        val callerDisplayName = intent.getStringExtra("caller_name") ?: intent.getStringExtra("sender_name") ?: callerUser
        val callerAvatar = intent.getStringExtra("caller_avatar") ?: intent.getStringExtra("avatar")

        if (roomId.isNotBlank()) {
            Log.i(TAG, "Processing incoming call via receiver: room=$roomId from $callerDisplayName")
            val appCtx = context.applicationContext

            try {
                val pm = appCtx.getSystemService(Context.POWER_SERVICE) as? PowerManager
                @Suppress("DEPRECATION")
                val wakeLock = pm?.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "connecto:call_receiver_wake"
                )
                wakeLock?.acquire(30000L)
            } catch (e: Exception) {
                Log.w(TAG, "WakeLock acquire failed: ${e.message}")
            }

            ConnectoNotificationManager.init(appCtx)
            VoiceCallManager.init(appCtx)
            VoiceCallManager.triggerIncomingCallFromPushOrBackground(
                roomId = roomId,
                callerId = callerUser,
                callerUsername = callerUser,
                callerName = callerDisplayName,
                callerAvatar = callerAvatar
            )

            try {
                val fullScreenIntent = Intent(appCtx, IncomingCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(ConnectoNotificationManager.EXTRA_ROOM_ID, roomId)
                    putExtra(ConnectoNotificationManager.EXTRA_CALLER_NAME, callerDisplayName)
                    putExtra("extra_caller_username", callerUser)
                    putExtra("extra_caller_avatar", callerAvatar)
                }
                appCtx.startActivity(fullScreenIntent)
            } catch (e: Exception) {
                Log.d(TAG, "Direct activity start from receiver bypassed: ${e.message}")
            }
        }
    }
}
