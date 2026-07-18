package com.example.takebreak

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takebreak.components.MeshGradientBox
import com.example.takebreak.components.NoiseGrainTexture
import com.example.takebreak.ui.theme.TakeBreakTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class InterceptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val packageName = intent.getStringExtra("packageName") ?: return

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
            TakeBreakTheme {
                Scaffold { padding ->
                    InterstitialScreen(
                        appName = getAppNameFromPackage(packageName),
                        modifier = Modifier
                            .fillMaxSize(),
                        onContinue = {
                            launchApp(this)
                        },
                        onClose = {
                            finishAffinity() // Closes the screen
                            val homeIntent = Intent(Intent.ACTION_MAIN)
                            homeIntent.addCategory(Intent.CATEGORY_HOME)
                            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(homeIntent)
                        }
                    )

                }
            }
        }
    }

    private fun getAppNameFromPackage(pkg: String): String {
        return when (pkg) {
            "com.instagram.android" -> "Instagram"
            "com.google.android.youtube" -> "YouTube"
            else -> "App"
        }
    }
}

@Preview
@Composable
fun InterstitialScreen(
    appName: String = "App",
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var isBreathingTextVisible by remember { mutableStateOf(0) }
    var isBreathingIn by remember { mutableStateOf(true) }
    var isButtonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            isButtonsVisible = true
            delay(6000)
            isBreathingIn = false
        }
        launch {
            delay(8000)
            isBreathingTextVisible = 2
        }
        launch {
            delay(500)
            isBreathingTextVisible = 1
        }
    }

    BackHandler {
        onClose()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NoiseGrainTexture {
                MeshGradientBox()
            }
        } else {
            MeshGradientBox()
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = isBreathingTextVisible == 1,
                enter = fadeIn(tween(1000, delayMillis = 500)),
                exit = fadeOut(tween(1000))
            ) {
                AnimatedContent(
                    targetState = isBreathingIn,
                    transitionSpec = {
                        fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000)))
                    },
                    label = "Breathing Text"
                ) { visible ->
                    Text(
                        text = if (visible) "Breathe in" else "Breathe out",
                        color = Color.White
                    )
                }
            }
            AnimatedVisibility(
                visible = isButtonsVisible,
                enter = fadeIn(tween(1000, delayMillis = 8000))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "`Bored? Curious? Lonely? Or just routine?`",
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .then(Modifier.size(50.dp))
                                .border(1.dp, Color.White, shape = CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "content description",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = onContinue,
                            modifier = Modifier
                                .then(Modifier.size(50.dp))
                                .border(1.dp, Color.White, shape = CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "content description",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

fun launchApp(activity: Activity) {
    InterceptControl.isTemporarilyAllowed = true

    activity.finish()

    Handler(Looper.getMainLooper()).postDelayed({
        InterceptControl.isTemporarilyAllowed = false
    }, 1500)
}

object InterceptControl {
    @Volatile
    var isTemporarilyAllowed = false
}