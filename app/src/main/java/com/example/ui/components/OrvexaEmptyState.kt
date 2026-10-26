package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.ThemeMode

@Composable
fun OrvexaEmptyState(
    isSearchEmpty: Boolean = false,
    searchQuery: String = "",
    onCaptureIdea: () -> Unit,
    onLoadSampleData: (() -> Unit)? = null,
    onClearSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeMode = LocalThemeMode.current
    val isDark = themeMode == ThemeMode.DARK

    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F172A)
    val secondaryAccent = if (isDark) Color(0xFF6366F1) else Color(0xFF3B82F6)
    val pulseColor = if (isDark) Color(0x3300E5FF) else Color(0x220F172A)

    val infiniteTransition = rememberInfiniteTransition(label = "nexus_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Centered Futuristic Orvexa Brand Nexus Illustration
        Box(
            modifier = Modifier
                .size(190.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width * 0.42f

                // Outer ambient glow ring
                drawCircle(
                    color = pulseColor,
                    radius = baseRadius * pulseScale,
                    center = center
                )

                // Concentric telemetry rings
                drawCircle(
                    color = primaryAccent.copy(alpha = 0.25f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f)
                    )
                )

                drawCircle(
                    color = secondaryAccent.copy(alpha = 0.35f),
                    radius = baseRadius * 0.72f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), rotationAngle)
                    )
                )

                drawCircle(
                    color = primaryAccent.copy(alpha = 0.5f),
                    radius = baseRadius * 0.45f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Radar coordinate axes
                drawLine(
                    color = primaryAccent.copy(alpha = 0.2f),
                    start = Offset(center.x - baseRadius * 0.9f, center.y),
                    end = Offset(center.x + baseRadius * 0.9f, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = primaryAccent.copy(alpha = 0.2f),
                    start = Offset(center.x, center.y - baseRadius * 0.9f),
                    end = Offset(center.x, center.y + baseRadius * 0.9f),
                    strokeWidth = 1.dp.toPx()
                )

                // Center core node
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryAccent, secondaryAccent.copy(alpha = 0.7f)),
                        center = center,
                        radius = baseRadius * 0.28f
                    ),
                    radius = baseRadius * 0.22f,
                    center = center
                )

                // Core inner ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = baseRadius * 0.08f,
                    center = center
                )
            }

            // Central icon overlay
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f)
                        else Color.White.copy(alpha = 0.85f)
                    )
                    .border(1.dp, primaryAccent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSearchEmpty) Icons.Default.SearchOff else Icons.Default.Add,
                    contentDescription = null,
                    tint = primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Brand Sub-Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF162032) else Color(0xFFE2E8F0),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDark) primaryAccent.copy(alpha = 0.4f) else Color(0xFFCBD5E1)
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = if (isSearchEmpty) "DATABASE FILTER: ZERO MATCHES" else "ORVEXA DECISION ENGINE",
                style = MaterialTheme.typography.labelSmall,
                color = primaryAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }

        // Title
        Text(
            text = if (isSearchEmpty) "No Ideas Match Criteria" else "No Ideas Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = if (isSearchEmpty) {
                "No thesis in the Room database corresponds to \"$searchQuery\". Try adjusting keywords or clear the active filter."
            } else {
                "Your idea portfolio is currently empty. Capture an emerging market shift, technical inflection, or unaddressed customer problem into the Room database."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (isSearchEmpty) {
                Button(
                    onClick = { onClearSearch?.invoke() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryAccent,
                        contentColor = if (isDark) Color(0xFF070A0F) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clear_filter_button")
                ) {
                    Text(
                        "Clear Search Query",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Button(
                    onClick = onCaptureIdea,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryAccent,
                        contentColor = if (isDark) Color(0xFF070A0F) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("empty_state_capture_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Capture First Idea",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (onLoadSampleData != null) {
                    OutlinedButton(
                        onClick = onLoadSampleData,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("load_sample_data_button")
                    ) {
                        Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Load Sample Intelligence Ideas",
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
