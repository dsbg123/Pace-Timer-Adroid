package com.example.pacetimer

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaceTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    private val _intervals = mutableStateListOf<Interval>()
    val intervals: List<Interval> = _intervals

    private val _currentIntervalIndex = mutableIntStateOf(0)
    val currentIntervalIndex: Int get() = _currentIntervalIndex.intValue

    private val _currentTimeLeft = mutableLongStateOf(0L)
    val currentTimeLeft: Long get() = _currentTimeLeft.longValue

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    val currentBackgroundColor: Color
        get() = if (currentIntervalIndex < intervals.size)
            intervals[currentIntervalIndex].color
        else Color.White

    var onIntervalEnd: (() -> Unit)? = null

    init {
        loadIntervals()
    }

    private fun loadIntervals() {
        val loaded = prefs.loadIntervals()
        if (loaded.isNotEmpty()) {
            _intervals.clear()
            _intervals.addAll(loaded)
        } else {
            addDefaultIntervals()
        }
    }

    private fun addDefaultIntervals() {
        addInterval("Satz 1", 5, Color.Blue, true)
        addInterval("Satz 2", 3, Color.Green, true)
        addInterval("Pause", 1, Color.Gray, false)
        saveIntervals()
    }

    private fun saveIntervals() {
        prefs.saveIntervals(_intervals)
    }

    fun addInterval(name: String, minutes: Int, color: Color, soundEnabled: Boolean) {
        _intervals.add(
            Interval(
                name = name,
                duration = minutes * 60L * 1000L,
                color = color,
                soundEnabled = soundEnabled
            )
        )
        saveIntervals()
    }

    fun updateInterval(index: Int, name: String, minutes: Int, color: Color, sound: Boolean) {
        if (index !in _intervals.indices) return
        val ms = minutes.coerceAtLeast(1) * 60_000L
        _intervals[index] = Interval(name.trim().ifEmpty { "Abschnitt" }, ms, color, sound)
        if (index == _currentIntervalIndex.intValue) {
            _currentTimeLeft.longValue = ms
        }
        saveIntervals()
    }

    fun removeInterval(index: Int) {
        if (index !in _intervals.indices) return
        _intervals.removeAt(index)
        if (index == currentIntervalIndex) {
            _currentIntervalIndex.intValue = 0
            _currentTimeLeft.longValue = _intervals.getOrNull(0)?.duration ?: 0L
        } else if (index < currentIntervalIndex) {
            _currentIntervalIndex.intValue--
        }
        saveIntervals()
    }

    fun startTimer() {
        if (!_isRunning.value && currentIntervalIndex < intervals.size) {
            _isRunning.value = true
            viewModelScope.launch {
                while (_isRunning.value && currentIntervalIndex < intervals.size) {
                    val interval = intervals[currentIntervalIndex]
                    if (_currentTimeLeft.longValue <= 0L) {
                        _currentTimeLeft.longValue = interval.duration
                    }
                    var timeLeft = _currentTimeLeft.longValue

                    while (timeLeft > 0 && _isRunning.value) {
                        delay(1000L)
                        timeLeft -= 1000L
                        _currentTimeLeft.longValue = timeLeft
                    }
                    if (!_isRunning.value) break

                    onIntervalEnd?.invoke()
                    _currentIntervalIndex.intValue++
                }
                _isRunning.value = false
            }
        }
    }

    fun stopTimer() { _isRunning.value = false }

    fun resetTimer() {
        _isRunning.value = false
        resetCurrentInterval()
    }

    private fun resetCurrentInterval() {
        if (currentIntervalIndex < intervals.size) {
            _currentTimeLeft.longValue = intervals[currentIntervalIndex].duration
        }
    }

    fun getIntervalForEdit(index: Int): Interval? = _intervals.getOrNull(index)

    fun moveTo(index: Int) {
        if (index !in _intervals.indices) return
        _currentIntervalIndex.intValue = index
        resetCurrentInterval()
    }

    fun nextInterval() {
        stopTimer()
        if (currentIntervalIndex < intervals.size - 1) {
            _currentIntervalIndex.intValue++
        } else {
            _currentIntervalIndex.intValue = 0
        }
        resetCurrentInterval()
    }

    fun previousInterval() {
        stopTimer()
        if (currentIntervalIndex > 0) {
            _currentIntervalIndex.intValue--
        } else {
            _currentIntervalIndex.intValue = intervals.size - 1
        }
        resetCurrentInterval()
    }
}
