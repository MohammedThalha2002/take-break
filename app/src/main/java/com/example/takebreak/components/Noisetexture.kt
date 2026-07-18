package com.example.takebreak.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
val NoiseGrain1 = RuntimeShader(
    """
    uniform float2 resolution;
    uniform shader image; 
    uniform float intensity;
    
    vec4 main( vec2 fragCoord )
    {
        vec2 uv = fragCoord/resolution.xy;
        
        // Check if pixel is inside viewport bounds
        if (fragCoord.x < 0.0 || fragCoord.x > resolution.x || fragCoord.y < 0.0 || fragCoord.y > resolution.y) {
            return vec4(image.eval(fragCoord));
        }

        float mdf = -0.8 * intensity; // increase for noise amount 
        float noise = (fract(sin(dot(uv, vec2(12.9898,78.233)*2.0)) * 43758.5453));
        vec4 tex = vec4(image.eval(fragCoord));
        
        mdf *= 1.5;
        
        vec4 col = tex - noise * mdf;

        return col;
    }
    """.trimIndent()
)


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NoiseGrainTexture(
    modifier: Modifier = Modifier,
    content : @Composable () -> Unit = {  }
) {
    var intensity by remember { mutableStateOf(0.1f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                NoiseGrain1.setFloatUniform(
                    "resolution",
                    size.width.toFloat(),
                    size.height.toFloat()
                )
            }
            .graphicsLayer {
                NoiseGrain1.setFloatUniform("intensity", intensity)
                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(
                        NoiseGrain1,
                        "image"
                    )
                    .asComposeRenderEffect()
            },
    ){
        content()
    }
}