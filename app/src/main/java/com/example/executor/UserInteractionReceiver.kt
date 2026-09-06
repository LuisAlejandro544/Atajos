package com.example.executor

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class UserInteractionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_REPLY = "com.example.ACTION_USER_REPLY"
        const val ACTION_CANCEL = "com.example.ACTION_USER_CANCEL"
        const val EXTRA_INTERACTION_ID = "interaction_id"
        const val EXTRA_EXPECTED_KEYWORD = "expected_keyword"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val interactionId = intent.getStringExtra(EXTRA_INTERACTION_ID) ?: return
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 9001)

        NotificationManagerCompat.from(context).cancel(notifId)

        when (intent.action) {
            ACTION_REPLY -> {
                val results = RemoteInput.getResultsFromIntent(intent)
                val replyText = results?.getCharSequence(KEY_TEXT_REPLY)?.toString() ?: ""
                val expected = intent.getStringExtra(EXTRA_EXPECTED_KEYWORD) ?: ""
                val matches = replyText.trim().equals(expected.trim(), ignoreCase = true)
                UserInteractionBridge.resolve(interactionId, matches)
            }
            ACTION_CANCEL -> {
                UserInteractionBridge.resolve(interactionId, false)
            }
        }
    }
}
