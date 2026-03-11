package com.example.pacetimer

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.remember
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.gestures.pointerInput
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold


@Composable
fun PaceTimerScreen(viewModel: PaceTimerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    // Edit/Add Dialog States
    var showTimerDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf(-1) } // -1=Add, >=0=Edit

    if (showTimerDialog) {
        val editingInterval = if (editingIndex >= 0) {
            viewModel.getIntervalForEdit(editingIndex)
        } else null

        TimerDialog(
            initialInterval = editingInterval,
            onDismiss = {
                showTimerDialog = false
                editingIndex = -1
            },
            onSave = { name, minutes, color, sound ->
                if (editingIndex >= 0) {
                    viewModel.updateInterval(editingIndex, name, minutes, color, sound)
                } else {
                    viewModel.addInterval(name, minutes, color, sound)
                }
                showTimerDialog = false
                editingIndex = -1
            }
        )
    }

    val haptic = LocalHapticFeedback.current
    LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntervalEnd = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = viewModel.currentBackgroundColor,
        animationSpec = tween(800),
        label = "bg_color"
    )

    val scale by animateDpAsState(
        targetValue = if (viewModel.isRunning.value) 1.dp else 1.1.dp,
        label = "blink"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingIndex = -1
                showTimerDialog = true
            }) {
                Icon(Icons.Default.Add, "Neuer Timer")
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                //verticalArrangement = Arrangement.Center
            ) {
                // ✅ NEUER farbiger Rahmen um Timer-Display
                val currentName = if (viewModel.currentIntervalIndex < viewModel.intervals.size)
                    viewModel.intervals[viewModel.currentIntervalIndex].name else "Done!"

                val borderColor by animateColorAsState(  // Animation für Rahmen
                    targetValue = viewModel.currentBackgroundColor,
                    animationSpec = tween(800),
                    label = "border_color"
                )
                // Textanzeige in der Box mit Rahmen
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 16.dp)
                        .border(
                            width = 12.dp,
                            color = borderColor,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .background(
                            Color.Black.copy(alpha = 0.3f),  // Leicht getönt innen
                            androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = formatTime(viewModel.currentTimeLeft),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Endzeit: ${
                                formatTime(
                                    System.currentTimeMillis() + viewModel.currentTimeLeft,
                                    true
                                )
                            }",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                // NavigationsButtons
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = { if (!viewModel.isRunning.value) viewModel.startTimer() }
                    ) {
                        Icon(Icons.Default.PlayArrow, "Start")
                    }
                    FloatingActionButton(
                        onClick = { if (viewModel.isRunning.value) viewModel.stopTimer() }
                    ) {
                        Icon(Icons.Filled.Stop, "Stop")
                    }
                    FloatingActionButton(onClick = { viewModel.resetTimer() }) {
                        Text("R")
                    }
                    FloatingActionButton(
                        onClick = { viewModel.nextInterval() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, "Nächstes")
                    }
                    FloatingActionButton(
                        onClick = { viewModel.previousInterval() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, "Vorheriges")
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
                // Interval-Liste
                LazyColumn {
                    itemsIndexed(viewModel.intervals) { index, interval ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.moveTo(index)  // Klick: Timer aktivieren
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            editingIndex = index  // Long-Press: Edit
                                            showTimerDialog = true
                                        }
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = interval.color)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Farbkreis (optional)
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(interval.color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))

                                // Name + Dauer
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = interval.name,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${interval.duration / 60000} min",
                                        color = Color.Black.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                }

                                // Aktiv? Markierung
                                if (index == viewModel.currentIntervalIndex) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Aktiv",
                                        tint = Color.Black,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                // Delete Button (immer sichtbar)
                                IconButton(onClick = {
                                    viewModel.removeInterval(index)
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Löschen",
                                        tint = Color.Red
                                    )
                                }

                            }
                        }
                    }
                }


            }

        }
    )
}

@Composable
private fun AddIntervalForm(
    viewModel: PaceTimerViewModel,
    onAddClicked: () -> Unit
) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            FloatingActionButton(
                onClick = onAddClicked
            ) {
                Icon(Icons.Filled.Add, "Neuer Timer")
            }

        }

    }
}

@Composable
private fun ColorPickerButton(
    color: Color,
    selected: Color,
    onSelect: (Color) -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                3.dp,
                if (color == selected) Color.Black else Color.Transparent,
                CircleShape
            )
            .clickable { onSelect(color) }
    )
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun TimerDialog(
    initialInterval: Interval? = null,
    onDismiss: () -> Unit,
    onSave: (String, Int, Color, Boolean) -> Unit
) {
    var name by remember(initialInterval) {
        mutableStateOf(initialInterval?.name ?: "Satz 1")
    }
    var minutesText by remember(initialInterval) {
        mutableStateOf((initialInterval?.duration?.div(60000) ?: 10).toString())
    }
    var selectedColor by remember(initialInterval) {
        mutableStateOf(initialInterval?.color ?: Color.Blue)
    }
    var soundEnabled by remember(initialInterval) {
        mutableStateOf(initialInterval?.soundEnabled ?: false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialInterval != null) "Bearbeiten" else "Neuer Timer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (name.isNotEmpty()) {
                            IconButton(onClick = { name = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Löschen")
                            }
                        }
                    }
                )
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Minuten") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (minutesText.isNotEmpty()) {
                            IconButton(onClick = { minutesText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Löschen")
                            }
                        }
                    }
                )


                Text("Farbe:", fontWeight = FontWeight.Medium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ColorPickerButton(Color.Red, selectedColor) { selectedColor = it }
                    ColorPickerButton(Color.Yellow, selectedColor) { selectedColor = it }
                    ColorPickerButton(Color.Blue, selectedColor) { selectedColor = it }
                    ColorPickerButton(Color.Green, selectedColor) { selectedColor = it }
                    ColorPickerButton(Color.Magenta, selectedColor) { selectedColor = it }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                    Text("Sound On")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val minutes = minutesText.toIntOrNull() ?: 10
                onSave(name, minutes, selectedColor, soundEnabled)
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

fun formatTime(ms: Long, showEndTime: Boolean = false): String {
    if (ms < 0) return if (showEndTime) "--:--" else "00:00"

    return if (showEndTime) {
        // Endzeit-Format HH:MM
        val date = java.util.Date(ms)
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        sdf.format(date)
    } else {
        // Countdown MM:SS
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        "%d:%02d".format(minutes, seconds)
    }
}




