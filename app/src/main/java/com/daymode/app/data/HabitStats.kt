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

data class HabitStreakStat(
    val habit: Habit,
    val doneLast7: Int,
    val totalDays: Int = 7,
) {
    val percent: Int
        get() = if (totalDays == 0) 0 else ((doneLast7 * 100f) / totalDays).toInt()
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
    if (habits.isEmpty()) {
        return (0..6).map { offset ->
            DayHabitStat(date = endDate.minusDays((6 - offset).toLong()), done = 0, total = 0)
        }
    }
    return (0..6).map { offset ->
        val date = endDate.minusDays((6 - offset).toLong())
        val done = completedIdsForDate(completions, date).count { id ->
            habits.any { it.id == id }
        }
        DayHabitStat(date = date, done = done, total = habits.size)
    }
}

fun perHabitStatsLast7(
    habits: List<Habit>,
    completions: Set<String>,
    endDate: LocalDate = LocalDate.now(),
): List<HabitStreakStat> {
    return habits.map { habit ->
        val done = (0..6).count { offset ->
            val date = endDate.minusDays(offset.toLong())
            HabitsRepository.completionKey(habit.id, date) in completions
        }
        HabitStreakStat(habit = habit, doneLast7 = done)
    }
}
