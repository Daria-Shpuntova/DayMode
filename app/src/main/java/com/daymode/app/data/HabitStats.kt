package com.daymode.app.data

import java.time.LocalDate

data class DayHabitStat(
    val date: LocalDate,
    val done: Int,
    val total: Int,
) {
    val percent: Int
        get() = if (total == 0) 0 else ((done * 100f) / total).toInt()
}

data class HabitPeriodStat(
    val habit: Habit,
    val done: Int,
    val totalDays: Int,
) {
    val percent: Int
        get() = if (totalDays == 0) 0 else ((done * 100f) / totalDays).toInt()
}

fun completedIdsForDate(completions: Set<String>, date: LocalDate): Set<String> {
    val suffix = ":$date"
    return completions
        .filter { it.endsWith(suffix) }
        .map { it.substringBefore(":") }
        .toSet()
}

fun weekHabitStats(
    habits: List<Habit>,
    completions: Set<String>,
    endDate: LocalDate = LocalDate.now(),
): List<DayHabitStat> {
    return (0..6).map { offset ->
        val date = endDate.minusDays((6 - offset).toLong())
        val done = completedIdsForDate(completions, date).count { id ->
            habits.any { it.id == id }
        }
        DayHabitStat(date = date, done = done, total = habits.size)
    }
}

fun perHabitStatsForDays(
    habits: List<Habit>,
    completions: Set<String>,
    days: Int,
    endDate: LocalDate = LocalDate.now(),
): List<HabitPeriodStat> {
    val range = (0 until days).toList()
    return habits.map { habit ->
        val done = range.count { offset ->
            val date = endDate.minusDays(offset.toLong())
            HabitsRepository.completionKey(habit.id, date) in completions
        }
        HabitPeriodStat(habit = habit, done = done, totalDays = days)
    }
}
