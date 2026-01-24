package io.github.xororz.localdream.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class SavedPrompt(
    val id: String,
    val name: String,
    val prompt: String,
    val negativePrompt: String,
    val timestamp: Long
)

class PromptManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("saved_prompts", Context.MODE_PRIVATE)
    
    var savedPrompts by mutableStateOf(loadPrompts())
        private set

    private fun loadPrompts(): List<SavedPrompt> {
        val jsonString = prefs.getString("prompts_list", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<SavedPrompt>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                SavedPrompt(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    prompt = obj.getString("prompt"),
                    negativePrompt = obj.getString("negativePrompt"),
                    timestamp = obj.getLong("timestamp")
                )
            )
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun savePrompt(name: String, prompt: String, negativePrompt: String) {
        val newList = savedPrompts.toMutableList()
        val newItem = SavedPrompt(
            id = System.currentTimeMillis().toString(),
            name = name,
            prompt = prompt,
            negativePrompt = negativePrompt,
            timestamp = System.currentTimeMillis()
        )
        newList.add(0, newItem)
        updatePrefs(newList)
        savedPrompts = newList
    }

    fun deletePrompt(id: String) {
        val newList = savedPrompts.filter { it.id != id }
        updatePrefs(newList)
        savedPrompts = newList
    }

    private fun updatePrefs(list: List<SavedPrompt>) {
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("prompt", item.prompt)
                put("negativePrompt", item.negativePrompt)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("prompts_list", jsonArray.toString()).apply()
    }
}
