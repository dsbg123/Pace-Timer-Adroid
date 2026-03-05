package com.example.pacetimer

import androidx.compose.ui.graphics.Color

data class Interval(
    val name: String,
    val duration: Long,  // in ms
    val color: Color,
    val soundEnabled: Boolean = true  // Akustik optional
)
