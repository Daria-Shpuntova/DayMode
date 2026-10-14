package com.daymode.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daymode_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val wake = intPreferencesKey("wake_minutes")
        val restWake = intPreferencesKey("rest_wake_minutes")
        val workStart = intPreferencesKey("work_start_minutes")
        val workEnd = intPreferencesKey("work_end_minutes")
        val isFiveTwo = booleanPreferencesKey("is_five_two")
        val workScheduleKind = stringPreferencesKey("work_schedule_kind")
        val workDays = stringPreferencesKey("work_days")
        val rotateWork = intPreferencesKey("rotate_work_days")
        val rotateRest = intPreferencesKey("rotate_rest_days")
        val rotateAnchor = longPreferencesKey("rotate_anchor_epoch_day")
        val bedtime = intPreferencesKey("bedtime_minutes")
        val restBedtime = intPreferencesKey("rest_bedtime_minutes")
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
        val workStart = prefs[Keys.workStart] ?: (10 * 60)
        val workEnd = prefs[Keys.workEnd] ?: (workStart + 4 * 60).coerceAtMost(23 * 60 + 59)
        val wake = prefs[Keys.wake] ?: (7 * 60 + 30)
        val bedtime = prefs[Keys.bedtime] ?: (23 * 60)

        val kind = when (prefs[Keys.workScheduleKind]) {
            WorkScheduleKind.ROTATING.name -> WorkScheduleKind.ROTATING
            WorkScheduleKind.CUSTOM_WEEK.name -> WorkScheduleKind.CUSTOM_WEEK
            WorkScheduleKind.FIVE_TWO.name -> WorkScheduleKind.FIVE_TWO
            else -> {
                if (prefs[Keys.isFiveTwo] == false) WorkScheduleKind.CUSTOM_WEEK
                else WorkScheduleKind.FIVE_TWO
            }
        }

        UserSettings(
            onboardingDone = prefs[Keys.onboardingDone] ?: false,
            wakeMinutes = wake,
            restWakeMinutes = prefs[Keys.restWake] ?: (9 * 60).coerceAtLeast(wake),
            workStartMinutes = workStart,
            workEndMinutes = workEnd,
            workScheduleKind = kind,
            workDays = when (kind) {
                WorkScheduleKind.FIVE_TWO -> setOf(1, 2, 3, 4, 5)
                else -> days
            },
            rotateWorkDays = prefs[Keys.rotateWork] ?: 2,
            rotateRestDays = prefs[Keys.rotateRest] ?: 2,
            rotateAnchorEpochDay = prefs[Keys.rotateAnchor] ?: LocalDateAnchor.todayEpochDay(),
            bedtimeMinutes = bedtime,
            restBedtimeMinutes = prefs[Keys.restBedtime] ?: 0,
            windDownBeforeBedMinutes = prefs[Keys.windDown] ?: 60,
            themeMode = when (prefs[Keys.themeMode]) {
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.LIGHT
            },
        )
    }

    suspend fun save(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.onboardingDone] = settings.onboardingDone
            prefs[Keys.wake] = settings.wakeMinutes
            prefs[Keys.restWake] = settings.restWakeMinutes
            prefs[Keys.workStart] = settings.workStartMinutes
            prefs[Keys.workEnd] = settings.workEndMinutes
            prefs[Keys.workScheduleKind] = settings.workScheduleKind.name
            prefs[Keys.isFiveTwo] = settings.workScheduleKind == WorkScheduleKind.FIVE_TWO
            prefs[Keys.workDays] = settings.workDays.sorted().joinToString(",")
            prefs[Keys.rotateWork] = settings.rotateWorkDays
            prefs[Keys.rotateRest] = settings.rotateRestDays
            prefs[Keys.rotateAnchor] = settings.rotateAnchorEpochDay
            prefs[Keys.bedtime] = settings.bedtimeMinutes
            prefs[Keys.restBedtime] = settings.restBedtimeMinutes
            prefs[Keys.windDown] = settings.windDownBeforeBedMinutes
            prefs[Keys.themeMode] = settings.themeMode.name
        }
    }
}
