package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.util.HapticManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen backdrop drawing page base + 3 radial gradient glows without Modifier.blur.
 */
@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = AppTheme.tokens

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Base background fill
                if (tokens.isPageGradient && tokens.pageGradientStops.size >= 3) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = tokens.pageGradientStops,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                } else {
                    drawRect(color = tokens.pageBackground)
                }

                // Glow 1: Top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tokens.glowTopLeft, Color.Transparent),
                        center = Offset(0f, -size.height * 0.05f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(0f, -size.height * 0.05f)
                )

                // Glow 2: Right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tokens.glowRight, Color.Transparent),
                        center = Offset(size.width * 1.05f, size.height * 0.22f),
                        radius = size.width * 0.80f
                    ),
                    radius = size.width * 0.80f,
                    center = Offset(size.width * 1.05f, size.height * 0.22f)
                )

                // Glow 3: Bottom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tokens.glowBottom, Color.Transparent),
                        center = Offset(size.width * 0.40f, size.height * 0.95f),
                        radius = size.width * 0.90f
                    ),
                    radius = size.width * 0.90f,
                    center = Offset(size.width * 0.40f, size.height * 0.95f)
                )
            }
    ) {
        content()
    }
}

/**
 * Reusable GlassCard: 28dp radius (or custom), 160-deg gradient fill,
 * 1dp border, inner top & bottom highlights, and realistic spatial shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    enablePressTilt: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val tokens = AppTheme.tokens
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val tiltModifier = if (enablePressTilt) {
        Modifier.graphicsLayer {
            val tiltTarget = if (isPressed) 4f else 0f
            rotationX = tiltTarget
            cameraDistance = 12 * density
        }
    } else Modifier

    val clickableMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .then(tiltModifier)
            .shadow(
                elevation = tokens.glassShadowOffsetY,
                shape = shape,
                ambientColor = tokens.glassShadow,
                spotColor = tokens.glassShadow
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(tokens.glassFillTop, tokens.glassFillBottom),
                    start = Offset(0f, 0f),
                    end = Offset(400f, 1000f) // ~160 degrees
                )
            )
            .border(width = 1.dp, color = tokens.glassBorder, shape = shape)
            .drawBehind {
                // Inner top highlight 1dp
                drawLine(
                    color = tokens.glassInnerTopHighlight,
                    start = Offset(16.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width - 16.dp.toPx(), 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                // Inner bottom line
                drawLine(
                    color = tokens.glassInnerBottomLine,
                    start = Offset(24.dp.toPx(), size.height - 1.dp.toPx()),
                    end = Offset(size.width - 24.dp.toPx(), size.height - 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .then(clickableMod)
    ) {
        content()
    }
}

/**
 * Fully rounded Glass Pill (CircleShape) with spatial specular borders.
 */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 999.dp,
        onClick = onClick,
        content = content
    )
}

/**
 * Top-sheet or bottom-sheet glass container with top corner radius 36dp.
 */
@Composable
fun GlassSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = AppTheme.tokens
    val shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 24.dp, shape = shape, spotColor = tokens.glassShadow)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(tokens.glassFillTop, tokens.glassFillBottom)
                )
            )
            .border(width = 1.dp, color = tokens.glassBorder, shape = shape)
    ) {
        content()
    }
}

/**
 * Fully rounded chip with custom/selected state.
 */
@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    hapticManager: HapticManager? = null
) {
    val tokens = AppTheme.tokens
    val fill = if (selected) tokens.selectedPillFill else tokens.inputChipFill
    val borderCol = if (selected) tokens.selectedPillHighlight else tokens.inputChipBorder
    val textCol = if (selected) tokens.textPrimary else tokens.textSecondary

    Surface(
        modifier = modifier
            .clickable {
                hapticManager?.performTick()
                onClick()
            },
        shape = CircleShape,
        color = fill,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textCol,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = textCol
            )
        }
    }
}

/**
 * Primary or Ghost glass button adhering to theme tokens.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    leadingIcon: ImageVector? = null,
    hapticManager: HapticManager? = null
) {
    val tokens = AppTheme.tokens
    val bgCol = if (isPrimary) tokens.primaryButtonFill else tokens.inputChipFill
    val textCol = if (isPrimary) tokens.primaryButtonText else tokens.textPrimary
    val borderCol = if (isPrimary) Color.Transparent else tokens.glassBorder

    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable {
                hapticManager?.performClick()
                onClick()
            },
        shape = CircleShape,
        color = bgCol,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textCol,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = textCol
            )
        }
    }
}

/**
 * Glass input text field with placeholder, microphone/clear action, and 56dp glass pill height.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingAction: (@Composable () -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    val tokens = AppTheme.tokens
    val shape = CircleShape

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(tokens.inputChipFill)
            .border(1.dp, tokens.inputChipBorder, shape)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = tokens.textSecondary.copy(alpha = 0.6f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = tokens.textPrimary
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(tokens.primaryButtonFill),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onDone?.invoke() })
                )
            }
            if (trailingAction != null) {
                trailingAction()
            }
        }
    }
}

/**
 * 4-Segment Mode Selector: Analyze, Research, Compare, Validate.
 * Fits in 350dp, radio group semantics, tick haptic on change.
 */
