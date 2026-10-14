package com.daymode.app.data

/**
 * Core day anchors collected during onboarding.
 * Times are minutes from midnight (0..1439).
 */
data class UserSettings(
    val onboardingDone: Boolean = false,
    val wakeMinutes: Int = 7 * 60 + 30,
    val workStartMinutes: Int = 10 * 60,
    val workEndMinutes: Int = 14 * 60,
    /** true = Mon–Fri; false = custom [workDays] */
    val isFiveTwo: Boolean = true,
    /** ISO day numbers 1=Mon .. 7=Sun */
    val workDays: Set<Int> = setOf(1, 2, 3, 4, 5),
    val bedtimeMinutes: Int = 23 * 60,
    /** Minutes before bedtime for "phone-free / soon sleep" window start */
    val windDownBeforeBedMinutes: Int = 60,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
)

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
