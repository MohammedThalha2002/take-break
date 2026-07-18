package com.example.takebreak.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import com.example.takebreak.ui.theme.Blue400
import com.example.takebreak.ui.theme.Rose400
import com.example.takebreak.ui.theme.Slate100

@Composable
fun Modifier.meshGradient(
    points: List<List<Pair<Offset, Color>>>,
    resolutionX: Int = 1,
    resolutionY: Int = 1,
    showPoints: Boolean = false,
    indicesModifier: (List<Int>) -> List<Int> = { it }
): Modifier {
    val pointData by remember(points, resolutionX, resolutionY) {
        derivedStateOf {
            PointData(points, resolutionX, resolutionY)
        }
    }

    return drawBehind {
        drawIntoCanvas { canvas ->
            scale(
                scaleX = size.width,
                scaleY = size.height,
                pivot = Offset.Zero
            ) {
                canvas.drawVertices(
                    vertices = Vertices(
                        vertexMode = VertexMode.Triangles,
                        positions = pointData.offsets,
                        textureCoordinates = pointData.offsets,
                        colors = pointData.colors,
                        indices = indicesModifier(pointData.indices)
                    ),
                    blendMode = BlendMode.Dst,
                    paint = paint,
                )
            }


            if (showPoints) {
                val flattenedPaint = Paint()
                flattenedPaint.color = Color.White.copy(alpha = .9f)
                flattenedPaint.strokeWidth = 4f * .001f
                flattenedPaint.strokeCap = StrokeCap.Round
                flattenedPaint.blendMode = BlendMode.SrcOver

                scale(
                    scaleX = size.width,
                    scaleY = size.height,
                    pivot = Offset.Zero
                ) {
                    canvas.drawPoints(
                        pointMode = PointMode.Points,
                        points = pointData.offsets,
                        paint = flattenedPaint
                    )
                }
            }
        }
    }
}


class PointData(
    private val points: List<List<Pair<Offset, Color>>>,
    private val stepsX: Int,
    private val stepsY: Int,
) {
    val offsets: MutableList<Offset>
    val colors: MutableList<Color>
    val indices: List<Int>
    private val xLength: Int = (points[0].size * stepsX) - (stepsX - 1)
    private val yLength: Int = (points.size * stepsY) - (stepsY - 1)
    private val measure = PathMeasure()

    private val indicesBlocks: List<IndicesBlock>

    init {
        offsets = buildList {
            repeat((xLength - 0) * (yLength - 0)) {
                add(Offset(0f, 0f))
            }
        }.toMutableList()

        colors = buildList {
            repeat((xLength - 0) * (yLength - 0)) {
                add(Color.Transparent)
            }
        }.toMutableList()

        indicesBlocks =
            buildList {
                for (y in 0..yLength - 2) {
                    for (x in 0..xLength - 2) {

                        val a = (y * xLength) + x
                        val b = a + 1
                        val c = ((y + 1) * xLength) + x
                        val d = c + 1

                        add(
                            IndicesBlock(
                                indices = buildList {
                                    add(a)
                                    add(c)
                                    add(d)

                                    add(a)
                                    add(b)
                                    add(d)
                                },
                                x = x, y = y
                            )
                        )

                    }
                }
            }

        indices = indicesBlocks.flatMap { it.indices }
        generateInterpolatedOffsets()
    }

    private fun generateInterpolatedOffsets() {
        for (y in 0..points.lastIndex) {
            for (x in 0..points[y].lastIndex) {
                this[x * stepsX, y * stepsY] = points[y][x].first
                this[x * stepsX, y * stepsY] = points[y][x].second

                if (x != points[y].lastIndex) {
                    val path = cubicPathX(
                        point1 = points[y][x].first,
                        point2 = points[y][x + 1].first,
                        when (x) {
                            0 -> 0
                            points[y].lastIndex - 1 -> 2
                            else -> 1
                        }
                    )
                    measure.setPath(path, false)

                    for (i in 1..<stepsX) {
                        measure.getPosition(i / stepsX.toFloat() * measure.length).let {
                            this[(x * stepsX) + i, (y * stepsY)] = Offset(it.x, it.y)
                            this[(x * stepsX) + i, (y * stepsY)] =
                                lerp(
                                    points[y][x].second,
                                    points[y][x + 1].second,
                                    i / stepsX.toFloat(),
                                )
                        }
                    }
                }
            }
        }

        for (y in 0..<points.lastIndex) {
            for (x in 0..<this.xLength) {
                val path = cubicPathY(
                    point1 = this[x, y * stepsY].let { Offset(it.x, it.y) },
                    point2 = this[x, (y + 1) * stepsY].let { Offset(it.x, it.y) },
                    when (y) {
                        0 -> 0
                        points[y].lastIndex - 1 -> 2
                        else -> 1
                    }
                )
                measure.setPath(path, false)
                for (i in (1..<stepsY)) {
                    val point3 = measure.getPosition(i / stepsY.toFloat() * measure.length).let {
                        Offset(it.x, it.y)
                    }

                    this[x, ((y * stepsY) + i)] = point3

                    this[x, ((y * stepsY) + i)] = lerp(
                        this.getColor(x, y * stepsY),
                        this.getColor(x, (y + 1) * stepsY),
                        i / stepsY.toFloat(),
                    )

                }
            }
        }
    }

    data class IndicesBlock(val indices: List<Int>, val x: Int, val y: Int)

    operator fun get(x: Int, y: Int): Offset {
        val index = (y * xLength) + x
        return offsets[index]
    }

    private fun getColor(x: Int, y: Int): Color {
        val index = (y * xLength) + x
        return colors[index]
    }

    private operator fun set(x: Int, y: Int, offset: Offset) {
        val index = (y * xLength) + x
        offsets[index] = Offset(offset.x, offset.y)
    }

    private operator fun set(x: Int, y: Int, color: Color) {
        val index = (y * xLength) + x
        colors[index] = color
    }
}


