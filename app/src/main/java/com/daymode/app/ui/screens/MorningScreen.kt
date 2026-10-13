package com.daymode.app.ui.screens

import androidx.compose.runtime.Composable
import com.daymode.app.data.Habit
import com.daymode.app.data.HabitPeriod
import java.time.LocalDate

@Composable
fun MorningScreen(
    habits: List<Habit>,
    completions: Set<String>,
    selectedDate: LocalDate,
    completedIdsForSelectedDay: Set<String>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    onAdd: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    HabitsScreen(
        period = HabitPeriod.MORNING,
        habits = habits,
        completions = completions,
        selectedDate = selectedDate,
        completedIdsForSelectedDay = completedIdsForSelectedDay,
        onPreviousDay = onPreviousDay,
        onNextDay = onNextDay,
        onResetToToday = onResetToToday,
        onAdd = onAdd,
        onToggle = onToggle,
        onDelete = onDelete,
    )
}
