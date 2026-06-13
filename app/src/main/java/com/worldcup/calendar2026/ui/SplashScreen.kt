package com.worldcup.calendar2026.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.worldcup.calendar2026.R

private val SplashOverlay = Color(0xFF031740)

@Composable
fun SplashScreen(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashOverlay)
    ) {
        AsyncImage(
            model = stringResource(R.string.splash_image_url),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.85f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SplashOverlay.copy(alpha = 0.53f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Image(
                painter = painterResource(R.drawable.ic_worldcup_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 28.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
