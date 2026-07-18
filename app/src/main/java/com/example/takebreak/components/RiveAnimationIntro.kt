package com.example.takebreak.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import com.example.takebreak.R
import kotlinx.coroutines.delay

// loaders
// 1. https://rive.app/marketplace/6992-13438-moving-shapes-001/
// 2. https://rive.app/marketplace/3703-7734-fire-button/ -- opening apps in night time
// 3. https://rive.app/marketplace/4366-10188-forgot-passoprt/
@Composable
fun RiveAnimationIntro(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    val abstractRiv = R.raw.breathing

    LaunchedEffect(Unit) {
        delay(1000) // Small delay for smoother animation
        isVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AndroidView(
                factory = { context ->
                    RiveAnimationView(context).also {
                        it.setRiveResource(
                            resId = abstractRiv,
                            autoplay = true,
                        )
                    }
                },
            )
            Text(
                text = "Instead, take 2 minutes for breathing?",
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp),
                fontSize = 12.sp
            )
            AnimatedVisibility(
                visible = isVisible
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .then(Modifier.size(50.dp))
                            .border(1.dp, Color.Red, shape = CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "content description",
                            tint = Color.Red
                        )
                    }
                    IconButton(
                        onClick = onContinue,
                        modifier = Modifier
                            .then(Modifier.size(50.dp))
                            .border(1.dp, Color.Green, shape = CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "content description",
                            tint = Color.Green
                        )
                    }
                }
            }
        }
    }
}