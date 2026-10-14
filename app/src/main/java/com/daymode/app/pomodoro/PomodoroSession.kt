package com.daymode.app.pomodoro

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class PomodoroPhase {
    IDLE,
    FOCUS,
    BREAK,
}

data class PomodoroUiState(
    val phase: PomodoroPhase = PomodoroPhase.IDLE,
    val remainingSeconds: Int = FOCUS_SECONDS,
    val isRunning: Boolean = false,
    val completedFocusSessions: Int = 0,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    /** When running: deadline in [SystemClock.elapsedRealtime] millis */
    val endsAtElapsed: Long? = null,
) {
    val totalSeconds: Int
        get() = when (phase) {
            PomodoroPhase.BREAK -> breakMinutes * 60
            else -> focusMinutes * 60
        }

    fun remainingNow(nowElapsed: Long = SystemClock.elapsedRealtime()): Int {
        val end = endsAtElapsed
        return if (isRunning && end != null) {
            ((end - nowElapsed) / 1000L).toInt().coerceAtLeast(0)
        } else {
            remainingSeconds
        }
    }

    companion object {
        const val FOCUS_SECONDS = 25 * 60
    }
}

/**
 * Process-wide pomodoro state. Survives Activity recreation;
 * [PomodoroService] keeps it alive while the timer runs.
 */
object PomodoroSession {
    private val _state = MutableStateFlow(PomodoroUiState())
    val state: StateFlow<PomodoroUiState> = _state.asStateFlow()

    fun snapshot(): PomodoroUiState = _state.value

    fun refreshRemaining() {
        val current = _state.value
        if (!current.isRunning || current.endsAtElapsed == null) return
        val left = current.remainingNow()
        if (left != current.remainingSeconds) {
            _state.update { it.copy(remainingSeconds = left) }
        }
    }

    fun startFocus() {
        val focusSec = _state.value.focusMinutes * 60
        val end = SystemClock.elapsedRealtime() + focusSec * 1000L
        _state.update {
            it.copy(
                phase = PomodoroPhase.FOCUS,
                remainingSeconds = focusSec,
                isRunning = true,
                endsAtElapsed = end,
            )
        }
    }

    fun startBreak() {
        val breakSec = _state.value.breakMinutes * 60
        val end = SystemClock.elapsedRealtime() + breakSec * 1000L
        _state.update {
            it.copy(
                phase = PomodoroPhase.BREAK,
                remainingSeconds = breakSec,
                isRunning = true,
                endsAtElapsed = end,
            )
        }
    }

    fun pause() {
        val current = _state.value
        if (!current.isRunning) return
        val left = current.remainingNow()
        _state.update {
            it.copy(
                isRunning = false,
                remainingSeconds = left,
                endsAtElapsed = null,
            )
        }
    }

    fun resume() {
        val current = _state.value
        if (current.isRunning || current.phase == PomodoroPhase.IDLE) return
        val left = current.remainingSeconds.coerceAtLeast(1)
        val end = SystemClock.elapsedRealtime() + left * 1000L
        _state.update {
            it.copy(
                isRunning = true,
                remainingSeconds = left,
                endsAtElapsed = end,
            )
        }
    }

    fun togglePause() {
        if (_state.value.isRunning) pause() else resume()
    }

    fun reset() {
        _state.update {
            it.copy(
                phase = PomodoroPhase.IDLE,
                remainingSeconds = it.focusMinutes * 60,
                isRunning = false,
                endsAtElapsed = null,
            )
        }
    }

    fun setFocusMinutes(minutes: Int) {
        if (_state.value.isRunning) return
        _state.update {
            it.copy(
                focusMinutes = minutes,
                remainingSeconds = if (it.phase != PomodoroPhase.BREAK) minutes * 60 else it.remainingSeconds,
            )
        }
    }

    fun setBreakMinutes(minutes: Int) {
        if (_state.value.isRunning) return
        _state.update {
            it.copy(
                breakMinutes = minutes,
                remainingSeconds = if (it.phase == PomodoroPhase.BREAK) minutes * 60 else it.remainingSeconds,
            )
        }
    }

    /** Called when the scheduled end alarm fires (or remaining hits 0). */
    fun completePhase() {
        val phase = _state.value.phase
        when (phase) {
            PomodoroPhase.FOCUS -> {
                _state.update {
                    it.copy(
                        phase = PomodoroPhase.BREAK,
                        remainingSeconds = it.breakMinutes * 60,
                        isRunning = false,
                        endsAtElapsed = null,
                        completedFocusSessions = it.completedFocusSessions + 1,
                    )
                }
            }
            PomodoroPhase.BREAK -> {
                _state.update {
                    it.copy(
                        phase = PomodoroPhase.IDLE,
                        remainingSeconds = it.focusMinutes * 60,
                        isRunning = false,
                        endsAtElapsed = null,
                    )
                }
            }
            PomodoroPhase.IDLE -> Unit
        }
    }
}
