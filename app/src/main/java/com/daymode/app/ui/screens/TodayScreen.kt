package com.daymode.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daymode.app.data.DayBlock
import com.daymode.app.data.UserSettings
import com.daymode.app.data.toClockLabel
import com.daymode.app.ui.components.DateSwitcher
import java.time.LocalDate
import java.time.LocalTime

private data class TimelineItem(
    val sortKey: Int,
    val title: String,
    val time: String,
    val hint: String,
    val block: DayBlock? = null,
    val anchor: AnchorKind? = null,
)

@Composable
fun TodayScreen(
    settings: UserSettings,
    blocks: List<DayBlock>,
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    onSaveSettings: (UserSettings) -> Unit,
    onSaveBlock: (DayBlock) -> Unit,
    onDeleteBlock: (String) -> Unit,
) {
    val realToday = LocalDate.now()
    val isViewingToday = selectedDate == realToday
    val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
    val isoDay = selectedDate.dayOfWeek.value
    val isWorkDay = isoDay in settings.workDays
    val windDownStart =
        (settings.bedtimeMinutes - settings.windDownBeforeBedMinutes + 24 * 60) % (24 * 60)

    val dayBlocks = blocks.filter { it.occursOn(isoDay) }.sortedBy { it.startMinutes }

    val scrollState = rememberScrollState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<DayBlock?>(null) }
    var editingAnchor by remember { mutableStateOf<AnchorKind?>(null) }

    val timeline = buildList {
        add(
            TimelineItem(
                sortKey = settings.wakeMinutes,
                title = "Подъём",
                time = settings.wakeMinutes.toClockLabel(),
                hint = "нажми, чтобы изменить",
                anchor = AnchorKind.WAKE,
            ),
        )
        add(
            TimelineItem(
                sortKey = settings.workStartMinutes,
                title = "Работа / учёба",
                time = "с ${settings.workStartMinutes.toClockLabel()}",
                hint = if (isWorkDay) "нажми, чтобы изменить" else "выходной · нажми, чтобы изменить",
                anchor = AnchorKind.WORK,
            ),
        )
        dayBlocks.forEach { block ->
            add(
                TimelineItem(
                    sortKey = block.startMinutes,
                    title = block.title,
                    time = block.timeLabel(),
                    hint = block.daysLabel(),
                    block = block,
                ),
            )
        }
        add(
            TimelineItem(
                sortKey = windDownStart,
                title = "Без телефона",
                time = "с ${windDownStart.toClockLabel()}",
                hint = "за ${settings.windDownBeforeBedMinutes} мин до сна · нажми, чтобы изменить",
                anchor = AnchorKind.WIND_DOWN,
            ),
        )
        add(
            TimelineItem(
                sortKey = settings.bedtimeMinutes,
                title = "Отбой",
                time = settings.bedtimeMinutes.toClockLabel(),
                hint = "нажми, чтобы изменить",
                anchor = AnchorKind.BEDTIME,
            ),
        )
    }.sortedBy { it.sortKey }

    val nextItem = if (isViewingToday) {
        timeline.firstOrNull { it.sortKey > nowMinutes } ?: timeline.firstOrNull()
    } else {
        timeline.firstOrNull()
    }

    val phase = if (!isViewingToday) {
        "Просмотр другого дня"
    } else {
        when {
            nowMinutes >= settings.bedtimeMinutes || nowMinutes < settings.wakeMinutes -> "Ночь / сон"
            nowMinutes >= windDownStart -> "Окно без телефона"
            dayBlocks.any {
                nowMinutes >= it.startMinutes && (it.endMinutes == null || nowMinutes < it.endMinutes)
            } -> dayBlocks.first {
                nowMinutes >= it.startMinutes && (it.endMinutes == null || nowMinutes < it.endMinutes)
            }.title
            isWorkDay && nowMinutes >= settings.workStartMinutes -> "Рабочий / учебный день"
            nowMinutes >= settings.wakeMinutes -> "Утро"
            else -> "День"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DateSwitcher(
            selectedDate = selectedDate,
            onPrevious = onPreviousDay,
            onNext = onNextDay,
            onResetToToday = onResetToToday,
        )

        Text(
            if (isViewingToday) "Сейчас: $phase" else phase,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            if (isViewingToday) {
                "Дальше: ${nextItem?.let { "${it.title} · ${it.time}" } ?: "—"}"
            } else {
                "План дня: ${timeline.size} блоков"
            },
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(4.dp))
        Text("Лента дня", style = MaterialTheme.typography.titleSmall)

        timeline.forEach { item ->
            DayBlockCard(
                title = item.title,
                time = item.time,
                hint = item.hint,
                onClick = {
                    when {
                        item.block != null -> editing = item.block
                        item.anchor != null -> editingAnchor = item.anchor
                    }
                },
            )
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                editing = null
                showAdd = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Добавить в день")
        }
    }

    if (showAdd || editing != null) {
        AddBlockSheet(
            existing = editing,
            onDismiss = {
                showAdd = false
                editing = null
            },
            onSave = { block ->
                onSaveBlock(block)
                showAdd = false
                editing = null
            },
            onDelete = editing?.let {
                { id ->
                    onDeleteBlock(id)
                    showAdd = false
                    editing = null
                }
            },
        )
    }

    editingAnchor?.let { kind ->
        EditAnchorSheet(
            kind = kind,
            settings = settings,
            onDismiss = { editingAnchor = null },
            onSave = { updated ->
                onSaveSettings(updated)
                editingAnchor = null
            },
        )
    }
}

@Composable
private fun DayBlockCard(
    title: String,
    time: String,
    hint: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(time, style = MaterialTheme.typography.bodyLarge)
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
