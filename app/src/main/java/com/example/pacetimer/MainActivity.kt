package com.example.pacetimer

// import PaceTimerScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pacetimer.ui.theme.PaceTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaceTimerTheme {
                PaceTimerScreen()
            }
        }
    }
}
