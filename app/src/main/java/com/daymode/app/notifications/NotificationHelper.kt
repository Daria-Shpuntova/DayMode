package com.daymode.app.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daymode.app.MainActivity
import com.daymode.app.R

object NotificationHelper {
    const val CHANNEL_ID = "daymode_events"
    const val POMODORO_CHANNEL_ID = "daymode_pomodoro"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания DayMode",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Напоминания про работу, отдых и сон"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    fun ensurePomodoroChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            POMODORO_CHANNEL_ID,
            "Таймер фокуса",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Помидор на экране блокировки"
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    fun show(
        context: Context,
        notificationId: Int,
        title: String,
        text: String,
        highPriority: Boolean = false,
    ) {
        ensureChannel(context)
        if (highPriority) ensurePomodoroChannel(context)
        val channel = if (highPriority) POMODORO_CHANNEL_ID else CHANNEL_ID
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(
                if (highPriority) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT,
            )
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
