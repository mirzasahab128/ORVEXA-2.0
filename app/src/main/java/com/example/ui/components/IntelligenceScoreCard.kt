package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Criteria dimension for the Intelligence Score Card
 */
data class IntelligenceDimension(
    val key: String,
    val name: String,
    val score: Int, // 0 to 100
    val weight: Float,
    val color: Color,
    val benchmark: Int = 65
)

enum class ScoreCardVisualizationMode {
    RADAR,
    PROGRESS_BARS
}

/**
 * IntelligenceScoreCard
 *
 * Dynamically visualizes idea potential based on key criteria:
 * - Market Demand
 * - Feasibility
 * - Risk Mitigation
 * - Execution Speed
 * - Financial Upside / Unit Economics
 *
 * Employs a clean Recharts-style polygon radar visualization with concentric rings,
 * radial spokes, vertices, and animated progress gauges, built on a glassmorphic surface.
 *
 * Features smooth, non-intrusive Compose Animation progress states during AI data processing,
 * including radar beam scanning, harmonic breathing glow, specular shimmer waves, and live step pills.
 */
@Composable
fun IntelligenceScoreCard(
    ideas: List<Idea>,
    modifier: Modifier = Modifier,
    initialSelectedIdeaId: String? = null,
    isExternalProcessing: Boolean = false,
    onTriggerAiEvaluation: ((Idea) -> Unit)? = null,
    isDark: Boolean = true
) {
    var selectedIdeaId by remember(ideas) {
        mutableStateOf(initialSelectedIdeaId ?: ideas.firstOrNull()?.id)
    }

    val haptic = LocalHapticFeedback.current
    var visualizationMode by remember { mutableStateOf(ScoreCardVisualizationMode.RADAR) }
    var localProcessingState by remember { mutableStateOf(false) }
    var processingStepText by remember { mutableStateOf("Auditing market demand...") }
    val scope = rememberCoroutineScope()

    val isProcessing = isExternalProcessing || localProcessingState

    val activeIdea = remember(ideas, selectedIdeaId) {
        ideas.firstOrNull { it.id == selectedIdeaId } ?: ideas.firstOrNull()
    }

    // Compute dimensions dynamically based on idea attributes
    val dimensions = remember(activeIdea) {
        if (activeIdea == null) {
            listOf(
                IntelligenceDimension("market", "Market Demand", 78, 0.25f, OrvexaIntelligentCyan),
                IntelligenceDimension("feasibility", "Feasibility", 84, 0.25f, OrvexaElectricViolet),
                IntelligenceDimension("risk", "Risk Mitigation", 62, 0.20f, OrvexaWarmOrange),
                IntelligenceDimension("speed", "Execution Speed", 75, 0.15f, ModuleExperimentsColor),
                IntelligenceDimension("economics", "Unit Economics", 80, 0.15f, ModuleInsightsColor)
            )
        } else {
            val demandScore = ((activeIdea.opportunityScore * 0.9f) + (activeIdea.feasibilityScore * 0.1f)).toInt().coerceIn(15, 98)
            val feasibilityScore = activeIdea.feasibilityScore.coerceIn(10, 99)
            val riskSeverity = activeIdea.risks.firstOrNull()?.severity ?: "Medium"
            val riskMitigationScore = when (riskSeverity.lowercase()) {
                "low" -> 88
                "medium" -> 66
                "high" -> 42
                else -> 60
            }
            val speedScore = (70 + (activeIdea.feasibilityScore - 50) / 3).coerceIn(20, 95)
            val economicsScore = ((activeIdea.opportunityScore * 0.6f) + (demandScore * 0.4f)).toInt().coerceIn(20, 96)

            listOf(
                IntelligenceDimension("market", "Market Demand", demandScore, 0.25f, OrvexaIntelligentCyan),
                IntelligenceDimension("feasibility", "Feasibility", feasibilityScore, 0.25f, OrvexaElectricViolet),
                IntelligenceDimension("risk", "Risk Mitigation", riskMitigationScore, 0.20f, OrvexaWarmOrange),
                IntelligenceDimension("speed", "Execution Speed", speedScore, 0.15f, ModuleExperimentsColor),
                IntelligenceDimension("economics", "Unit Economics", economicsScore, 0.15f, ModuleInsightsColor)
            )
        }
    }

    val overallScore = remember(dimensions) {
        dimensions.map { it.score * it.weight }.sum().toInt()
    }

    val recommendationTier = when {
        overallScore >= 80 -> "Tier 1: High Conviction Venture"
        overallScore >= 65 -> "Tier 2: Strong Venture — Test Objections"
        else -> "Tier 3: Early Hypothesis — Validate Moat"
    }

    // Compose Animation: Continuous radar scanner rotation & specular shimmer
    val infiniteTransition = rememberInfiniteTransition(label = "score_card_pulse")

    val radarBeamAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_beam"
    )

    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    val breathingGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_glow"
    )

    // Glass styling
    val cardBg = if (isDark) OrvexaDarkSurface else Color.White
    val cardBorder = if (isDark) {
        if (isProcessing) OrvexaOrange.copy(alpha = breathingGlow * 0.7f) else Color(0x2EFFFFFF)
    } else {
        if (isProcessing) OrvexaOrange.copy(alpha = 0.6f) else Color(0x33000000)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp))
            .testTag("intelligence_score_card"),
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(if (isProcessing) 1.8.dp else 1.2.dp, cardBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Optional non-intrusive specular background shimmer ribbon during AI processing
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    OrvexaOrange.copy(alpha = 0.05f),
                                    OrvexaIntelligentCyan.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startX = shimmerX * 800f,
                                endX = (shimmerX + 0.5f) * 800f
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header: Title, AI Audit action & View Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(OrvexaOrange, OrvexaElectricViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isProcessing) Icons.Default.AutoAwesome else Icons.Default.Assessment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Intelligence ScoreCard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else OrvexaDarkBackground
                                )
                                if (isProcessing) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = OrvexaIntelligentCyan.copy(alpha = breathingGlow),
                                        modifier = Modifier.size(7.dp)
                                    ) {}
                                }
                            }
                            Text(
                                text = "Multi-criteria potential & risk analysis",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // AI Re-Audit Button (Triggers AI Evaluation with Compose Animation)
                        Surface(
                            onClick = {
                                if (!isProcessing && activeIdea != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    localProcessingState = true
                                    scope.launch {
                                        processingStepText = "Auditing market demand signals..."
                                        delay(700)
                                        processingStepText = "Analyzing technical feasibility..."
                                        delay(700)
                                        processingStepText = "Simulating unit economics..."
                                        delay(700)
                                        processingStepText = "Synthesizing investment conviction..."
                                        delay(600)
                                        localProcessingState = false
                                        onTriggerAiEvaluation?.invoke(activeIdea)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isProcessing) OrvexaOrange.copy(alpha = 0.20f) else Color(0x18FFFFFF),
                            border = BorderStroke(1.dp, if (isProcessing) OrvexaOrange else Color(0x33FFFFFF)),
                            modifier = Modifier.testTag("score_card_ai_audit_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Run AI Audit",
                                    tint = if (isProcessing) OrvexaOrange else OrvexaIntelligentCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isProcessing) "Auditing" else "AI Audit",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isProcessing) OrvexaOrange else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // View Mode Segmented Switch (Radar vs Progress)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFE2E8F0),
                            border = BorderStroke(1.dp, Color(0x2EFFFFFF))
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        visualizationMode = ScoreCardVisualizationMode.RADAR
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (visualizationMode == ScoreCardVisualizationMode.RADAR) OrvexaOrange else Color.Transparent
                                        )
                                        .testTag("score_card_radar_mode_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterTiltShift,
                                        contentDescription = "Radar Chart View",
                                        tint = if (visualizationMode == ScoreCardVisualizationMode.RADAR) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        visualizationMode = ScoreCardVisualizationMode.PROGRESS_BARS
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (visualizationMode == ScoreCardVisualizationMode.PROGRESS_BARS) OrvexaOrange else Color.Transparent
                                        )
                                        .testTag("score_card_progress_mode_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatAlignLeft,
                                        contentDescription = "Progress Bars View",
                                        tint = if (visualizationMode == ScoreCardVisualizationMode.PROGRESS_BARS) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Smooth Non-Intrusive Processing Banner
                AnimatedVisibility(
                    visible = isProcessing,
                    enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x1AE4531F),
                        border = BorderStroke(1.dp, OrvexaOrange.copy(alpha = breathingGlow * 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = OrvexaOrange.copy(alpha = breathingGlow),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = processingStepText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 11.5.sp
                                )
                            }

                            Text(
                                text = "Live Gemini Analysis",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrvexaIntelligentCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Idea Picker Chip Carousel (when multiple ideas exist)
                if (ideas.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ideas.take(6).forEach { idea ->
                            val isSelected = idea.id == activeIdea?.id
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedIdeaId = idea.id
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) OrvexaOrange.copy(alpha = 0.20f) else Color(0x14FFFFFF),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) OrvexaOrange else Color(0x2EFFFFFF)
                                ),
                                modifier = Modifier.testTag("score_card_idea_chip_${idea.id}")
                            ) {
                                Text(
                                    text = idea.name.take(20),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) OrvexaOrange else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Summary Gauge Header
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x10FFFFFF),
                    border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeIdea?.name ?: "Portfolio Aggregate Potential",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = recommendationTier,
                                style = MaterialTheme.typography.bodySmall,
                                color = OrvexaOrange,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OrvexaOrange.copy(alpha = if (isProcessing) 0.35f else 0.22f),
                            border = BorderStroke(1.dp, OrvexaOrange.copy(alpha = if (isProcessing) breathingGlow else 0.5f))
                        ) {
                            Text(
                                text = "$overallScore / 100",
                                fontWeight = FontWeight.ExtraBold,
                                color = OrvexaOrange,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Dynamic Visualization: Radar or Progress Bars
                AnimatedContent(
                    targetState = visualizationMode,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    label = "score_card_vis"
                ) { mode ->
                    when (mode) {
                        ScoreCardVisualizationMode.RADAR -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                RadarPolygonChart(
                                    dimensions = dimensions,
                                    isDark = isDark,
                                    isProcessing = isProcessing,
                                    radarBeamAngle = radarBeamAngle,
                                    breathingGlow = breathingGlow,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        ScoreCardVisualizationMode.PROGRESS_BARS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                dimensions.forEach { dim ->
                                    ScoreDimensionProgressBar(
                                        dim = dim,
                                        isDark = isDark,
                                        isProcessing = isProcessing,
                                        shimmerOffset = shimmerX
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Criteria Key Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    dimensions.forEach { dim ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = dim.color,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${dim.name}: ${dim.score}%",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recharts-style Native Radar Polygon Chart for Jetpack Compose with Animated Scanner Beam
 */
@Composable
private fun RadarPolygonChart(
    dimensions: List<IntelligenceDimension>,
    isDark: Boolean,
    isProcessing: Boolean = false,
    radarBeamAngle: Float = 0f,
    breathingGlow: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    val count = dimensions.size.coerceAtLeast(3)

    // Animated score ratio interpolation
    val transitionProgress = remember { Animatable(0f) }
    LaunchedEffect(dimensions) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val gridColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
    val spokeColor = if (isDark) Color(0x22FFFFFF) else Color(0x22000000)
    val areaColor = if (isProcessing) OrvexaOrange.copy(alpha = 0.35f * breathingGlow) else OrvexaOrange.copy(alpha = 0.28f)
    val strokeColor = OrvexaOrange

    Canvas(modifier = modifier.padding(16.dp)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = (size.width.coerceAtMost(size.height) / 2f) * 0.82f

        // 1. Draw 4 concentric polygon rings (25%, 50%, 75%, 100%)
        val ringSteps = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        ringSteps.forEach { step ->
            val ringPath = Path()
            val ringRadius = maxRadius * step

            for (i in 0 until count) {
                val angle = (Math.PI * 2 / count * i - Math.PI / 2).toFloat()
                val px = centerX + ringRadius * cos(angle)
                val py = centerY + ringRadius * sin(angle)
                if (i == 0) ringPath.moveTo(px, py) else ringPath.lineTo(px, py)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = gridColor,
                style = Stroke(width = 1f, cap = StrokeCap.Round)
            )
        }

        // 2. Draw radial spokes from center to outer ring vertices
        for (i in 0 until count) {
            val angle = (Math.PI * 2 / count * i - Math.PI / 2).toFloat()
            val outerX = centerX + maxRadius * cos(angle)
            val outerY = centerY + maxRadius * sin(angle)
            drawLine(
                color = spokeColor,
                start = Offset(centerX, centerY),
                end = Offset(outerX, outerY),
                strokeWidth = 1f
            )
        }

        // 3. Draw radar data polygon
        val dataPath = Path()
        val points = mutableListOf<Offset>()

        for (i in 0 until count) {
            val dim = dimensions[i]
            val animatedScore = (dim.score.toFloat() / 100f) * transitionProgress.value
            val currentRadius = maxRadius * animatedScore
            val angle = (Math.PI * 2 / count * i - Math.PI / 2).toFloat()
            val px = centerX + currentRadius * cos(angle)
            val py = centerY + currentRadius * sin(angle)

            points.add(Offset(px, py))
            if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
        }
        dataPath.close()

        // Fill radar polygon
        drawPath(path = dataPath, color = areaColor)

        // Outline radar polygon with glowing stroke
        drawPath(
            path = dataPath,
            color = strokeColor,
            style = Stroke(width = if (isProcessing) 3.2f else 2.5f, cap = StrokeCap.Round)
        )

        // 4. Smooth non-intrusive radar beam scanner during AI processing
        if (isProcessing) {
            val beamRad = Math.toRadians(radarBeamAngle.toDouble()).toFloat()
            val beamTargetX = centerX + maxRadius * cos(beamRad)
            val beamTargetY = centerY + maxRadius * sin(beamRad)

            // Scanning beam line
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        OrvexaIntelligentCyan.copy(alpha = 0.9f),
                        OrvexaOrange.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(beamTargetX, beamTargetY)
                ),
                start = Offset(centerX, centerY),
                end = Offset(beamTargetX, beamTargetY),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Glowing center emitter
            drawCircle(
                color = OrvexaIntelligentCyan.copy(alpha = 0.7f),
                radius = 6f,
                center = Offset(centerX, centerY)
            )
        }

        // 5. Draw vertex dots and outer labels
        points.forEachIndexed { index, point ->
            val dim = dimensions[index]
            // Outer pulse when processing
            if (isProcessing) {
                drawCircle(
                    color = dim.color.copy(alpha = 0.35f * breathingGlow),
                    radius = 9f,
                    center = point
                )
            }
            // Vertex dot
            drawCircle(color = dim.color, radius = 5.5f, center = point)
            drawCircle(color = Color.White, radius = 2.5f, center = point)
        }
    }
}

/**
 * Progress Bar alternative visualization for individual criteria with shimmer flow
 */
@Composable
private fun ScoreDimensionProgressBar(
    dim: IntelligenceDimension,
    isDark: Boolean,
    isProcessing: Boolean = false,
    shimmerOffset: Float = 0f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = dim.score / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress_${dim.key}"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = dim.color,
                    modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dim.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color.Black,
                    fontSize = 12.5.sp
                )
            }

            Text(
                text = "${dim.score}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = dim.color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Multi-segment progress track with fluid shimmer animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isDark) Color(0x1AFFFFFF) else Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isProcessing) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    dim.color.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.85f),
                                    dim.color
                                ),
                                startX = shimmerOffset * 300f,
                                endX = (shimmerOffset + 0.5f) * 300f
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(dim.color.copy(alpha = 0.7f), dim.color)
                            )
                        }
                    )
            )
        }
    }
}
