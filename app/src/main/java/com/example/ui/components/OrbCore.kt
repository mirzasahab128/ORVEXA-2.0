package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 8 Canonical Orb States for ORVEXA Intelligence Core.
 */
enum class OrbState(val statusText: String) {
    IDLE("ORVEXA is ready"),
    LISTENING("ORVEXA is listening"),
    ANALYZING("ORVEXA is analyzing"),
    RESEARCHING("ORVEXA is researching"),
    COMPARING("ORVEXA is comparing"),
    VALIDATING("ORVEXA is validating"),
    SUCCESS("Analysis complete"),
    ERROR("Evaluation error")
}

/**
 * The Signature ORVEXA Orb Core.
 * Reference size: 220dp (Layout box 252dp so nothing clips).
 * Simplified mode for dock buttons (48dp).
 */
@Composable
fun OrbCore(
    state: OrbState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    rmsLevel: Float = 0f,
    enableParallax: Boolean = false,
    isSimplified: Boolean = false
) {
    val tokens = AppTheme.tokens
    val scaleFactor = size.value / 220f
    val boxSize = size * (252f / 220f)

    // Device tilt parallax (max 6 degrees, low pass filtered)
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    if (enableParallax) {
        val context = LocalContext.current
        DisposableEffect(Unit) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null && event.values.size >= 2) {
                        val rawY = event.values[0] * 12f
                        val rawX = event.values[1] * 12f
                        // Low-pass filter
                        tiltX += (rawX.coerceIn(-6f, 6f) - tiltX) * 0.15f
                        tiltY += (rawY.coerceIn(-6f, 6f) - tiltY) * 0.15f
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        }
    }

    // Infinite transitions for smooth continuous animation loops
    val transition = rememberInfiniteTransition(label = "OrbTransitions")

    // Breathing scale: Idle = 4s (1.0 to 1.045), Analyzing = 1.6s (1.0 to 1.06)
    val breathePeriod = when (state) {
        OrbState.ANALYZING -> 1600
        else -> 4000
    }
    val breatheScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state == OrbState.ANALYZING) 1.06f else 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(breathePeriod, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheScale"
    )

    // Ring 1 period: 14s default, 9s analyzing, 6s researching, 42s validating
    val r1Period = when (state) {
        OrbState.ANALYZING -> 9000
        OrbState.RESEARCHING -> 6000
        OrbState.VALIDATING -> 42000
        else -> 14000
    }
    val r1Progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(r1Period, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "R1Progress"
    )

    // Ring 2 period: 20s default, 9s researching, 42s validating
    val r2Period = when (state) {
        OrbState.RESEARCHING -> 9000
        OrbState.VALIDATING -> 42000
        else -> 20000
    }
    val r2Progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(r2Period, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "R2Progress"
    )

    // Ring 3 period: 26s default, 42s validating
    val r3Period = if (state == OrbState.VALIDATING) 42000 else 26000
    val r3Progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(r3Period, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "R3Progress"
    )

    // Scan band progress (Analyzing: 2.2s top to bottom)
    val scanProgress by transition.animateFloat(
        initialValue = -0.34f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanProgress"
    )

    // Ripples for Researching (3s, 1.0 to 1.6, fade 0.8 to 0)
    val ripple1Progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ripple1Progress"
    )
    val ripple2Progress by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ripple2Progress"
    )

    // Satellites for Comparing: Satellite 1 period 7s, Satellite 2 period 9s
    val sat1Progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Sat1Progress"
    )
    val sat2Progress by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Sat2Progress"
    )

    // Validating checkmark: draw in 1.2s, hold to 2.9s, undraw by 3.4s, loops
    val checkProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CheckProgress"
    )

    // Smoothed RMS for Listening state (80ms)
    val smoothedRms by animateFloatAsState(
        targetValue = if (state == OrbState.LISTENING) rmsLevel.coerceIn(0f, 10f) else 0f,
        animationSpec = tween(80),
        label = "SmoothedRms"
    )

    // Error shake & flicker
    val errorShake = remember { Animatable(0f) }
    LaunchedEffect(state) {
        if (state == OrbState.ERROR) {
            repeat(3) {
                errorShake.animateTo(6f, tween(70))
                errorShake.animateTo(-6f, tween(70))
            }
            errorShake.animateTo(0f, tween(60))
        }
    }

    Box(
        modifier = modifier
            .size(boxSize)
            .graphicsLayer {
                if (enableParallax) {
                    rotationX = tiltX
                    rotationY = tiltY
                    cameraDistance = 12 * density
                }
                translationX = errorShake.value
            }
            .semantics {
                contentDescription = state.statusText
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(boxSize)) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val orbRadius = (220.dp.toPx() / 2f) * scaleFactor
            val tintRadius = orbRadius - (25.dp.toPx() * scaleFactor)
            val shellRadius = orbRadius - (19.dp.toPx() * scaleFactor)

            // Layer 1: Ground shadow (Morning only: 200x26dp ellipse under orb)
            if (tokens.hasGroundShadow) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(tokens.groundShadowColor, Color.Transparent),
                        center = Offset(centerOffset.x, centerOffset.y + orbRadius + 8.dp.toPx() * scaleFactor),
                        radius = 100.dp.toPx() * scaleFactor
                    ),
                    topLeft = Offset(
                        centerOffset.x - 100.dp.toPx() * scaleFactor,
                        centerOffset.y + orbRadius - 5.dp.toPx() * scaleFactor
                    ),
                    size = Size(200.dp.toPx() * scaleFactor, 26.dp.toPx() * scaleFactor)
                )
            }

            // Layer 2: Ambient Drop Shadow / Glow
            val glowColor = getOrbGlowColor(state, tokens)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = tokens.orbGlowAlpha), Color.Transparent),
                    center = centerOffset,
                    radius = orbRadius * 1.35f
                ),
                radius = orbRadius * 1.35f,
                center = centerOffset
            )

            // Layer 3: Tint Sphere with Radial Gradient
            val currentScale = if (state == OrbState.LISTENING) {
                1.0f + (smoothedRms * 0.015f)
            } else if (state == OrbState.SUCCESS) {
                1.08f
            } else {
                breatheScale
            }

            val sphereGradient = getOrbSphereBrush(state, centerOffset, tintRadius * currentScale)
            drawCircle(
                brush = sphereGradient,
                radius = tintRadius * currentScale,
                center = centerOffset
            )

            // Inner shadow for depth (bottom right, 50% opacity in Idle)
            if (state == OrbState.IDLE) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.50f)),
                        center = Offset(centerOffset.x - tintRadius * 0.2f, centerOffset.y - tintRadius * 0.2f),
                        radius = tintRadius * currentScale
                    ),
                    radius = tintRadius * currentScale,
                    center = centerOffset
                )
            }

            // Layer 4: Scan Band (Analyzing state: 34% of sphere height)
            if (state == OrbState.ANALYZING && !isSimplified) {
                val bandHeight = tintRadius * 2f * 0.34f
                val bandTop = centerOffset.y - tintRadius + (scanProgress * tintRadius * 2f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent),
                        startY = bandTop,
                        endY = bandTop + bandHeight
                    ),
                    topLeft = Offset(centerOffset.x - tintRadius, bandTop),
                    size = Size(tintRadius * 2f, bandHeight)
                )
            }

            // Layer 5: Shell (Border + Specular gradient)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(tokens.shellFillTop, Color.Transparent, tokens.shellFillBottom),
                    start = Offset(centerOffset.x - shellRadius, centerOffset.y - shellRadius),
                    end = Offset(centerOffset.x + shellRadius, centerOffset.y + shellRadius)
                ),
                radius = shellRadius,
                center = centerOffset
            )
            drawCircle(
                color = tokens.shellBorder,
                radius = shellRadius,
                center = centerOffset,
                style = Stroke(width = 1.2.dp.toPx() * scaleFactor)
            )

            // Layer 6: Rings with Beads
            // Ring 1 (r1): vertical axis scaled to 0.28 (rotateX 74deg equivalent)
            drawRing1(
                center = centerOffset,
                radius = orbRadius,
                progress = r1Progress,
                strokeColor = tokens.ringStrokeR1,
                scaleFactor = scaleFactor
            )

            if (!isSimplified) {
                // Ring 2 (r2): horizontal axis scaled to 0.28, inset -10dp
                val r2Radius = orbRadius + 10.dp.toPx() * scaleFactor
                drawRing2(
                    center = centerOffset,
                    radius = r2Radius,
                    progress = r2Progress,
                    strokeColor = tokens.ringStrokeR2,
                    scaleFactor = scaleFactor
                )

                // Ring 3 (r3): dashed circle, tilted 40 deg, inset 7dp
                val r3Radius = orbRadius - 7.dp.toPx() * scaleFactor
                drawRing3(
                    center = centerOffset,
                    radius = r3Radius,
                    progress = r3Progress,
                    strokeColor = tokens.ringStrokeR3,
                    scaleFactor = scaleFactor
                )
            }

            // Layer 7: Satellites in Comparing state
            if (state == OrbState.COMPARING && !isSimplified) {
                drawSatellites(
                    center = centerOffset,
                    orbRadius = orbRadius,
                    sat1Progress = sat1Progress,
                    sat2Progress = sat2Progress,
                    scaleFactor = scaleFactor
                )
            }

            // Layer 8: Ripples in Researching state
            if (state == OrbState.RESEARCHING && !isSimplified) {
                drawRipples(centerOffset, tintRadius, ripple1Progress, ripple2Progress)
            }

            // Layer 9: Checkmark in Validating state
            if (state == OrbState.VALIDATING && !isSimplified) {
                drawCheckmark(centerOffset, checkProgress, scaleFactor)
            }

            // Layer 10: Listening Pulse Ring
            if (state == OrbState.LISTENING && smoothedRms > 1.5f) {
                val pulseRadius = shellRadius + (smoothedRms * 3.5f * scaleFactor)
                drawCircle(
                    color = Color(0xFFE4531F).copy(alpha = 0.45f),
                    radius = pulseRadius,
                    center = centerOffset,
                    style = Stroke(width = 2.dp.toPx() * scaleFactor)
                )
            }
        }
    }
}

