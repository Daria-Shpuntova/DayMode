package com.daymode.app.data

import java.time.LocalDate

/**
 * Core day anchors collected during onboarding.
 * Times are minutes from midnight (0..1439).
 *
 * [wakeMinutes] / [bedtimeMinutes] — рабочие дни по графику.
 * [restWakeMinutes] / [restBedtimeMinutes] — выходные / дни отдыха.
 */
data class UserSettings(
    val onboardingDone: Boolean = false,
    val wakeMinutes: Int = 7 * 60 + 30,
    val restWakeMinutes: Int = 9 * 60,
    val workStartMinutes: Int = 10 * 60,
    val workEndMinutes: Int = 14 * 60,
    val workScheduleKind: WorkScheduleKind = WorkScheduleKind.FIVE_TWO,
    /** ISO day numbers 1=Mon .. 7=Sun — used for [WorkScheduleKind.CUSTOM_WEEK] */
    val workDays: Set<Int> = setOf(1, 2, 3, 4, 5),
    /** Working days in a rotating cycle */
    val rotateWorkDays: Int = 2,
    /** Rest days in a rotating cycle */
    val rotateRestDays: Int = 2,
    /** Epoch day when the rotating cycle starts (first work day) */
    val rotateAnchorEpochDay: Long = LocalDateAnchor.todayEpochDay(),
    val bedtimeMinutes: Int = 23 * 60,
    val restBedtimeMinutes: Int = 0, // 00:00
    /** Minutes before bedtime for "phone-free / soon sleep" window start */
    val windDownBeforeBedMinutes: Int = 60,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
)

object LocalDateAnchor {
    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
}

fun UserSettings.wakeMinutesFor(date: LocalDate): Int =
    if (isWorkDay(date)) wakeMinutes else restWakeMinutes

fun UserSettings.bedtimeMinutesFor(date: LocalDate): Int =
    if (isWorkDay(date)) bedtimeMinutes else restBedtimeMinutes

fun UserSettings.windDownStartFor(date: LocalDate): Int {
    val bed = bedtimeMinutesFor(date)
    return (bed - windDownBeforeBedMinutes + 24 * 60) % (24 * 60)
}

fun Int.toClockLabel(): String {
    val h = this / 60
    val m = this % 60
    return "%02d:%02d".format(h, m)
}

fun parseClockToMinutes(text: String): Int? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}
