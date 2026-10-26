package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Visual gauge component that displays Feasibility and Innovation scores
 * on idea cards with animated color arcs and responsive density.
 */
@Composable
fun ScoreVisualGauge(
    score: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    strokeWidth: Dp = 4.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    val targetFraction = (score.coerceIn(0, 100)) / 100f

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = targetFraction,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val gaugeColor = when {
        score >= 75 -> Color(0xFF10B981) // Emerald Green
        score >= 50 -> Color(0xFF06B6D4) // Cyan / Blue
        score >= 35 -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444)        // Coral Red
    }

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated progress arc
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedProgress.value * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Dual visual gauge badge displaying both Feasibility and Innovation scores
 * side-by-side with semantic icons for quick visual scanning on Idea cards.
 */
@Composable
fun DualScoreGaugeBadge(
    feasibilityScore: Int,
    innovationScore: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Feasibility Gauge
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScoreVisualGauge(
                    score = feasibilityScore,
                    label = "Feasibility",
                    size = 38.dp,
                    strokeWidth = 3.5.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Feasibility",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (feasibilityScore >= 70) "High" else if (feasibilityScore >= 45) "Moderate" else "Challenging",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 24.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            // Innovation Gauge
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScoreVisualGauge(
                    score = innovationScore,
                    label = "Innovation",
                    size = 38.dp,
                    strokeWidth = 3.5.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF8B5CF6)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Innovation",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (innovationScore >= 75) "Breakthrough" else if (innovationScore >= 50) "Differentiated" else "Incremental",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
