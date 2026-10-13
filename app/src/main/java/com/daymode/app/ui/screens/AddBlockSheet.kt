package com.daymode.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daymode.app.data.BlockType
import com.daymode.app.data.BlocksRepository
import com.daymode.app.data.DayBlock
import com.daymode.app.ui.components.TimePickerField
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddBlockSheet(
    existing: DayBlock? = null,
    onDismiss: () -> Unit,
    onSave: (DayBlock) -> Unit,
    onDelete: ((String) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var type by remember { mutableStateOf(existing?.type ?: BlockType.READING) }
    var title by remember {
        mutableStateOf(existing?.title ?: existing?.type?.label ?: BlockType.READING.label)
    }
    var start by remember { mutableIntStateOf(existing?.startMinutes ?: (18 * 60)) }
    var end by remember { mutableStateOf(existing?.endMinutes) }
    var hasEnd by remember { mutableStateOf(existing?.endMinutes != null) }
    var days by remember {
        mutableStateOf(
            existing?.daysOfWeek ?: setOf(LocalDate.now().dayOfWeek.value),
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
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

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

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

            Spacer(Modifier.height(8.dp))
        }
    }
}
