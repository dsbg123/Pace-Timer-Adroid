package com.example.pacetimer

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PaceTimerViewModel : ViewModel() {

    private var _startTime: Long = 0L
    private var _pauseTime: Long = 0L
    private var _totalPausedTime: Long = 0L

    val endTimeFormatted: String
        get() {
            val remainingMs = currentTimeLeft
            val estimatedEnd = System.currentTimeMillis() + remainingMs
            return formatTime(estimatedEnd)
        }

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
        else Color.White

    // Callback für Feedback
    var onIntervalEnd: (() -> Unit)? = null

    fun addInterval(name: String, minutes: Int, color: Color, soundEnabled: Boolean) {
        _intervals.add(Interval(name, minutes * 60L * 1000L, color, soundEnabled))
    }

    fun startTimer() {
        if (!_isRunning.value && currentIntervalIndex < intervals.size) {
            _isRunning.value = true
            _startTime = System.currentTimeMillis()
            _pauseTime = 0L

            viewModelScope.launch {
                while (_isRunning.value && currentIntervalIndex < intervals.size) {
                    val interval = intervals[currentIntervalIndex]
                    // Wenn zuvor Stop gedrückt wurde, dann muss er mit der gestoppten Zeit weiterfahren.
                    if (_currentTimeLeft.longValue <= 0L) {
                        _currentTimeLeft.longValue = interval.duration
                    }

                    var timeLeft = _currentTimeLeft.longValue

                    while (timeLeft > 0 && _isRunning.value) {
                        delay(1000L)
                        timeLeft -= 1000L
                        _currentTimeLeft.longValue = timeLeft
                    }
                    // Wenn gestopt: Schleife sauber beenden, NICHT weiterschalten
                    if (!_isRunning.value) {
                        break
                    }

                    // Timer abgelaufen → Feedback
                    onIntervalEnd?.invoke()
                    _currentIntervalIndex.intValue++
                }
                _isRunning.value = false
            }
        }
    }

    fun stopTimer() {
        _isRunning.value = false
    }

    fun resetTimer() {
        _isRunning.value = false
        //_pauseTime = System.currentTimeMillis()
        //_totalPausedTime += (_pauseTime - _startTime)
        resetCurrentInterval()
    }

    private fun resetCurrentInterval() {
        if (currentIntervalIndex < intervals.size) {
            _currentTimeLeft.longValue = intervals[currentIntervalIndex].duration
        }
    }

    fun updateInterval(index: Int, name: String, minutes: Int, color: Color, sound: Boolean) {
        if (index !in _intervals.indices) return // Überprüfen, ob Index gültig
        val ms = minutes.coerceAtLeast(1) * 60_000L
        _intervals[index] = Interval(name.trim().ifEmpty { "Abschnitt" }, ms, color, sound)

        //Fals aktuell bearbeitet wird, Zeit aktualisieren
        if (index == _currentIntervalIndex.intValue) {
            _currentTimeLeft.longValue = ms
        }
    }

    fun getIntervalForEdit(index: Int): Interval? = _intervals.getOrNull(index)

    // _currentIntervalIndex.intValue = index
    fun moveTo(index: Int) {
        if (index !in _intervals.indices) return
        _currentIntervalIndex.intValue = index
        _currentTimeLeft.longValue = intervals[index].duration
    }

    // In PaceTimerViewModel.kt HINZUFÜGEN:
    fun removeInterval(index: Int) {
        if (index !in _intervals.indices) return
        _intervals.removeAt(index)

        // Falls gelöschter Timer aktiv war → zum Ersten wechseln
        if (index == currentIntervalIndex) {
            _currentIntervalIndex.intValue = 0.coerceAtLeast(0)
            _currentTimeLeft.longValue = _intervals.getOrNull(0)?.duration ?: 0L
        } else if (index < currentIntervalIndex) {
            _currentIntervalIndex.intValue--
        }
    }

    // ← NEU: Vor/zurück navigieren
    fun nextInterval() {
        stopTimer()
        if (currentIntervalIndex < intervals.size - 1) {
            _currentIntervalIndex.intValue++
        } else {
            _currentIntervalIndex.intValue = 0  // Zyklisch zum Anfang
        }
        resetCurrentInterval()
    }

    fun previousInterval() {
        stopTimer()
        if (currentIntervalIndex > 0) {
            _currentIntervalIndex.intValue--
        } else {
            _currentIntervalIndex.intValue = intervals.size - 1  // Zyklisch zum Ende
        }
        resetCurrentInterval()
    }



}