// Helpers for Drawing Rings, Beads, Satellites, Ripples, and Checkmark

private fun DrawScope.drawRing1(
    center: Offset,
    radius: Float,
    progress: Float,
    strokeColor: Color,
    scaleFactor: Float
) {
    val hRadius = radius
    val vRadius = radius * 0.28f

    // Draw elliptical path
    drawOval(
        color = strokeColor,
        topLeft = Offset(center.x - hRadius, center.y - vRadius),
        size = Size(hRadius * 2f, vRadius * 2f),
        style = Stroke(width = 1.4.dp.toPx() * scaleFactor)
    )

    // Bead (6dp white 90% traveling along it)
    val angle = progress * 2f * PI.toFloat()
    val beadX = center.x + (hRadius * cos(angle))
    val beadY = center.y + (vRadius * sin(angle))
    drawCircle(
        color = Color.White.copy(alpha = 0.90f),
        radius = 3.dp.toPx() * scaleFactor,
        center = Offset(beadX, beadY)
    )
}

private fun DrawScope.drawRing2(
    center: Offset,
    radius: Float,
    progress: Float,
    strokeColor: Color,
    scaleFactor: Float
) {
    val hRadius = radius * 0.28f
    val vRadius = radius

    drawOval(
        color = strokeColor,
        topLeft = Offset(center.x - hRadius, center.y - vRadius),
        size = Size(hRadius * 2f, vRadius * 2f),
        style = Stroke(width = 1.2.dp.toPx() * scaleFactor)
    )

    // Bead traveling opposite way
    val angle = (1f - progress) * 2f * PI.toFloat()
    val beadX = center.x + (hRadius * cos(angle))
    val beadY = center.y + (vRadius * sin(angle))
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = 3.dp.toPx() * scaleFactor,
        center = Offset(beadX, beadY)
    )
}

