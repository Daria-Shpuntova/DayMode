package com.daymode.app.ui.pomodoro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.daymode.app.pomodoro.PomodoroSession
import com.daymode.app.pomodoro.PomodoroService
import com.daymode.app.pomodoro.PomodoroUiState
import kotlinx.coroutines.flow.StateFlow

class PomodoroViewModel(
    application: Application,
) : AndroidViewModel(application) {

    val state: StateFlow<PomodoroUiState> = PomodoroSession.state

    fun startFocus() {
        PomodoroService.startFocus(getApplication())
    }

    fun startBreak() {
        PomodoroService.startBreak(getApplication())
    }

    fun togglePause() {
        PomodoroService.togglePause(getApplication())
    }

    fun reset() {
        PomodoroService.reset(getApplication())
    }

    fun setFocusMinutes(minutes: Int) {
        PomodoroSession.setFocusMinutes(minutes)
    }

    fun setBreakMinutes(minutes: Int) {
        PomodoroSession.setBreakMinutes(minutes)
    }

    fun refreshRemaining() {
        PomodoroSession.refreshRemaining()
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                PomodoroViewModel(app)
            }
        }
    }
}
