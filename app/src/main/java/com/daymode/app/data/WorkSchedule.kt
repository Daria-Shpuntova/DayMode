package com.daymode.app.data

import java.time.LocalDate

enum class WorkScheduleKind {
    /** Пн–Пт */
    FIVE_TWO,
    /** N рабочих / M выходных по кругу */
    ROTATING,
    /** Произвольные дни недели */
    CUSTOM_WEEK,
}

data class WorkSchedulePreset(
    val label: String,
    val kind: WorkScheduleKind,
    val workDays: Int = 0,
    val restDays: Int = 0,
)

val WORK_SCHEDULE_PRESETS = listOf(
    WorkSchedulePreset("5/2", WorkScheduleKind.FIVE_TWO),
    WorkSchedulePreset("2/2", WorkScheduleKind.ROTATING, workDays = 2, restDays = 2),
    WorkSchedulePreset("3/3", WorkScheduleKind.ROTATING, workDays = 3, restDays = 3),
    WorkSchedulePreset("1/1", WorkScheduleKind.ROTATING, workDays = 1, restDays = 1),
    WorkSchedulePreset("4/2", WorkScheduleKind.ROTATING, workDays = 4, restDays = 2),
    WorkSchedulePreset("3/1", WorkScheduleKind.ROTATING, workDays = 3, restDays = 1),
    WorkSchedulePreset("Свои дни", WorkScheduleKind.CUSTOM_WEEK),
)

fun UserSettings.isWorkDay(date: LocalDate): Boolean = when (workScheduleKind) {
    WorkScheduleKind.FIVE_TWO -> date.dayOfWeek.value in 1..5
    WorkScheduleKind.CUSTOM_WEEK -> date.dayOfWeek.value in workDays
    WorkScheduleKind.ROTATING -> {
        val work = rotateWorkDays.coerceAtLeast(1)
        val rest = rotateRestDays.coerceAtLeast(0)
        val cycle = (work + rest).coerceAtLeast(1)
        val diff = date.toEpochDay() - rotateAnchorEpochDay
        val offset = ((diff % cycle) + cycle) % cycle
        offset < work
    }
}

fun UserSettings.scheduleLabel(): String = when (workScheduleKind) {
    WorkScheduleKind.FIVE_TWO -> "5/2"
    WorkScheduleKind.CUSTOM_WEEK -> "свои дни"
    WorkScheduleKind.ROTATING -> "$rotateWorkDays/$rotateRestDays"
}
