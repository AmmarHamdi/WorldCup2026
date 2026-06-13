package com.worldcup.calendar2026

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.worldcup.calendar2026.ui.MainScreen
import com.worldcup.calendar2026.ui.SplashScreen
import com.worldcup.calendar2026.ui.theme.WorldCupTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

private const val SPLASH_STEPS = 20
private const val SPLASH_STEP_DELAY_MS = 120L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorldCupTheme {
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (!granted) {
                        Toast.makeText(
                            this@MainActivity,
                            "Enable notifications to receive live and upcoming match alerts.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                LaunchedEffect(Unit) {
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                var showSplash by remember { mutableStateOf(true) }
                var progress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    repeat(SPLASH_STEPS) {
                        delay(SPLASH_STEP_DELAY_MS)
                        progress = (it + 1) / SPLASH_STEPS.toFloat()
                    }
                    showSplash = false
                }

                if (showSplash) SplashScreen(progress = progress)
                else MainScreen()
            }
        }
    }
}
