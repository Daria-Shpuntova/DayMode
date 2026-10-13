package com.daymode.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daymode_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val wake = intPreferencesKey("wake_minutes")
        val workStart = intPreferencesKey("work_start_minutes")
        val isFiveTwo = booleanPreferencesKey("is_five_two")
        val workDays = stringPreferencesKey("work_days")
        val bedtime = intPreferencesKey("bedtime_minutes")
        val windDown = intPreferencesKey("wind_down_before_bed")
        val themeMode = stringPreferencesKey("theme_mode")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val daysRaw = prefs[Keys.workDays]
        val days = daysRaw
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: setOf(1, 2, 3, 4, 5)

        UserSettings(
            onboardingDone = prefs[Keys.onboardingDone] ?: false,
            wakeMinutes = prefs[Keys.wake] ?: (7 * 60 + 30),
            workStartMinutes = prefs[Keys.workStart] ?: (10 * 60),
            isFiveTwo = prefs[Keys.isFiveTwo] ?: true,
            workDays = if (prefs[Keys.isFiveTwo] != false) setOf(1, 2, 3, 4, 5) else days,
            bedtimeMinutes = prefs[Keys.bedtime] ?: (23 * 60),
            windDownBeforeBedMinutes = prefs[Keys.windDown] ?: 60,
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[Keys.themeMode] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
        )
    }

    suspend fun save(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.onboardingDone] = settings.onboardingDone
            prefs[Keys.wake] = settings.wakeMinutes
            prefs[Keys.workStart] = settings.workStartMinutes
            prefs[Keys.isFiveTwo] = settings.isFiveTwo
            prefs[Keys.workDays] = settings.workDays.sorted().joinToString(",")
            prefs[Keys.bedtime] = settings.bedtimeMinutes
            prefs[Keys.windDown] = settings.windDownBeforeBedMinutes
            prefs[Keys.themeMode] = settings.themeMode.name
        }
    }
}