private fun DrawScope.drawRing3(
    center: Offset,
    radius: Float,
    progress: Float,
    strokeColor: Color,
    scaleFactor: Float
) {
    rotate(degrees = 40f + (progress * 360f), pivot = center) {
        drawCircle(
            color = strokeColor,
            radius = radius * 0.77f,
            center = center,
            style = Stroke(
                width = 1.2.dp.toPx() * scaleFactor,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
        )
    }
}

private fun DrawScope.drawSatellites(
    center: Offset,
    orbRadius: Float,
    sat1Progress: Float,
    sat2Progress: Float,
    scaleFactor: Float
) {
    // Satellite 1: orbits at 104% radius, 7s period
    val r1 = orbRadius * 1.04f
    val a1 = sat1Progress * 2f * PI.toFloat()
    val s1X = center.x + (r1 * cos(a1))
    val s1Y = center.y + (r1 * sin(a1))

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFFF9A4D), Color(0xFFC2185B)),
            center = Offset(s1X, s1Y),
            radius = 6.dp.toPx() * scaleFactor
        ),
        radius = 6.dp.toPx() * scaleFactor,
        center = Offset(s1X, s1Y)
    )

    // Satellite 2: orbits opposite at 88% radius, 9s period
    val r2 = orbRadius * 0.88f
    val a2 = sat2Progress * 2f * PI.toFloat()
    val s2X = center.x + (r2 * cos(a2))
    val s2Y = center.y + (r2 * sin(a2))

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFFF9A4D), Color(0xFFC2185B)),
            center = Offset(s2X, s2Y),
            radius = 5.dp.toPx() * scaleFactor
        ),
        radius = 5.dp.toPx() * scaleFactor,
        center = Offset(s2X, s2Y)
    )
}

