package com.daymode.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.daymode.app.data.DayBlock
import com.daymode.app.data.UserSettings
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {

    private const val PREFS = "daymode_alarms"
    private const val KEY_CODES = "request_codes"
    const val PRE_MINUTES = 15

    fun reschedule(context: Context, settings: UserSettings, blocks: List<DayBlock>) {
        NotificationHelper.ensureChannel(context)
        cancelAll(context)

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val codes = mutableSetOf<Int>()

        EventPlanner.eventsForDays(settings, blocks).forEach { event ->
            val eventMillis = LocalDateTime.of(
                event.date,
                LocalTime.of(event.minutesFromMidnight / 60, event.minutesFromMidnight % 60),
            ).atZone(zone).toInstant().toEpochMilli()

            val preMillis = eventMillis - PRE_MINUTES * 60_000L

            if (preMillis > now) {
                val code = stableCode(event.preKey)
                codes += code
                schedule(
                    context = context,
                    alarmManager = alarmManager,
                    triggerAt = preMillis,
                    requestCode = code,
                    title = event.preTitle(),
                    text = event.preText(),
                    notificationId = code,
                )
            }

            if (eventMillis > now) {
                val code = stableCode(event.atKey)
                codes += code
                schedule(
                    context = context,
                    alarmManager = alarmManager,
                    triggerAt = eventMillis,
                    requestCode = code,
                    title = event.atTitle(),
                    text = event.atText(),
                    notificationId = code,
                )
            }
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_CODES, codes.map { it.toString() }.toSet())
            .apply()
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val codes = prefs.getStringSet(KEY_CODES, emptySet()).orEmpty()
        codes.forEach { raw ->
            val code = raw.toIntOrNull() ?: return@forEach
            alarmManager.cancel(pendingIntent(context, code, "", "", code))
        }
        prefs.edit().remove(KEY_CODES).apply()
    }

    private fun schedule(
        context: Context,
        alarmManager: AlarmManager,
        triggerAt: Long,
        requestCode: Int,
        title: String,
        text: String,
        notificationId: Int,
    ) {
        val pi = pendingIntent(context, requestCode, title, text, notificationId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun pendingIntent(
        context: Context,
        requestCode: Int,
        title: String,
        text: String,
        notificationId: Int,
    ): PendingIntent {
        val intent = Intent(context, EventAlarmReceiver::class.java).apply {
            action = EventAlarmReceiver.ACTION_EVENT
            putExtra(EventAlarmReceiver.EXTRA_TITLE, title)
            putExtra(EventAlarmReceiver.EXTRA_TEXT, text)
            putExtra(EventAlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun stableCode(key: String): Int {
        return (key.hashCode() and Int.MAX_VALUE).let { if (it == 0) 1 else it }
    }
}
