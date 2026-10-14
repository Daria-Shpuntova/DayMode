package com.daymode.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.daymode.app.data.BlocksRepository
import com.daymode.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(context).settings.first()
                val blocks = BlocksRepository(context).blocks.first()
                if (settings.onboardingDone) {
                    NotificationScheduler.reschedule(context, settings, blocks)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