enum class OrbMode(val label: String, val title: String, val desc: String) {
    ANALYZE("Analyze", "Analyzing your idea", "Breaking the idea into problem, customer, and revenue model."),
    RESEARCH("Research", "Researching the market", "Pulling sources, competitor signals, and customer discussions."),
    COMPARE("Compare", "Comparing alternatives", "Placing your idea next to the closest existing options."),
    VALIDATE("Validate", "Validating assumptions", "Testing each claim against the evidence collected.")
}

@Composable
fun SegmentedModes(
    selectedMode: OrbMode,
    onModeSelected: (OrbMode) -> Unit,
    modifier: Modifier = Modifier,
    hapticManager: HapticManager? = null
) {
    val tokens = AppTheme.tokens
    val shape = CircleShape

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(tokens.inputChipFill)
            .border(1.dp, tokens.inputChipBorder, shape)
            .padding(4.dp)
            .selectableGroup()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrbMode.values().forEach { mode ->
                val isSelected = mode == selectedMode
                val bgCol = if (isSelected) tokens.selectedPillFill else Color.Transparent
                val borderCol = if (isSelected) tokens.selectedPillHighlight else Color.Transparent
                val textCol = if (isSelected) tokens.textPrimary else tokens.textSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(shape)
                        .background(bgCol)
                        .border(1.dp, borderCol, shape)
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = {
                                if (!isSelected) {
                                    hapticManager?.performTick()
                                    onModeSelected(mode)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = textCol,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Concentric Ring Meter for Portfolio Pulse (Validation, Execution, Readiness).
 */
@Composable
fun RingMeter(
    progressValues: List<Float>, // 3 values from 0f to 1f
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 6.dp
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val spacingPx = strokePx * 1.5f

            progressValues.forEachIndexed { index, prog ->
                val inset = index * spacingPx + strokePx / 2f
                val ringDiameter = size.toPx() - (inset * 2f)
                if (ringDiameter > 0) {
                    val topLeft = Offset(inset, inset)
                    val arcSize = Size(ringDiameter, ringDiameter)
                    val ringColor = colors.getOrElse(index) { Color.Cyan }

                    // Track
                    drawArc(
                        color = ringColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    // Progress
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * prog.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

/**
 * Bento Stat Tile with large numeral (32-48sp semibold, tabular figures).
 */
@Composable
fun StatTile(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val tokens = AppTheme.tokens

    GlassCard(
        modifier = modifier,
        enablePressTilt = true,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.16.sp
                    ),
                    color = tokens.textLabel
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tokens.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Default
                ),
                color = tokens.textPrimary
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = tokens.textSecondary
                )
            }
        }
    }
}

/**
 * Reasoning Log Step Item: 4 states (pending, active spinner, done check, failed).
 */
enum class StepStatus {
    PENDING, ACTIVE, DONE, FAILED
}

@Composable
fun ReasoningStep(
    stepNumber: Int,
    text: String,
    status: StepStatus,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tokens = AppTheme.tokens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tokens.inputChipFill)
            .border(1.dp, tokens.inputChipBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when (status) {
                        StepStatus.DONE -> tokens.modeValidate.copy(alpha = 0.2f)
                        StepStatus.FAILED -> tokens.modeError.copy(alpha = 0.2f)
                        else -> tokens.selectedPillFill
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                StepStatus.PENDING -> {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = tokens.textSecondary
                    )
                }
                StepStatus.ACTIVE -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = tokens.modeAnalyze
                    )
                }
                StepStatus.DONE -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = tokens.modeValidate,
                        modifier = Modifier.size(16.dp)
                    )
                }
                StepStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Failed, tap to retry",
                        tint = tokens.modeError,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(enabled = onRetry != null) { onRetry?.invoke() }
                    )
                }
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = when (status) {
                    StepStatus.PENDING -> tokens.textSecondary
                    StepStatus.DONE -> tokens.textPrimary
                    StepStatus.FAILED -> tokens.modeError
                    StepStatus.ACTIVE -> tokens.textPrimary
                }
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Shimmering skeleton loader for card placeholders.
 */
@Composable
fun SkeletonShimmer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp
) {
    val tokens = AppTheme.tokens
    val shimmerAlpha = remember { Animatable(0.2f) }

    LaunchedEffect(Unit) {
        shimmerAlpha.animateTo(
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(tokens.inputChipFill.copy(alpha = shimmerAlpha.value))
            .border(1.dp, tokens.inputChipBorder, RoundedCornerShape(cornerRadius))
    )
}
