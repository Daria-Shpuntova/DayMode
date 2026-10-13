package com.daymode.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daymode.app.data.Habit
import com.daymode.app.data.HabitPeriod
import com.daymode.app.data.HabitsRepository
import com.daymode.app.ui.components.DateSwitcher
import java.time.LocalDate

@Composable
fun HabitsScreen(
    period: HabitPeriod,
    habits: List<Habit>,
    selectedDate: LocalDate,
    completedIdsForSelectedDay: Set<String>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    headerExtra: (@Composable () -> Unit)? = null,
    onAdd: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val periodHabits = habits.filter { it.period == period }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DateSwitcher(
            selectedDate = selectedDate,
            onPrevious = onPreviousDay,
            onNext = onNextDay,
            onResetToToday = onResetToToday,
        )
        Text(period.label, style = MaterialTheme.typography.headlineMedium)
        headerExtra?.invoke()
        Text(
            "Отметки относятся к выбранному дню сверху.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        if (periodHabits.isEmpty()) {
            Text(
                "Пока пусто — добавь первую привычку.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        periodHabits.forEach { habit ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = habit.id in completedIdsForSelectedDay,
                    onCheckedChange = { onToggle(habit.id) },
                )
                Text(
                    habit.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onDelete(habit.id) }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        OutlinedButton(
            onClick = {
                newTitle = ""
                showAddDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Добавить привычку")
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Новая привычка") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = newTitle.trim()
                        if (title.isNotEmpty()) {
                            onAdd(title)
                            showAddDialog = false
                        }
                    },
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена")
                }
            },
        )
    }
}

fun createHabit(period: HabitPeriod, title: String, existing: List<Habit>): Habit {
    val order = existing.filter { it.period == period }.maxOfOrNull { it.order }?.plus(1) ?: 0
    return Habit(
        id = HabitsRepository.newId(),
        title = title,
        period = period,
        order = order,
    )
}
