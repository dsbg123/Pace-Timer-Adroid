package com.example.pacetimer

import Interval
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaceTimerViewModel : ViewModel() {
    private val _intervals = mutableStateListOf<Interval>()
    val intervals: List<Interval> = _intervals

    private val _currentIntervalIndex = mutableIntStateOf(0)
    val currentIntervalIndex: Int get() = _currentIntervalIndex.intValue

    private val _currentTimeLeft = mutableLongStateOf(0L)
    val currentTimeLeft: Long get() = _currentTimeLeft.longValue

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    // Aktuelle Farbe für Hintergrund
    val currentBackgroundColor: Color
        get() = if (currentIntervalIndex < intervals.size)
            intervals[currentIntervalIndex].color
        else Color.Black

    // Callback für Feedback
    var onIntervalEnd: (() -> Unit)? = null

    fun addInterval(name: String, minutes: Int, color: Color, soundEnabled: Boolean) {
        _intervals.add(Interval(name, minutes * 60L * 1000L, color, soundEnabled))
    }

    fun startTimer() {
        if (!_isRunning.value && currentIntervalIndex < intervals.size) {
            _isRunning.value = true
            viewModelScope.launch {
                while (_isRunning.value && currentIntervalIndex < intervals.size) {
                    val interval = intervals[currentIntervalIndex]
                    _currentTimeLeft.longValue = interval.duration
                    var timeLeft = interval.duration
                    while (timeLeft > 0 && _isRunning.value) {
                        delay(1000L)
                        timeLeft -= 1000L
                        _currentTimeLeft.longValue = timeLeft
                    }
                    // Timer abgelaufen → Feedback
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
        _currentIntervalIndex.intValue = 0
        _currentTimeLeft.longValue = 0L
    }
}
