package com.daymode.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.daymode.app.data.UserSettings
import com.daymode.app.data.toClockLabel
import com.daymode.app.ui.components.TimePickerField

enum class AnchorKind {
    WAKE,
    WORK,
    WIND_DOWN,
    BEDTIME,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditAnchorSheet(
    kind: AnchorKind,
    settings: UserSettings,
    onDismiss: () -> Unit,
    onSave: (UserSettings) -> Unit,
) {
    var wake by remember { mutableIntStateOf(settings.wakeMinutes) }
    var work by remember { mutableIntStateOf(settings.workStartMinutes) }
    var bed by remember { mutableIntStateOf(settings.bedtimeMinutes) }
    var windDown by remember { mutableIntStateOf(settings.windDownBeforeBedMinutes) }
    var isFiveTwo by remember { mutableStateOf(settings.isFiveTwo) }
    var workDays by remember { mutableStateOf(settings.workDays) }
    var error by remember { mutableStateOf<String?>(null) }
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.85f).dp

    val title = when (kind) {
        AnchorKind.WAKE -> "Подъём"
        AnchorKind.WORK -> "Работа / учёба"
        AnchorKind.WIND_DOWN -> "Окно без телефона"
        AnchorKind.BEDTIME -> "Отбой"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Нажми на время, чтобы открыть циферблат",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )

                when (kind) {
                    AnchorKind.WAKE -> TimePickerField(
                        label = "Подъём",
                        minutes = wake,
                        onMinutesChange = { wake = it },
                    )

                    AnchorKind.WORK -> {
                        TimePickerField(
                            label = "Начало",
                            minutes = work,
                            onMinutesChange = { work = it },
                        )
                        Text("График", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isFiveTwo,
                                onClick = {
                                    isFiveTwo = true
                                    workDays = setOf(1, 2, 3, 4, 5)
                                },
                                label = { Text("5/2") },
                            )
                            FilterChip(
                                selected = !isFiveTwo,
                                onClick = { isFiveTwo = false },
                                label = { Text("Свои дни") },
                            )
                        }
                        if (!isFiveTwo) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт",
                                    5 to "Пт", 6 to "Сб", 7 to "Вс",
                                ).forEach { (day, label) ->
                                    FilterChip(
                                        selected = day in workDays,
                                        onClick = {
                                            workDays =
                                                if (day in workDays) workDays - day else workDays + day
                                        },
                                        label = { Text(label) },
                                    )
                                }
                            }
                        }
                    }

                    AnchorKind.WIND_DOWN -> {
                        Text("За сколько до отбоя", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 45, 60, 90).forEach { minutes ->
                                FilterChip(
                                    selected = windDown == minutes,
                                    onClick = { windDown = minutes },
                                    label = { Text("$minutes мин") },
                                )
                            }
                        }
                        Text(
                            "Сейчас окно начнётся в ${(
                                (bed - windDown + 24 * 60) % (24 * 60)
                                ).toClockLabel()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    AnchorKind.BEDTIME -> TimePickerField(
                        label = "Отбой",
                        minutes = bed,
                        onMinutesChange = { bed = it },
                    )
                }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = {
                        if (kind == AnchorKind.WORK && !isFiveTwo && workDays.isEmpty()) {
                            error = "Выбери хотя бы один день"
                            return@Button
                        }
                        onSave(
                            settings.copy(
                                wakeMinutes = wake,
                                workStartMinutes = work,
                                bedtimeMinutes = bed,
                                windDownBeforeBedMinutes = windDown,
                                isFiveTwo = isFiveTwo,
                                workDays = if (isFiveTwo) setOf(1, 2, 3, 4, 5) else workDays,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}