private fun DrawScope.drawRipples(
    center: Offset,
    baseRadius: Float,
    p1: Float,
    p2: Float
) {
    val normP1 = (p1 % 1f)
    val r1 = baseRadius * (1.0f + (0.6f * normP1))
    val alpha1 = (0.75f * (1f - normP1)).coerceIn(0f, 0.75f)

    drawCircle(
        color = Color(0xFF1FB6E8).copy(alpha = alpha1),
        radius = r1,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    val normP2 = (p2 % 1f)
    val r2 = baseRadius * (1.0f + (0.6f * normP2))
    val alpha2 = (0.75f * (1f - normP2)).coerceIn(0f, 0.75f)

    drawCircle(
        color = Color(0xFF1FB6E8).copy(alpha = alpha2),
        radius = r2,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawCheckmark(
    center: Offset,
    progress: Float,
    scaleFactor: Float
) {
    // 0 to 0.35: draw in, 0.35 to 0.85: hold, 0.85 to 1.0: undraw
    val drawFactor = when {
        progress < 0.35f -> progress / 0.35f
        progress < 0.85f -> 1.0f
        else -> 1.0f - ((progress - 0.85f) / 0.15f)
    }

    if (drawFactor > 0.05f) {
        val path = Path().apply {
            moveTo(center.x - 22.dp.toPx() * scaleFactor, center.y + 2.dp.toPx() * scaleFactor)
            lineTo(
                center.x - 6.dp.toPx() * scaleFactor,
                center.y + 18.dp.toPx() * scaleFactor
            )
            lineTo(
                center.x + 24.dp.toPx() * scaleFactor,
                center.y - 18.dp.toPx() * scaleFactor
            )
        }
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(
                width = 5.dp.toPx() * scaleFactor,
                cap = StrokeCap.Round
            )
        )
    }
}

private fun getOrbGlowColor(state: OrbState, tokens: com.example.ui.theme.ThemeTokens): Color {
    return when (state) {
        OrbState.ANALYZING, OrbState.IDLE -> tokens.modeAnalyze
        OrbState.RESEARCHING -> tokens.modeResearch
        OrbState.COMPARING -> tokens.modeCompare
        OrbState.VALIDATING -> tokens.modeValidate
        OrbState.LISTENING -> tokens.modeListening
        OrbState.ERROR -> tokens.modeError
        OrbState.SUCCESS -> Color.White
    }
}

private fun getOrbSphereBrush(state: OrbState, center: Offset, radius: Float): Brush {
    val lightCenter = Offset(center.x - (radius * 0.36f), center.y - (radius * 0.48f))

    val colors = when (state) {
        OrbState.ANALYZING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFA8C8FF),
            Color(0xFF6A7DFF),
            Color(0xFF1B1470)
        )
        OrbState.RESEARCHING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFA6FFF4),
            Color(0xFF1FB6E8),
            Color(0xFF08305E)
        )
        OrbState.COMPARING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFC2E6),
            Color(0xFFFF7A6E),
            Color(0xFF5A1250)
        )
        OrbState.VALIDATING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFC4FFD9),
            Color(0xFF35C987),
            Color(0xFF0A4A38)
        )
        OrbState.LISTENING -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFD2BD),
            Color(0xFFE4531F),
            Color(0xFF5A1A06)
        )
        OrbState.ERROR -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFE2A0),
            Color(0xFFFFB020),
            Color(0xFF6A3A00)
        )
        OrbState.SUCCESS -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFFC4FFD9),
            Color(0xFF6A7DFF),
            Color(0xFF1B1470)
        )
        OrbState.IDLE -> listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.60f),
            Color(0xFFA8C8FF).copy(alpha = 0.60f),
            Color(0xFF6A7DFF).copy(alpha = 0.60f),
            Color(0xFF1B1470).copy(alpha = 0.60f)
        )
    }

    return Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to colors[0],
            0.14f to colors[1],
            0.48f to colors[2],
            1.00f to colors[3]
        ),
        center = lightCenter,
        radius = radius * 1.4f
    )
}
