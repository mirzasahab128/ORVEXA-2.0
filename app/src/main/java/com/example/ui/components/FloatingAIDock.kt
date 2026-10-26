package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * FloatingAIDock
 *
 * High-elevation glassmorphic floating action dock positioned at the bottom of the workspace.
 * Provides quick access to "Analyze Idea" and "Generate Prompt" actions, maintaining
 * accessibility standards (touch targets >= 48dp, high-contrast labels, keyboard focus indicators)
 * and responsiveness across compact mobile, tablet, and desktop viewports.
 *
 * Visual spec:
 * - Glassmorphic surface: rgba(255, 255, 255, 0.085) with layered elevated backdrop
 * - Restrained reflective highlight: rgba(255, 255, 255, 0.18) border
 * - High elevation shadow (16dp)
 * - Spring-assisted click feedback and ambient glow
 */
@Composable
fun FloatingAIDock(
    onAnalyzeIdea: () -> Unit,
    onGeneratePrompt: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    compactMode: Boolean = false,
    activeIdeaTitle: String? = null
) {
    // Ambient breathing pulse for the AI intelligence indicator
    val infiniteTransition = rememberInfiniteTransition(label = "floating_dock_pulse")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Glass styling tokens
    val glassFill = if (isDark) {
        Color(0x16FFFFFF) // rgba(255, 255, 255, 0.085f)
    } else {
        Color(0xCCFFFFFF)
    }

    val glassBorder = if (isDark) {
        Color(0x2EFFFFFF) // rgba(255, 255, 255, 0.18f)
    } else {
        Color(0x33000000)
    }

    val containerBackdrop = if (isDark) {
        OrvexaDarkSurface.copy(alpha = 0.88f)
    } else {
        Color(0xFFF8FAFC).copy(alpha = 0.94f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("floating_ai_dock"),
        contentAlignment = Alignment.Center
    ) {
        // High-elevation outer glow shadow
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = OrvexaOrange.copy(alpha = pulseGlowAlpha * 0.4f),
                    ambientColor = Color.Black.copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(26.dp),
            color = containerBackdrop,
            border = BorderStroke(1.2.dp, glassBorder)
        ) {
            // Glassmorphic specular gradient overlay
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                glassFill,
                                glassFill.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Action: Analyze Idea
                    FloatingAIDockActionButton(
                        icon = Icons.Default.AutoAwesome,
                        label = "Analyze Idea",
                        subtitle = if (compactMode) null else (activeIdeaTitle?.take(18) ?: "Deep thesis audit"),
                        accentColor = OrvexaOrange,
                        testTagId = "floating_ai_dock_analyze_idea",
                        onClick = onAnalyzeIdea,
                        modifier = Modifier.weight(1f)
                    )

                    // Center AI Status Indicator / Separator
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf<Color>(
                                        OrvexaElectricViolet.copy(alpha = pulseGlowAlpha),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = CircleShape,
                            color = OrvexaOrange,
                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f))
                        ) {}
                    }

                    // Right Action: Generate Prompt
                    FloatingAIDockActionButton(
                        icon = Icons.Default.Lightbulb,
                        label = "Generate Prompt",
                        subtitle = if (compactMode) null else "Venture prompt engine",
                        accentColor = OrvexaIntelligentCyan,
                        testTagId = "floating_ai_dock_generate_prompt",
                        onClick = onGeneratePrompt,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Individual accessible action button for the FloatingAIDock.
 * Ensures a minimum 48dp x 48dp touch target, keyboard navigation support, and clear semantic labels.
 */
@Composable
private fun FloatingAIDockActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String?,
    accentColor: Color,
    testTagId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(18.dp),
        color = if (isFocused) accentColor.copy(alpha = 0.20f) else accentColor.copy(alpha = 0.12f),
        border = BorderStroke(
            width = if (isFocused) 1.5.dp else 1.dp,
            color = if (isFocused) accentColor else accentColor.copy(alpha = 0.35f)
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .focusable(interactionSource = interactionSource)
            .semantics {
                role = Role.Button
                contentDescription = "$label: ${subtitle ?: ""}"
            }
            .testTag(testTagId)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon with glowing badge container
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
