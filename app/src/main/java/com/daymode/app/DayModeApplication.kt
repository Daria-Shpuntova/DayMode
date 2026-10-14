package com.daymode.app

import android.app.Application
import com.daymode.app.notifications.NotificationHelper

class DayModeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        NotificationHelper.ensurePomodoroChannel(this)
    }
}
