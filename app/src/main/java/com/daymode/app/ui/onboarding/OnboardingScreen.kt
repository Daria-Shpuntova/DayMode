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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daymode.app.data.UserSettings
import com.daymode.app.data.toClockLabel
import com.daymode.app.ui.components.TimePickerField

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
fun OnboardingScreen(
    initial: UserSettings,
    onFinished: (UserSettings) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var wakeMinutes by remember { mutableIntStateOf(initial.wakeMinutes) }
    var workStartMinutes by remember { mutableIntStateOf(initial.workStartMinutes) }
    var workEndMinutes by remember { mutableIntStateOf(initial.workEndMinutes) }
    var isFiveTwo by remember { mutableStateOf(initial.isFiveTwo) }
    var workDays by remember { mutableStateOf(initial.workDays) }
    var bedMinutes by remember { mutableIntStateOf(initial.bedtimeMinutes) }
    var windDown by remember { mutableIntStateOf(initial.windDownBeforeBedMinutes) }

    val title = when (step) {
        0 -> "Во сколько встаёшь?"
        1 -> "Работа или учёба"
        2 -> "График"
        3 -> "Во сколько отбой?"
        else -> "Когда напомнить про сон?"
    }

    val subtitle = when (step) {
        0 -> "Нажми на время — откроется циферблат."
        1 -> "Укажи начало и окончание. Позже можно добавить ещё блоки."
        2 -> "5/2 или свои рабочие дни."
        3 -> "От этого считается вечернее окно без телефона."
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
            0 -> TimePickerField(
                label = "Подъём",
                minutes = wakeMinutes,
                onMinutesChange = { wakeMinutes = it },
            )

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

            2 -> {
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
                        dayLabels.forEach { (day, label) ->
                            FilterChip(
                                selected = day in workDays,
                                onClick = {
                                    workDays = if (day in workDays) workDays - day else workDays + day
                                },
                                label = { Text(label) },
                            )
                        }
                    }
                }
            }

            3 -> TimePickerField(
                label = "Отбой",
                minutes = bedMinutes,
                onMinutesChange = { bedMinutes = it },
            )

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
                    "Окно без телефона начнётся в ${(
                        (bedMinutes - windDown + 24 * 60) % (24 * 60)
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
                    2 -> if (isFiveTwo || workDays.isNotEmpty()) step = 3
                    else -> onFinished(
                        UserSettings(
                            onboardingDone = true,
                            wakeMinutes = wakeMinutes,
                            workStartMinutes = workStartMinutes,
                            workEndMinutes = workEndMinutes,
                            isFiveTwo = isFiveTwo,
                            workDays = if (isFiveTwo) setOf(1, 2, 3, 4, 5) else workDays,
                            bedtimeMinutes = bedMinutes,
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
