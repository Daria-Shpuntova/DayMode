package com.daymode.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.daymode.app.data.BlockType
import com.daymode.app.data.BlocksRepository
import com.daymode.app.data.DayBlock
import com.daymode.app.ui.components.TimePickerField
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBlockSheet(
    existing: DayBlock? = null,
    onDismiss: () -> Unit,
    onSave: (DayBlock) -> Unit,
    onDelete: ((String) -> Unit)? = null,
) {
    var type by remember { mutableStateOf(existing?.type ?: BlockType.READING) }
    var title by remember {
        mutableStateOf(existing?.title ?: existing?.type?.label ?: BlockType.READING.label)
    }
    var start by remember { mutableIntStateOf(existing?.startMinutes ?: (18 * 60)) }
    var end by remember { mutableStateOf(existing?.endMinutes) }
    var hasEnd by remember { mutableStateOf(existing?.endMinutes != null) }
    var days by remember {
        mutableStateOf(existing?.daysOfWeek ?: setOf(LocalDate.now().dayOfWeek.value))
    }
    var error by remember { mutableStateOf<String?>(null) }
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.85f).dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    if (existing == null) "Добавить в день" else "Изменить блок",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Text("Тип", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BlockType.entries.forEach { item ->
                        FilterChip(
                            selected = type == item,
                            onClick = {
                                val shouldRename =
                                    title.isBlank() || BlockType.entries.any { it.label == title }
                                type = item
                                if (shouldRename) title = item.label
                            },
                            label = { Text(item.label) },
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                TimePickerField(
                    label = "Начало",
                    minutes = start,
                    onMinutesChange = { start = it },
                )

                FilterChip(
                    selected = hasEnd,
                    onClick = {
                        hasEnd = !hasEnd
                        if (hasEnd && end == null) end = (start + 60) % (24 * 60)
                    },
                    label = { Text("Указать окончание") },
                )
                if (hasEnd) {
                    TimePickerField(
                        label = "Конец",
                        minutes = end ?: ((start + 60) % (24 * 60)),
                        onMinutesChange = { end = it },
                    )
                }

                Text("Дни недели", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс")
                        .forEach { (day, label) ->
                            FilterChip(
                                selected = day in days,
                                onClick = {
                                    days = if (day in days) days - day else days + day
                                },
                                label = { Text(label) },
                            )
                        }
                }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            error = "Введи название"
                            return@Button
                        }
                        if (days.isEmpty()) {
                            error = "Выбери хотя бы один день"
                            return@Button
                        }
                        onSave(
                            DayBlock(
                                id = existing?.id ?: BlocksRepository.newId(),
                                title = title.trim(),
                                type = type,
                                startMinutes = start,
                                endMinutes = if (hasEnd) end else null,
                                daysOfWeek = days,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Сохранить")
                }

                if (existing != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(existing.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Удалить блок", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}