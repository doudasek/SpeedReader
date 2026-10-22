package com.martindoudera.speedreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme
import com.martindoudera.speedreader.feature.speedtest.ui.SpeedTestScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedReaderTheme {
                SpeedTestScreen()
            }
        }
    }
}
