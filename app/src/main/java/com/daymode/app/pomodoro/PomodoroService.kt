package com.daymode.app.pomodoro

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.daymode.app.MainActivity
import com.daymode.app.R
import com.daymode.app.notifications.NotificationHelper

class PomodoroService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOCUS -> {
                PomodoroSession.startFocus()
                beginForegroundAndSchedule()
            }
            ACTION_START_BREAK -> {
                PomodoroSession.startBreak()
                beginForegroundAndSchedule()
            }
            ACTION_TOGGLE_PAUSE -> {
                PomodoroSession.togglePause()
                val state = PomodoroSession.snapshot()
                if (state.isRunning) {
                    beginForegroundAndSchedule()
                } else {
                    cancelEndAlarm()
                    startForeground(NOTIF_ID, buildNotification(state))
                    if (state.phase == PomodoroPhase.IDLE) stopSelfSafely()
                }
            }
            ACTION_RESET -> {
                PomodoroSession.reset()
                cancelEndAlarm()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TICK_COMPLETE -> {
                handleComplete()
            }
            ACTION_REFRESH -> {
                PomodoroSession.refreshRemaining()
                val state = PomodoroSession.snapshot()
                if (state.phase == PomodoroPhase.IDLE && !state.isRunning) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    startForeground(NOTIF_ID, buildNotification(state))
                    if (state.isRunning) scheduleEndAlarm(state)
                }
            }
            else -> {
                val state = PomodoroSession.snapshot()
                if (state.phase != PomodoroPhase.IDLE || state.isRunning) {
                    beginForegroundAndSchedule()
                } else {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun beginForegroundAndSchedule() {
        val state = PomodoroSession.snapshot()
        startForeground(NOTIF_ID, buildNotification(state))
        if (state.isRunning) {
            scheduleEndAlarm(state)
        } else {
            cancelEndAlarm()
        }
    }

    private fun handleComplete() {
        val before = PomodoroSession.snapshot()
        if (!before.isRunning && before.endsAtElapsed == null) {
            // Already handled
            return
        }
        // Only complete if deadline actually passed (avoid stale alarms)
        val end = before.endsAtElapsed
        if (before.isRunning && end != null && end > SystemClock.elapsedRealtime() + 1500L) {
            scheduleEndAlarm(before)
            return
        }
        PomodoroSession.completePhase()
        val after = PomodoroSession.snapshot()
        cancelEndAlarm()
        val title: String
        val text: String
        when (before.phase) {
            PomodoroPhase.FOCUS -> {
                title = "Фокус закончился"
                text = "Отличная работа. Можно сделать короткий перерыв"
            }
            PomodoroPhase.BREAK -> {
                title = "Перерыв закончился"
                text = "Можно снова включить фокус"
            }
            PomodoroPhase.IDLE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return
            }
        }
        NotificationHelper.show(
            this,
            notificationId = COMPLETE_NOTIF_ID,
            title = title,
            text = text,
            highPriority = true,
        )
        startForeground(NOTIF_ID, buildNotification(after))
        if (after.phase == PomodoroPhase.IDLE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun scheduleEndAlarm(state: PomodoroUiState) {
        val end = state.endsAtElapsed ?: return
        val triggerAt = System.currentTimeMillis() + (end - SystemClock.elapsedRealtime()).coerceAtLeast(0)
        val am = getSystemService(AlarmManager::class.java) ?: return
        val pi = endPendingIntent()
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        }
    }

    private fun cancelEndAlarm() {
        val am = getSystemService(AlarmManager::class.java) ?: return
        am.cancel(endPendingIntent())
    }

    private fun endPendingIntent(): PendingIntent {
        val intent = Intent(this, PomodoroService::class.java).setAction(ACTION_TICK_COMPLETE)
        return PendingIntent.getService(
            this,
            REQ_END,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildNotification(state: PomodoroUiState): Notification {
        NotificationHelper.ensurePomodoroChannel(this)
        val remaining = state.remainingNow()
        val phaseLabel = when (state.phase) {
            PomodoroPhase.IDLE -> "Помидор"
            PomodoroPhase.FOCUS -> "Фокус"
            PomodoroPhase.BREAK -> "Перерыв"
        }
        val open = PendingIntent.getActivity(
            this,
            REQ_OPEN,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_FOCUS, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val pausePi = PendingIntent.getService(
            this,
            REQ_PAUSE,
            Intent(this, PomodoroService::class.java).setAction(ACTION_TOGGLE_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val resetPi = PendingIntent.getService(
            this,
            REQ_RESET,
            Intent(this, PomodoroService::class.java).setAction(ACTION_RESET),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, NotificationHelper.POMODORO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(phaseLabel)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                0,
                if (state.isRunning) "Пауза" else "Продолжить",
                pausePi,
            )
            .addAction(0, "Сброс", resetPi)

        if (state.isRunning && state.endsAtElapsed != null) {
            val whenMs = System.currentTimeMillis() + remaining * 1000L
            builder
                .setContentText("До конца")
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(whenMs)
                .setShowWhen(true)
        } else {
            val mm = remaining / 60
            val ss = remaining % 60
            builder
                .setContentText("Пауза · %02d:%02d".format(mm, ss))
                .setUsesChronometer(false)
                .setShowWhen(false)
        }

        return builder.build()
    }

    private fun stopSelfSafely() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val NOTIF_ID = 9100
        const val COMPLETE_NOTIF_ID = 9101
        const val EXTRA_OPEN_FOCUS = "open_focus"

        const val ACTION_START_FOCUS = "com.daymode.app.pomodoro.START_FOCUS"
        const val ACTION_START_BREAK = "com.daymode.app.pomodoro.START_BREAK"
        const val ACTION_TOGGLE_PAUSE = "com.daymode.app.pomodoro.TOGGLE_PAUSE"
        const val ACTION_RESET = "com.daymode.app.pomodoro.RESET"
        const val ACTION_TICK_COMPLETE = "com.daymode.app.pomodoro.TICK_COMPLETE"
        const val ACTION_REFRESH = "com.daymode.app.pomodoro.REFRESH"

        private const val REQ_END = 911
        private const val REQ_OPEN = 912
        private const val REQ_PAUSE = 913
        private const val REQ_RESET = 914

        fun startFocus(context: Context) = start(context, ACTION_START_FOCUS)
        fun startBreak(context: Context) = start(context, ACTION_START_BREAK)
        fun togglePause(context: Context) = start(context, ACTION_TOGGLE_PAUSE)
        fun reset(context: Context) = start(context, ACTION_RESET)

        private fun start(context: Context, action: String) {
            val intent = Intent(context, PomodoroService::class.java).setAction(action)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
