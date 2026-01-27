
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PaceTimerScreen(viewModel: PaceTimerViewModel = viewModel()) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val currentName = if (viewModel.currentIntervalIndex < viewModel.intervals.size)
            viewModel.intervals[viewModel.currentIntervalIndex].name else "Done!"
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

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = formatTime(viewModel.currentTimeLeft),
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(48.dp))

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
        }

        Spacer(modifier = Modifier.height(48.dp))

        LazyColumn {
            items(viewModel.intervals) { interval ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = interval.color)
                ) {
                    Text(
                        text = "${interval.name}: ${interval.duration / 60000} min",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AddIntervalForm(viewModel)
    }
}

@Composable
private fun AddIntervalForm(viewModel: PaceTimerViewModel) {
    var name by remember { mutableStateOf("Set 1") }
    var minutes by remember { mutableStateOf(5) }
    var color by remember { mutableStateOf(Color.Yellow) }
    var soundEnabled by remember { mutableStateOf(true) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = minutes.toString(),
            onValueChange = { minutes = it.toIntOrNull() ?: 5 },
            label = { Text("Minutes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorPickerButton(Color.Red, color) { color = it }
            ColorPickerButton(Color.Yellow, color) { color = it }
            ColorPickerButton(Color.Blue, color) { color = it }
            ColorPickerButton(Color.Green, color) { color = it }
            ColorPickerButton(Color.Magenta, color) { color = it }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it }
            )
            Text("Sound On")
        }

        Button(
            onClick = {
                viewModel.addInterval(name, minutes, color, soundEnabled)
                name = "New Section"
                minutes = 5
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Add Interval")
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
                3.dp, Color.Black,
                if (color == selected) CircleShape else CircleShape
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
