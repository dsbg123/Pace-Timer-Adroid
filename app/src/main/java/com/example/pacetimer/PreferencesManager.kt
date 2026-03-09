package com.example.pacetimer

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("pace_timer_prefs", Context.MODE_PRIVATE)

    private val KeyIntervals = "intervals"

    fun saveIntervals(intervals: List<Interval>) {
        val jsonArray = JSONArray()
        intervals.forEach { interval ->
            val obj = JSONObject().apply {
                put("name", interval.name)
                put("duration", interval.duration)
                put("color", interval.color.toArgb())
                put("soundEnabled", interval.soundEnabled)
            }
            jsonArray.put(obj)
        }
        prefs.edit()
            .putString(KeyIntervals, jsonArray.toString())
            .apply()
    }

    fun loadIntervals(): List<Interval> {
        val jsonString = prefs.getString(KeyIntervals, null) ?: return emptyList()
        val result = mutableListOf<Interval>()
        try {
            val arr = JSONArray(jsonString)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.getString("name")
                val duration = obj.getLong("duration")
                val colorInt = obj.getInt("color")
                val soundEnabled = obj.optBoolean("soundEnabled", true)
                result.add(
                    Interval(
                        name = name,
                        duration = duration,
                        color = ComposeColor(colorInt),
                        soundEnabled = soundEnabled
                    )
                )
            }
        } catch (_: Exception) {
            // return empty list on error
        }
        return result
    }

    private fun ComposeColor.toArgb(): Int =
        AndroidColor.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
}
