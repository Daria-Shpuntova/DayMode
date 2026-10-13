package com.daymode.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.daymode.app.data.BlocksRepository
import com.daymode.app.data.DayBlock
import com.daymode.app.data.Habit
import com.daymode.app.data.HabitPeriod
import com.daymode.app.data.HabitsRepository
import com.daymode.app.data.SettingsRepository
import com.daymode.app.data.ThemeMode
import com.daymode.app.data.UserSettings
import com.daymode.app.notifications.NotificationScheduler
import com.daymode.app.ui.screens.createHabit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayModeViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val blocksRepository: BlocksRepository,
    private val habitsRepository: HabitsRepository,
) : AndroidViewModel(application) {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val settings: StateFlow<UserSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings(),
    )

    val blocks: StateFlow<List<DayBlock>> = blocksRepository.blocks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val habits: StateFlow<List<Habit>> = habitsRepository.habits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val completions: StateFlow<Set<String>> = habitsRepository.completions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    init {
        viewModelScope.launch {
            combine(settingsRepository.settings, blocksRepository.blocks) { s, b -> s to b }
                .collect { (s, b) ->
                    if (s.onboardingDone) {
                        NotificationScheduler.reschedule(getApplication(), s, b)
                    }
                }
        }
    }

    fun shiftSelectedDate(days: Long) {
        _selectedDate.value = _selectedDate.value.plusDays(days)
    }

    fun resetSelectedDateToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun completeOnboarding(draft: UserSettings) {
        viewModelScope.launch {
            settingsRepository.save(draft.copy(onboardingDone = true))
        }
    }

    fun updateSettings(draft: UserSettings) {
        viewModelScope.launch {
            settingsRepository.save(draft.copy(onboardingDone = true))
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.save(settings.value.copy(themeMode = mode, onboardingDone = true))
        }
    }

    fun saveBlock(block: DayBlock) {
        viewModelScope.launch {
            blocksRepository.upsert(block)
        }
    }

    fun deleteBlock(id: String) {
        viewModelScope.launch {
            blocksRepository.delete(id)
        }
    }

    fun addHabit(period: HabitPeriod, title: String) {
        viewModelScope.launch {
            habitsRepository.upsert(createHabit(period, title, habits.value))
        }
    }

    fun toggleHabit(habitId: String, date: LocalDate = _selectedDate.value) {
        viewModelScope.launch {
            habitsRepository.toggleCompletion(habitId, date)
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            habitsRepository.delete(id)
        }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            blocksRepository: BlocksRepository,
            habitsRepository: HabitsRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                DayModeViewModel(app, settingsRepository, blocksRepository, habitsRepository)
            }
        }
    }
}
