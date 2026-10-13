package com.daymode.app.data

enum class HabitPeriod(val label: String) {
    MORNING("Утро"),
    EVENING("Вечер"),
}

data class Habit(
    val id: String,
    val title: String,
    val period: HabitPeriod,
    val order: Int = 0,
)
