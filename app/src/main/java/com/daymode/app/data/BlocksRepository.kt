package com.daymode.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.blocksDataStore: DataStore<Preferences> by preferencesDataStore(name = "daymode_blocks")

class BlocksRepository(private val context: Context) {

    private val key = stringPreferencesKey("blocks_json")

    val blocks: Flow<List<DayBlock>> = context.blocksDataStore.data.map { prefs ->
        decode(prefs[key].orEmpty())
    }

    suspend fun upsert(block: DayBlock) {
        context.blocksDataStore.edit { prefs ->
            val current = decode(prefs[key].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.id == block.id }
            if (index >= 0) current[index] = block else current.add(block)
            prefs[key] = encode(current)
        }
    }

    suspend fun delete(id: String) {
        context.blocksDataStore.edit { prefs ->
            val current = decode(prefs[key].orEmpty()).filterNot { it.id == id }
            prefs[key] = encode(current)
        }
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()

        private fun encode(blocks: List<DayBlock>): String {
            val array = JSONArray()
            blocks.forEach { block ->
                array.put(
                    JSONObject()
                        .put("id", block.id)
                        .put("title", block.title)
                        .put("type", block.type.name)
                        .put("start", block.startMinutes)
                        .put("end", block.endMinutes ?: JSONObject.NULL)
                        .put("days", JSONArray(block.daysOfWeek.sorted())),
                )
            }
            return array.toString()
        }

        private fun decode(raw: String): List<DayBlock> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(raw)
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val daysJson = obj.getJSONArray("days")
                        val days = buildSet {
                            for (d in 0 until daysJson.length()) {
                                add(daysJson.getInt(d))
                            }
                        }
                        val endValue = obj.opt("end")
                        val end = when {
                            endValue == null || endValue == JSONObject.NULL -> null
                            else -> obj.getInt("end")
                        }
                        add(
                            DayBlock(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                type = runCatching {
                                    BlockType.valueOf(obj.getString("type"))
                                }.getOrDefault(BlockType.CUSTOM),
                                startMinutes = obj.getInt("start"),
                                endMinutes = end,
                                daysOfWeek = days.ifEmpty { setOf(1, 2, 3, 4, 5) },
                            ),
                        )
                    }
                }
            }.getOrDefault(emptyList())
        }
    }
}
