package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.ui.theme.*

/**
 * GeminiRiskVsRewardCard
 *
 * Institutional venture intelligence card powered by the Gemini API (gemini-3.5-flash).
 * Analyzes saved ideas, providing:
 * - Risk vs. Reward composite score (0-100) and payoff ratio (e.g. 2.8x)
 * - Risk breakdown (0-100) and Reward upside (0-100)
 * - Summarized potential market impact & TAM headroom
 * - Key downside risks & key reward drivers
 * - Strategic hypothesis validation
 */
@Composable
fun GeminiRiskVsRewardCard(
    idea: Idea,
    isAnalyzing: Boolean,
    onRunAnalysis: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val cardBg = if (isDark) OrvexaDarkSurface else Color.White
    val cardBorder = if (isDark) Color(0x2EFFFFFF) else Color(0x33000000)
    val primaryCyan = if (isDark) OrvexaIntelligentCyan else Color(0xFF0284C7)
    val orvexaOrange = OrvexaOrange

    // Smooth breathing animation during Gemini analysis
    val infiniteTransition = rememberInfiniteTransition(label = "risk_reward_pulse")
    val breathingGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gemini_analysis_pulse"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(if (isAnalyzing) 1.8.dp else 1.2.dp, if (isAnalyzing) orvexaOrange.copy(alpha = breathingGlow) else cardBorder),
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_risk_vs_reward_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title, Gemini Badge & Trigger Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(orvexaOrange, OrvexaElectricViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Analysis",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Risk vs. Reward Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else OrvexaDarkBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = primaryCyan.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, primaryCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "GEMINI 3.5",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryCyan,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Institutional payoff & market impact synthesis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Analyze button
                Button(
                    onClick = {
                        if (!isAnalyzing) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRunAnalysis()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnalyzing) orvexaOrange.copy(alpha = 0.25f) else orvexaOrange,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("run_gemini_risk_reward_btn")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyzing...", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analyze", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Banner: Composite Score, Ratio, Downside Risk vs Upside Reward
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0x16FFFFFF) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Risk-to-Reward Ratio
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PAYOFF RATIO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = idea.riskVsRewardRatio.ifBlank { "2.5x" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = primaryCyan
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0x33FFFFFF))
                    )

                    // Downside Risk Score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DOWNSIDE RISK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${idea.riskScore}/100",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0x33FFFFFF))
                    )

                    // Upside Reward Score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "COMMERCIAL UPSIDE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${idea.rewardScore}/100",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0x33FFFFFF))
                    )

                    // Composite Score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CONVICTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = orvexaOrange,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${idea.riskVsRewardScore}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = orvexaOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summarized Potential Market Impact
            Text(
                text = "POTENTIAL MARKET IMPACT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = primaryCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0x10FFFFFF) else Color(0x0A000000),
                border = BorderStroke(1.dp, Color(0x18FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = idea.potentialMarketImpactSummary.ifBlank {
                        "Targeting high-margin productivity gains in ${idea.category}. By replacing fragmented manual workarounds, this venture unlocks measurable ROI and high barrier-to-entry switching costs."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Strategic Hypothesis (if present)
            if (idea.strategicHypothesis.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "STRATEGIC HYPOTHESIS UNDER TEST",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = orvexaOrange,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = orvexaOrange.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, orvexaOrange.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = orvexaOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = idea.strategicHypothesis,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
