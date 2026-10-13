package com.daymode.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.daymode.app.data.DayHabitStat
import com.daymode.app.data.Habit
import com.daymode.app.data.HabitPeriod
import com.daymode.app.data.HabitPeriodStat
import com.daymode.app.data.completedIdsForDate
import com.daymode.app.data.perHabitStatsForDays
import com.daymode.app.data.weekHabitStats
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SelectedDayHabitSummary(
    habits: List<Habit>,
    completions: Set<String>,
    selectedDate: LocalDate,
    title: String = "За выбранный день",
) {
    val periodHabits = habits
    if (periodHabits.isEmpty()) return
    val done = completedIdsForDate(completions, selectedDate).count { id ->
        periodHabits.any { it.id == id }
    }
    val total = periodHabits.size
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                "$done из $total",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "${((done * 100f) / total).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
fun HabitStatsSection(
    period: HabitPeriod,
    habits: List<Habit>,
    completions: Set<String>,
    selectedDate: LocalDate,
) {
    val periodHabits = habits.filter { it.period == period }
    val week = weekHabitStats(periodHabits, completions, selectedDate)
    val weekPerHabit = perHabitStatsForDays(periodHabits, completions, days = 7, endDate = selectedDate)
    val monthPerHabit = perHabitStatsForDays(periodHabits, completions, days = 30, endDate = selectedDate)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Статистика · ${period.label}", style = MaterialTheme.typography.titleMedium)

        SelectedDayHabitSummary(
            habits = periodHabits,
            completions = completions,
            selectedDate = selectedDate,
            title = "За выбранный день",
        )

        if (periodHabits.isEmpty()) {
            Text("Добавь привычки — здесь появится статистика за неделю и месяц.")
            return
        }

        Text(
            "Последние 7 дней",
            style = MaterialTheme.typography.titleSmall,
        )
        WeekBars(week)

        Text("За неделю по привычкам", style = MaterialTheme.typography.titleSmall)
        weekPerHabit.forEach { HabitProgressRow(it, suffix = "/7") }

        Text("За месяц по привычкам", style = MaterialTheme.typography.titleSmall)
        Text(
            "Последние 30 дней",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        monthPerHabit.forEach { HabitProgressRow(it, suffix = "/30") }
    }
}

@Composable
private fun WeekBars(days: List<DayHabitStat>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            val ratio = if (day.total == 0) 0f else day.done.toFloat() / day.total
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "${day.done}/${day.total}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    )
                    val barRatio = ratio.coerceIn(0f, 1f).let {
                        if (it == 0f && day.total > 0) 0.04f else it
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(barRatio)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru")),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun HabitProgressRow(stat: HabitPeriodStat, suffix: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stat.habit.title, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${stat.done}$suffix · ${stat.percent}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((stat.percent / 100f).coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
