package com.daymode.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EventAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "DayMode" }
        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, title.hashCode())
        NotificationHelper.show(context, notificationId, title, text)
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val ACTION_EVENT = "com.daymode.app.ACTION_EVENT_ALARM"
    }
}
