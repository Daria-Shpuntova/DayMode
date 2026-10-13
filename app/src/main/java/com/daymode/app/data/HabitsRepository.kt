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
import java.time.LocalDate
import java.util.UUID

private val Context.habitsDataStore: DataStore<Preferences> by preferencesDataStore(name = "daymode_habits")

class HabitsRepository(private val context: Context) {

    private object Keys {
        val habits = stringPreferencesKey("habits_json")
        val completions = stringPreferencesKey("completions_json")
    }

    val habits: Flow<List<Habit>> = context.habitsDataStore.data.map { prefs ->
        decodeHabits(prefs[Keys.habits].orEmpty())
    }

    /** Keys like "habitId:2026-10-13" */
    val completions: Flow<Set<String>> = context.habitsDataStore.data.map { prefs ->
        decodeCompletions(prefs[Keys.completions].orEmpty())
    }

    suspend fun upsert(habit: Habit) {
        context.habitsDataStore.edit { prefs ->
            val current = decodeHabits(prefs[Keys.habits].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.id == habit.id }
            if (index >= 0) current[index] = habit else current.add(habit)
            prefs[Keys.habits] = encodeHabits(current)
        }
    }

    suspend fun delete(id: String) {
        context.habitsDataStore.edit { prefs ->
            val current = decodeHabits(prefs[Keys.habits].orEmpty()).filterNot { it.id == id }
            prefs[Keys.habits] = encodeHabits(current)
            val comps = decodeCompletions(prefs[Keys.completions].orEmpty())
                .filterNot { it.startsWith("$id:") }
                .toSet()
            prefs[Keys.completions] = encodeCompletions(comps)
        }
    }

    suspend fun toggleCompletion(habitId: String, date: LocalDate = LocalDate.now()) {
        val key = completionKey(habitId, date)
        context.habitsDataStore.edit { prefs ->
            val comps = decodeCompletions(prefs[Keys.completions].orEmpty()).toMutableSet()
            if (key in comps) comps.remove(key) else comps.add(key)
            prefs[Keys.completions] = encodeCompletions(comps)
        }
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()

        fun completionKey(habitId: String, date: LocalDate): String = "$habitId:$date"

        private fun encodeHabits(habits: List<Habit>): String {
            val array = JSONArray()
            habits.forEach { habit ->
                array.put(
                    JSONObject()
                        .put("id", habit.id)
                        .put("title", habit.title)
                        .put("period", habit.period.name)
                        .put("order", habit.order),
                )
            }
            return array.toString()
        }

        private fun decodeHabits(raw: String): List<Habit> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(raw)
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        add(
                            Habit(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                period = runCatching {
                                    HabitPeriod.valueOf(obj.getString("period"))
                                }.getOrDefault(HabitPeriod.MORNING),
                                order = obj.optInt("order", i),
                            ),
                        )
                    }
                }.sortedWith(compareBy({ it.period }, { it.order }, { it.title }))
            }.getOrDefault(emptyList())
        }

        private fun encodeCompletions(keys: Set<String>): String {
            val array = JSONArray()
            keys.sorted().forEach { array.put(it) }
            return array.toString()
        }

        private fun decodeCompletions(raw: String): Set<String> {
            if (raw.isBlank()) return emptySet()
            return runCatching {
                val array = JSONArray(raw)
                buildSet {
                    for (i in 0 until array.length()) add(array.getString(i))
                }
            }.getOrDefault(emptySet())
        }
    }
}
