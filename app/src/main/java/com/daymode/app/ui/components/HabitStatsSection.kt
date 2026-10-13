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
import com.daymode.app.data.HabitStreakStat
import com.daymode.app.data.completedIdsForDate
import com.daymode.app.data.perHabitStatsLast7
import com.daymode.app.data.weekHabitStats
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun SelectedDayHabitSummary(
    habits: List<Habit>,
    completions: Set<String>,
    selectedDate: LocalDate,
) {
    if (habits.isEmpty()) return
    val done = completedIdsForDate(completions, selectedDate).count { id ->
        habits.any { it.id == id }
    }
    val total = habits.size
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Привычки за день", style = MaterialTheme.typography.titleSmall)
            Text(
                "$done из $total",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                if (total == 0) "Добавь привычки на Утро или Вечер"
                else "${((done * 100f) / total).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
fun HabitStatsSection(
    habits: List<Habit>,
    completions: Set<String>,
    endDate: LocalDate = LocalDate.now(),
) {
    val week = weekHabitStats(habits, completions, endDate)
    val perHabit = perHabitStatsLast7(habits, completions, endDate)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Статистика привычек", style = MaterialTheme.typography.titleMedium)
        Text(
            "Последние 7 дней",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        if (habits.isEmpty()) {
            Text("Пока нет привычек — статистика появится после добавления.")
            return
        }

        WeekBars(week)

        Spacer(Modifier.height(4.dp))
        Text("По привычкам", style = MaterialTheme.typography.titleSmall)
        perHabit.forEach { stat ->
            HabitProgressRow(stat)
        }
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(ratio.coerceIn(0f, 1f).let { if (it == 0f && day.total > 0) 0.04f else it })
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
private fun HabitProgressRow(stat: HabitStreakStat) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stat.habit.title, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${stat.doneLast7}/7 · ${stat.percent}%",
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
                    .fillMaxWidth(stat.percent / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
