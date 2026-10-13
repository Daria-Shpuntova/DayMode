package com.daymode.app.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.daymode.app.data.Habit
import com.daymode.app.data.HabitPeriod
import com.daymode.app.data.UserSettings
import com.daymode.app.data.toClockLabel
import java.time.LocalDate

@Composable
fun EveningScreen(
    settings: UserSettings,
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
    val windDownStart =
        (settings.bedtimeMinutes - settings.windDownBeforeBedMinutes + 24 * 60) % (24 * 60)

    HabitsScreen(
        period = HabitPeriod.EVENING,
        habits = habits,
        completions = completions,
        selectedDate = selectedDate,
        completedIdsForSelectedDay = completedIdsForSelectedDay,
        onPreviousDay = onPreviousDay,
        onNextDay = onNextDay,
        onResetToToday = onResetToToday,
        headerExtra = {
            Text(
                "Окно без телефона: с ${windDownStart.toClockLabel()} до ${settings.bedtimeMinutes.toClockLabel()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        onAdd = onAdd,
        onToggle = onToggle,
        onDelete = onDelete,
    )
}
