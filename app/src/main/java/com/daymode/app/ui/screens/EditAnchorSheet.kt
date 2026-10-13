package com.daymode.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daymode.app.data.UserSettings
import com.daymode.app.data.toClockLabel
import com.daymode.app.ui.components.TimePickerField

enum class AnchorKind {
    WAKE,
    WORK,
    WIND_DOWN,
    BEDTIME,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditAnchorSheet(
    kind: AnchorKind,
    settings: UserSettings,
    onDismiss: () -> Unit,
    onSave: (UserSettings) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var wake by remember { mutableIntStateOf(settings.wakeMinutes) }
    var work by remember { mutableIntStateOf(settings.workStartMinutes) }
    var bed by remember { mutableIntStateOf(settings.bedtimeMinutes) }
    var windDown by remember { mutableIntStateOf(settings.windDownBeforeBedMinutes) }
    var isFiveTwo by remember { mutableStateOf(settings.isFiveTwo) }
    var workDays by remember { mutableStateOf(settings.workDays) }
    var error by remember { mutableStateOf<String?>(null) }

    val title = when (kind) {
        AnchorKind.WAKE -> "РџРѕРґСЉС‘Рј"
        AnchorKind.WORK -> "Р Р°Р±РѕС‚Р° / СѓС‡С‘Р±Р°"
        AnchorKind.WIND_DOWN -> "РћРєРЅРѕ Р±РµР· С‚РµР»РµС„РѕРЅР°"
        AnchorKind.BEDTIME -> "РћС‚Р±РѕР№"
    }

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
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "РќР°Р¶РјРё РЅР° РІСЂРµРјСЏ, С‡С‚РѕР±С‹ РѕС‚РєСЂС‹С‚СЊ С†РёС„РµСЂР±Р»Р°С‚",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            when (kind) {
                AnchorKind.WAKE -> TimePickerField(
                    label = "РџРѕРґСЉС‘Рј",
                    minutes = wake,
                    onMinutesChange = { wake = it },
                )

                AnchorKind.WORK -> {
                    TimePickerField(
                        label = "РќР°С‡Р°Р»Рѕ",
                        minutes = work,
                        onMinutesChange = { work = it },
                    )
                    Text("Р“СЂР°С„РёРє", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isFiveTwo,
                            onClick = {
                                isFiveTwo = true
                                workDays = setOf(1, 2, 3, 4, 5)
                            },
                            label = { Text("5/2") },
                        )
                        FilterChip(
                            selected = !isFiveTwo,
                            onClick = { isFiveTwo = false },
                            label = { Text("РЎРІРѕРё РґРЅРё") },
                        )
                    }
                    if (!isFiveTwo) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                1 to "РџРЅ", 2 to "Р’С‚", 3 to "РЎСЂ", 4 to "Р§С‚",
                                5 to "РџС‚", 6 to "РЎР±", 7 to "Р’СЃ",
                            ).forEach { (day, label) ->
                                FilterChip(
                                    selected = day in workDays,
                                    onClick = {
                                        workDays =
                                            if (day in workDays) workDays - day else workDays + day
                                    },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                }

                AnchorKind.WIND_DOWN -> {
                    Text("Р—Р° СЃРєРѕР»СЊРєРѕ РґРѕ РѕС‚Р±РѕСЏ", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 45, 60, 90).forEach { minutes ->
                            FilterChip(
                                selected = windDown == minutes,
                                onClick = { windDown = minutes },
                                label = { Text("$minutes РјРёРЅ") },
                            )
                        }
                    }
                    Text(
                        "РЎРµР№С‡Р°СЃ РѕРєРЅРѕ РЅР°С‡РЅС‘С‚СЃСЏ РІ ${(
                            (bed - windDown + 24 * 60) % (24 * 60)
                            ).toClockLabel()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                AnchorKind.BEDTIME -> TimePickerField(
                    label = "РћС‚Р±РѕР№",
                    minutes = bed,
                    onMinutesChange = { bed = it },
                )
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    if (kind == AnchorKind.WORK && !isFiveTwo && workDays.isEmpty()) {
                        error = "Р’С‹Р±РµСЂРё С…РѕС‚СЏ Р±С‹ РѕРґРёРЅ РґРµРЅСЊ"
                        return@Button
                    }
                    onSave(
                        settings.copy(
                            wakeMinutes = wake,
                            workStartMinutes = work,
                            bedtimeMinutes = bed,
                            windDownBeforeBedMinutes = windDown,
                            isFiveTwo = isFiveTwo,
                            workDays = if (isFiveTwo) setOf(1, 2, 3, 4, 5) else workDays,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("РЎРѕС…СЂР°РЅРёС‚СЊ")
            }
        }
    }
}

