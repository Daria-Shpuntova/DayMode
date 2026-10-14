package com.daymode.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daymode.app.data.LocalDateAnchor
import com.daymode.app.data.WORK_SCHEDULE_PRESETS
import com.daymode.app.data.WorkScheduleKind
import com.daymode.app.data.isWorkDay
import com.daymode.app.data.scheduleLabel
import java.time.LocalDate

private val dayLabels = listOf(
    1 to "Пн",
    2 to "Вт",
    3 to "Ср",
    4 to "Чт",
    5 to "Пт",
    6 to "Сб",
    7 to "Вс",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkSchedulePicker(
    kind: WorkScheduleKind,
    workDays: Set<Int>,
    rotateWorkDays: Int,
    rotateRestDays: Int,
    rotateAnchorEpochDay: Long,
    onKindChange: (WorkScheduleKind) -> Unit,
    onWorkDaysChange: (Set<Int>) -> Unit,
    onRotateWorkChange: (Int) -> Unit,
    onRotateRestChange: (Int) -> Unit,
    onAnchorChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val previewSettings = com.daymode.app.data.UserSettings(
        workScheduleKind = kind,
        workDays = workDays,
        rotateWorkDays = rotateWorkDays,
        rotateRestDays = rotateRestDays,
        rotateAnchorEpochDay = rotateAnchorEpochDay,
    )
    val today = LocalDate.now()
    val todayIsWork = previewSettings.isWorkDay(today)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("График", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WORK_SCHEDULE_PRESETS.forEach { preset ->
                val selected = when (preset.kind) {
                    WorkScheduleKind.FIVE_TWO -> kind == WorkScheduleKind.FIVE_TWO
                    WorkScheduleKind.CUSTOM_WEEK -> kind == WorkScheduleKind.CUSTOM_WEEK
                    WorkScheduleKind.ROTATING ->
                        kind == WorkScheduleKind.ROTATING &&
                            rotateWorkDays == preset.workDays &&
                            rotateRestDays == preset.restDays
                }
                FilterChip(
                    selected = selected,
                    onClick = {
                        when (preset.kind) {
                            WorkScheduleKind.FIVE_TWO -> {
                                onKindChange(WorkScheduleKind.FIVE_TWO)
                                onWorkDaysChange(setOf(1, 2, 3, 4, 5))
                            }
                            WorkScheduleKind.CUSTOM_WEEK -> {
                                onKindChange(WorkScheduleKind.CUSTOM_WEEK)
                            }
                            WorkScheduleKind.ROTATING -> {
                                onKindChange(WorkScheduleKind.ROTATING)
                                onRotateWorkChange(preset.workDays)
                                onRotateRestChange(preset.restDays)
                                onAnchorChange(LocalDateAnchor.todayEpochDay())
                            }
                        }
                    },
                    label = { Text(preset.label) },
                )
            }
        }

        when (kind) {
            WorkScheduleKind.CUSTOM_WEEK -> {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dayLabels.forEach { (day, label) ->
                        FilterChip(
                            selected = day in workDays,
                            onClick = {
                                onWorkDaysChange(
                                    if (day in workDays) workDays - day else workDays + day,
                                )
                            },
                            label = { Text(label) },
                        )
                    }
                }
            }
            WorkScheduleKind.ROTATING -> {
                Text(
                    "Цикл ${previewSettings.scheduleLabel()}: $rotateWorkDays раб. / $rotateRestDays вых.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
                Text("Рабочих дней подряд", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { n ->
                        FilterChip(
                            selected = rotateWorkDays == n,
                            onClick = { onRotateWorkChange(n) },
                            label = { Text("$n") },
                        )
                    }
                }
                Text("Выходных подряд", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { n ->
                        FilterChip(
                            selected = rotateRestDays == n,
                            onClick = { onRotateRestChange(n) },
                            label = { Text("$n") },
                        )
                    }
                }
                Text(
                    if (todayIsWork) "Сегодня по графику — рабочий день"
                    else "Сегодня по графику — выходной",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(onClick = { onAnchorChange(LocalDateAnchor.todayEpochDay()) }) {
                    Text("Считать сегодня первым рабочим")
                }
            }
            WorkScheduleKind.FIVE_TWO -> {
                Text(
                    "Пн–Пт рабочие, Сб–Вс выходные",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }
        }
    }
}
