package com.daymode.app.notifications

import com.daymode.app.data.DayBlock
import com.daymode.app.data.UserSettings
import com.daymode.app.data.bedtimeMinutesFor
import com.daymode.app.data.isWorkDay
import com.daymode.app.data.windDownStartFor
import java.time.LocalDate

object EventPlanner {

    /**
     * Builds reminders for the next days.
     * Wake/подъём is intentionally excluded.
     */
    fun eventsForDays(
        settings: UserSettings,
        blocks: List<DayBlock>,
        startDate: LocalDate = LocalDate.now(),
        dayCount: Int = 14,
    ): List<ScheduledEvent> {
        val result = mutableListOf<ScheduledEvent>()
        for (offset in 0 until dayCount) {
            val date = startDate.plusDays(offset.toLong())
            val isoDay = date.dayOfWeek.value

            if (settings.isWorkDay(date)) {
                result += ScheduledEvent(
                    id = "work_start",
                    title = "Работа / учёба",
                    date = date,
                    minutesFromMidnight = settings.workStartMinutes,
                    kind = NotifyKind.WORK_START,
                )
                if (settings.workEndMinutes != settings.workStartMinutes) {
                    result += ScheduledEvent(
                        id = "work_end",
                        title = "Конец работы / учёбы",
                        date = date,
                        minutesFromMidnight = settings.workEndMinutes,
                        kind = NotifyKind.WORK_END,
                    )
                }
            }

            blocks.filter { it.occursOn(isoDay) }.forEach { block ->
                result += ScheduledEvent(
                    id = "block:${block.id}",
                    title = block.title,
                    date = date,
                    minutesFromMidnight = block.startMinutes,
                    kind = NotifyKind.BLOCK,
                )
            }

            val windDownStart = settings.windDownStartFor(date)
            val bedtime = settings.bedtimeMinutesFor(date)
            result += ScheduledEvent(
                id = "wind_down",
                title = "Без телефона",
                date = date,
                minutesFromMidnight = windDownStart,
                kind = NotifyKind.WIND_DOWN,
            )
            result += ScheduledEvent(
                id = "bedtime",
                title = "Отбой",
                date = date,
                minutesFromMidnight = bedtime,
                kind = NotifyKind.BEDTIME,
            )
        }
        return result
    }
}