private fun cubicPathX(point1: Offset, point2: Offset, position: Int): Path {
    val path = Path().apply {
        moveTo(point1.x, point1.y)
        val delta = (point2.x - point1.x) * .5f
        when (position) {
            0 -> cubicTo(
                point1.x, point1.y,
                point2.x - delta, point2.y,
                point2.x, point2.y
            )

            2 -> cubicTo(
                point1.x + delta, point1.y,
                point2.x, point2.y,
                point2.x, point2.y
            )

            else -> cubicTo(
                point1.x + delta, point1.y,
                point2.x - delta, point2.y,
                point2.x, point2.y
            )
        }

        lineTo(point2.x, point2.y)
    }
    return path
}

private fun cubicPathY(point1: Offset, point2: Offset, position: Int): Path {
    val path = Path().apply {
        moveTo(point1.x, point1.y)
        val delta = (point2.y - point1.y) * .5f
        when (position) {
            0 -> cubicTo(
                point1.x, point1.y,
                point2.x, point2.y - delta,
                point2.x, point2.y
            )

            2 -> cubicTo(
                point1.x, point1.y + delta,
                point2.x, point2.y,
                point2.x, point2.y
            )

            else -> cubicTo(
                point1.x, point1.y + delta,
                point2.x, point2.y - delta,
                point2.x, point2.y
            )
        }

        lineTo(point2.x, point2.y)
    }
    return path
}

private val paint = Paint()


@SuppressLint("RestrictedApi")
@Composable
fun MeshGradientBox(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
//    val coefficient by infinite.animateFloat(
//        initialValue = 0f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = 4000, easing = EaseInOut),
//            repeatMode = RepeatMode.Reverse
//        )
//    )

    val coefficient = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coefficient.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4000, easing = EaseInOut)
        )
        coefficient.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 4000, easing = EaseInOut)
        )
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .meshGradient(
                showPoints = false,
                points = listOf(
                    listOf(
                        Offset(-0.3f, -0.3f) to Blue400,
                        Offset(0.5f, -0.3f) to Blue400,
                        Offset(1.3f, -0.3f) to Blue400,
                    ),
                    listOf(
                        Offset(-0.3f, lerp(0.0f, -0.6f, coefficient.value)) to Blue400,
                        Offset(0.5f, lerp(0.2f, -0.8f, coefficient.value)) to Blue400,
                        Offset(1.3f, lerp(0.0f, -0.6f, coefficient.value)) to Blue400,
                    ),
                    listOf(
                        Offset(-0.3f, lerp(0.5f, -0.3f, coefficient.value)) to Blue400,
                        Offset(0.35f, lerp(0.3f, -0.6f, coefficient.value)) to Blue400,
                        Offset(1.3f, lerp(0.5f, -0.3f, coefficient.value)) to Blue400,
                    ),
                    listOf(
                        Offset(-0.3f, lerp(0.8f, 0.0f, coefficient.value)) to Slate100,
                        Offset(0.5f, lerp(0.7f, 0.1f, coefficient.value)) to Slate100,
                        Offset(1.3f, lerp(0.8f, 0.0f, coefficient.value)) to Slate100,
                    ),
                    listOf(
                        Offset(-0.3f, lerp(1.2f, 1f, coefficient.value)) to Rose400,
                        Offset(0.3f, lerp(1.1f, 1f, coefficient.value)) to Rose400,
                        Offset(1.3f, lerp(1.2f, 1f, coefficient.value)) to Rose400,
                    ),
                    listOf(
                        Offset(-0.3f, 1.1f) to Rose400,
                        Offset(0.5f, 1.15f) to Rose400,
                        Offset(1.3f, 1.1f) to Rose400,
                    ),
                ),
                resolutionX = 24,
                resolutionY = 24
            )
    )
}