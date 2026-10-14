package com.daymode.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daymode.app.data.UserSettings
import com.daymode.app.data.WorkScheduleKind
import com.daymode.app.data.toClockLabel
import com.daymode.app.ui.components.TimePickerField
import com.daymode.app.ui.components.WorkSchedulePicker

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initial: UserSettings,
    onFinished: (UserSettings) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var wakeMinutes by remember { mutableIntStateOf(initial.wakeMinutes) }
    var restWakeMinutes by remember { mutableIntStateOf(initial.restWakeMinutes) }
    var workStartMinutes by remember { mutableIntStateOf(initial.workStartMinutes) }
    var workEndMinutes by remember { mutableIntStateOf(initial.workEndMinutes) }
    var scheduleKind by remember { mutableStateOf(initial.workScheduleKind) }
    var workDays by remember { mutableStateOf(initial.workDays) }
    var rotateWork by remember { mutableIntStateOf(initial.rotateWorkDays) }
    var rotateRest by remember { mutableIntStateOf(initial.rotateRestDays) }
    var rotateAnchor by remember { mutableLongStateOf(initial.rotateAnchorEpochDay) }
    var bedMinutes by remember { mutableIntStateOf(initial.bedtimeMinutes) }
    var restBedMinutes by remember { mutableIntStateOf(initial.restBedtimeMinutes) }
    var windDown by remember { mutableIntStateOf(initial.windDownBeforeBedMinutes) }

    val title = when (step) {
        0 -> "Во сколько встаёшь?"
        1 -> "Работа или учёба"
        2 -> "График"
        3 -> "Во сколько отбой?"
        else -> "Когда напомнить про сон?"
    }

    val subtitle = when (step) {
        0 -> "Отдельно для рабочего дня и выходного — по твоему графику."
        1 -> "Укажи начало и окончание. Позже можно добавить ещё блоки."
        2 -> "5/2, смены 2/2, 3/3 или свои дни недели."
        3 -> "Тоже можно по-разному в рабочие дни и выходные."
        else -> "За сколько минут до отбоя прислать напоминание."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("DayMode", style = MaterialTheme.typography.labelLarge)
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        when (step) {
            0 -> {
                TimePickerField(
                    label = "В рабочий день",
                    minutes = wakeMinutes,
                    onMinutesChange = { wakeMinutes = it },
                )
                TimePickerField(
                    label = "В выходной",
                    minutes = restWakeMinutes,
                    onMinutesChange = { restWakeMinutes = it },
                )
            }

            1 -> {
                TimePickerField(
                    label = "Начало",
                    minutes = workStartMinutes,
                    onMinutesChange = { workStartMinutes = it },
                )
                TimePickerField(
                    label = "Окончание",
                    minutes = workEndMinutes,
                    onMinutesChange = { workEndMinutes = it },
                )
            }

            2 -> WorkSchedulePicker(
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

            3 -> {
                TimePickerField(
                    label = "В рабочий день",
                    minutes = bedMinutes,
                    onMinutesChange = { bedMinutes = it },
                )
                TimePickerField(
                    label = "В выходной",
                    minutes = restBedMinutes,
                    onMinutesChange = { restBedMinutes = it },
                )
            }

            else -> {
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
                    "В рабочий день окно с ${(
                        (bedMinutes - windDown + 24 * 60) % (24 * 60)
                        ).toClockLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "В выходной — с ${(
                        (restBedMinutes - windDown + 24 * 60) % (24 * 60)
                        ).toClockLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                when (step) {
                    0, 3 -> step += 1
                    1 -> if (workEndMinutes > workStartMinutes) step = 2
                    2 -> {
                        val ok = scheduleKind != WorkScheduleKind.CUSTOM_WEEK || workDays.isNotEmpty()
                        if (ok) step = 3
                    }
                    else -> onFinished(
                        UserSettings(
                            onboardingDone = true,
                            wakeMinutes = wakeMinutes,
                            restWakeMinutes = restWakeMinutes,
                            workStartMinutes = workStartMinutes,
                            workEndMinutes = workEndMinutes,
                            workScheduleKind = scheduleKind,
                            workDays = if (scheduleKind == WorkScheduleKind.FIVE_TWO) {
                                setOf(1, 2, 3, 4, 5)
                            } else {
                                workDays
                            },
                            rotateWorkDays = rotateWork,
                            rotateRestDays = rotateRest,
                            rotateAnchorEpochDay = rotateAnchor,
                            bedtimeMinutes = bedMinutes,
                            restBedtimeMinutes = restBedMinutes,
                            windDownBeforeBedMinutes = windDown,
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (step < 4) "Далее" else "Начать")
        }

        if (step > 0) {
            TextButton(onClick = { step -= 1 }) {
                Text("Назад")
            }
        }
    }
}
