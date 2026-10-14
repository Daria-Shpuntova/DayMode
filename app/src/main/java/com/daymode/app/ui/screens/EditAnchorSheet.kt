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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.daymode.app.data.UserSettings
import com.daymode.app.data.WorkScheduleKind
import com.daymode.app.data.toClockLabel
import com.daymode.app.ui.components.TimePickerField
import com.daymode.app.ui.components.WorkSchedulePicker

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
    var restWake by remember { mutableIntStateOf(settings.restWakeMinutes) }
    var workStart by remember { mutableIntStateOf(settings.workStartMinutes) }
    var workEnd by remember { mutableIntStateOf(settings.workEndMinutes) }
    var bed by remember { mutableIntStateOf(settings.bedtimeMinutes) }
    var restBed by remember { mutableIntStateOf(settings.restBedtimeMinutes) }
    var windDown by remember { mutableIntStateOf(settings.windDownBeforeBedMinutes) }
    var scheduleKind by remember { mutableStateOf(settings.workScheduleKind) }
    var workDays by remember { mutableStateOf(settings.workDays) }
    var rotateWork by remember { mutableIntStateOf(settings.rotateWorkDays) }
    var rotateRest by remember { mutableIntStateOf(settings.rotateRestDays) }
    var rotateAnchor by remember { mutableLongStateOf(settings.rotateAnchorEpochDay) }
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
                    when (kind) {
                        AnchorKind.WAKE, AnchorKind.BEDTIME ->
                            "Отдельно для рабочих дней и выходных по твоему графику"
                        else -> "Нажми на время, чтобы открыть циферблат"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )

                when (kind) {
                    AnchorKind.WAKE -> {
                        TimePickerField(
                            label = "В рабочий день",
                            minutes = wake,
                            onMinutesChange = { wake = it },
                        )
                        TimePickerField(
                            label = "В выходной",
                            minutes = restWake,
                            onMinutesChange = { restWake = it },
                        )
                    }

                    AnchorKind.WORK -> {
                        TimePickerField(
                            label = "Начало",
                            minutes = workStart,
                            onMinutesChange = { workStart = it },
                        )
                        TimePickerField(
                            label = "Окончание",
                            minutes = workEnd,
                            onMinutesChange = { workEnd = it },
                        )
                        WorkSchedulePicker(
                            kind = scheduleKind,
                            workDays = workDays,
                            rotateWorkDays = rotateWork,
                            rotateRestDays = rotateRest,
                            rotateAnchorEpochDay = rotateAnchor,
                            onKindChange = { scheduleKind = it },
                            onWorkDaysChange = { workDays = it },
                            onRotateWorkChange = { rotateWork = it },
                            onRotateRestChange = { rotateRest = it },
                            onAnchorChange = { rotateAnchor = it },
                        )
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
                            "В рабочий день: с ${(
                                (bed - windDown + 24 * 60) % (24 * 60)
                                ).toClockLabel()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "В выходной: с ${(
                                (restBed - windDown + 24 * 60) % (24 * 60)
                                ).toClockLabel()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    AnchorKind.BEDTIME -> {
                        TimePickerField(
                            label = "В рабочий день",
                            minutes = bed,
                            onMinutesChange = { bed = it },
                        )
                        TimePickerField(
                            label = "В выходной",
                            minutes = restBed,
                            onMinutesChange = { restBed = it },
                        )
                    }
                }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = {
                        if (kind == AnchorKind.WORK &&
                            scheduleKind == WorkScheduleKind.CUSTOM_WEEK &&
                            workDays.isEmpty()
                        ) {
                            error = "Выбери хотя бы один день"
                            return@Button
                        }
                        if (kind == AnchorKind.WORK && workEnd <= workStart) {
                            error = "Окончание должно быть позже начала"
                            return@Button
                        }
                        onSave(
                            settings.copy(
                                wakeMinutes = wake,
                                restWakeMinutes = restWake,
                                workStartMinutes = workStart,
                                workEndMinutes = workEnd,
                                bedtimeMinutes = bed,
                                restBedtimeMinutes = restBed,
                                windDownBeforeBedMinutes = windDown,
                                workScheduleKind = scheduleKind,
                                workDays = if (scheduleKind == WorkScheduleKind.FIVE_TWO) {
                                    setOf(1, 2, 3, 4, 5)
                                } else {
                                    workDays
                                },
                                rotateWorkDays = rotateWork,
                                rotateRestDays = rotateRest,
                                rotateAnchorEpochDay = rotateAnchor,
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
