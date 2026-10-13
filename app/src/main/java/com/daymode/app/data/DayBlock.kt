package com.daymode.app.data

enum class BlockType(val label: String) {
    STUDY("Учёба"),
    WORK("Работа"),
    READING("Чтение"),
    SPORT("Спорт"),
    CUSTOM("Своё"),
}

data class DayBlock(
    val id: String,
    val title: String,
    val type: BlockType,
    val startMinutes: Int,
    val endMinutes: Int? = null,
    /** ISO day numbers 1=Mon .. 7=Sun */
    val daysOfWeek: Set<Int>,
) {
    fun occursOn(isoDay: Int): Boolean = isoDay in daysOfWeek

    fun timeLabel(): String {
        val start = startMinutes.toClockLabel()
        val end = endMinutes?.toClockLabel()
        return if (end != null) "$start–$end" else "с $start"
    }

    fun daysLabel(): String {
        val map = listOf(1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс")
        return map.filter { it.first in daysOfWeek }.joinToString(" ") { it.second }
    }
}
