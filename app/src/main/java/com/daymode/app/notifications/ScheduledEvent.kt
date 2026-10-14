package com.daymode.app.notifications

import java.time.LocalDate

enum class NotifyKind {
    WORK_START,
    WORK_END,
    BLOCK,
    WIND_DOWN,
    BEDTIME,
}

data class ScheduledEvent(
    val id: String,
    val title: String,
    val date: LocalDate,
    val minutesFromMidnight: Int,
    val kind: NotifyKind,
) {
    val preKey: String get() = "$id:${date}:pre"
    val atKey: String get() = "$id:${date}:at"

    fun preTitle(): String = when (kind) {
        NotifyKind.WORK_START -> "Скоро за работу"
        NotifyKind.WORK_END -> "Скоро свободное время"
        NotifyKind.BLOCK -> "Скоро: $title"
        NotifyKind.WIND_DOWN -> "Скоро без телефона"
        NotifyKind.BEDTIME -> "Скоро пора спать"
    }

    fun preText(): String = when (kind) {
        NotifyKind.WORK_START -> "Через 15 минут лучше сесть за работу или учёбу"
        NotifyKind.WORK_END -> "Через 15 минут можно закончить работу или учёбу"
        NotifyKind.BLOCK -> "Через 15 минут начнётся «$title»"
        NotifyKind.WIND_DOWN -> "Через 15 минут лучше убрать телефон — скоро сон"
        NotifyKind.BEDTIME -> "Через 15 минут лучше ложиться"
    }

    fun atTitle(): String = when (kind) {
        NotifyKind.WORK_START -> "Пора за работу"
        NotifyKind.WORK_END -> "Работа / учёба закончились"
        NotifyKind.BLOCK -> title
        NotifyKind.WIND_DOWN -> "Время без телефона"
        NotifyKind.BEDTIME -> "Пора спать"
    }

    fun atText(): String = when (kind) {
        NotifyKind.WORK_START -> "Можно начинать работу или учёбу"
        NotifyKind.WORK_END -> "Можно отдохнуть или переключиться на другое"
        NotifyKind.BLOCK -> "Пора заняться этим"
        NotifyKind.WIND_DOWN -> "Лучше провести это время без экрана"
        NotifyKind.BEDTIME -> "Спокойной ночи"
    }
}
