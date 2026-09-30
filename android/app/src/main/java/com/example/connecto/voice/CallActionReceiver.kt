package com.example.connecto.voice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.connecto.MainActivity
import com.example.connecto.notifications.ConnectoNotificationManager

class CallActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallActionReceiver"
        const val ACTION_END_CALL = "com.example.connecto.ACTION_END_CALL"
        const val ACTION_TOGGLE_MUTE = "com.example.connecto.ACTION_TOGGLE_MUTE"
        const val ACTION_DECLINE_CALL = "com.example.connecto.ACTION_DECLINE_CALL"
        const val ACTION_ANSWER_CALL = "com.example.connecto.ACTION_ANSWER_CALL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive action: $action")

        when (action) {
            ACTION_END_CALL -> {
                try {
                    VoiceCallManager.endCall()
                    VoiceCallManager.leaveVoiceRoom()
                    VoiceCallForegroundService.stopService(context)
                } catch (t: Throwable) {
                    Log.e(TAG, "Error ending call from notification: ${t.message}", t)
                }
            }

            ACTION_TOGGLE_MUTE -> {
                try {
                    VoiceCallManager.toggleMute()
                    VoiceCallForegroundService.updateMute(context, VoiceCallManager.isMuted.value)
                } catch (t: Throwable) {
                    Log.e(TAG, "Error toggling mute from notification: ${t.message}", t)
                }
            }

            ACTION_DECLINE_CALL -> {
                try {
                    VoiceCallManager.declineIncomingCall()
                    ConnectoNotificationManager.cancelCallNotification()
                } catch (t: Throwable) {
                    Log.e(TAG, "Error declining call from notification: ${t.message}", t)
                }
            }

            ACTION_ANSWER_CALL -> {
                try {
                    VoiceCallManager.acceptIncomingCall()
                    ConnectoNotificationManager.cancelCallNotification()

                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(ConnectoNotificationManager.EXTRA_NOTIFICATION_ACTION, ConnectoNotificationManager.ACTION_ANSWER_CALL)
                    }
                    context.startActivity(launchIntent)
                } catch (t: Throwable) {
                    Log.e(TAG, "Error answering call from notification: ${t.message}", t)
                }
            }
        }
    }
}
