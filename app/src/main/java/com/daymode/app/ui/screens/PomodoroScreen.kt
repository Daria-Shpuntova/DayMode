package com.daymode.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daymode.app.pomodoro.PomodoroPhase
import com.daymode.app.ui.pomodoro.PomodoroViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModel.factory()),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isRunning) {
        while (true) {
            viewModel.refreshRemaining()
            delay(250)
        }
    }

    val remaining = state.remainingNow()
    val minutes = remaining / 60
    val seconds = remaining % 60
    val phaseLabel = when (state.phase) {
        PomodoroPhase.IDLE -> "Готов к фокусу"
        PomodoroPhase.FOCUS -> "Фокус"
        PomodoroPhase.BREAK -> "Перерыв"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Помидор", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Таймер идёт даже при блокировке экрана — остаток виден в уведомлении.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))
        Text(phaseLabel, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(
            "%02d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
        )
        Text(
            "Сегодня завершено фокусов: ${state.completedFocusSessions}",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                state.phase == PomodoroPhase.IDLE -> {
                    Button(
                        onClick = viewModel::startFocus,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Старт фокуса")
                    }
                }
                else -> {
                    Button(
                        onClick = viewModel::togglePause,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (state.isRunning) "Пауза" else "Продолжить")
                    }
                    OutlinedButton(
                        onClick = viewModel::reset,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Сброс")
                    }
                }
            }
        }

        if (state.phase == PomodoroPhase.BREAK && !state.isRunning) {
            Button(
                onClick = viewModel::startBreak,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Начать перерыв")
            }
            OutlinedButton(
                onClick = viewModel::startFocus,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сразу новый фокус")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Длительность фокуса", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 25, 45, 50).forEach { minutesOption ->
                FilterChip(
                    selected = state.focusMinutes == minutesOption,
                    onClick = { viewModel.setFocusMinutes(minutesOption) },
                    enabled = !state.isRunning,
                    label = { Text("$minutesOption мин") },
                )
            }
        }
        Text("Длительность перерыва", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15).forEach { minutesOption ->
                FilterChip(
                    selected = state.breakMinutes == minutesOption,
                    onClick = { viewModel.setBreakMinutes(minutesOption) },
                    enabled = !state.isRunning,
                    label = { Text("$minutesOption мин") },
                )
            }
        }
    }
}
