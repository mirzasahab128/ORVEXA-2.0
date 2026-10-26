package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.ThemeMode

@Composable
fun IdeaCard(
    idea: Idea,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode = LocalThemeMode.current
    val isDark = themeMode == ThemeMode.DARK

    val scoreColor = when {
        idea.opportunityScore >= 80 -> Color(0xFF10B981)
        idea.opportunityScore >= 70 -> if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
        idea.opportunityScore >= 60 -> Color(0xFFF59E0B)
        else -> Color(0xFFF43F5E)
    }

    val recColor = when (idea.recommendation) {
        Recommendation.BUILD_NOW -> Color(0xFF10B981)
        Recommendation.VALIDATE_FIRST -> if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
        Recommendation.BUILD_SMALL, Recommendation.PARTNER -> Color(0xFF6366F1)
        Recommendation.WATCH, Recommendation.WAIT -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("idea_card_${idea.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isDark) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Score + Recommendation + Timing Window
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opportunity Score Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = scoreColor.copy(alpha = if (isDark) 0.18f else 0.12f),
                        border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${idea.opportunityScore}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )
                            Text(
                                text = "/100",
                                style = MaterialTheme.typography.labelSmall,
                                color = scoreColor.copy(alpha = 0.75f)
                            )
                        }
                    }

                    // Recommendation Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = recColor.copy(alpha = if (isDark) 0.16f else 0.12f)
                    ) {
                        Text(
                            text = idea.recommendation.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = recColor,
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Sample badge: demo data must never look like user data.
                    if (idea.isSample) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF6366F1).copy(alpha = if (isDark) 0.18f else 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "SAMPLE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6366F1),
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                val (priorityColor, priorityBg) = when (idea.priority.lowercase()) {
                    "high" -> Color(0xFFEF4444) to (if (isDark) Color(0xFF3B1219) else Color(0xFFFEE2E2))
                    "medium" -> Color(0xFFF59E0B) to (if (isDark) Color(0xFF332310) else Color(0xFFFEF3C7))
                    else -> Color(0xFF64748B) to (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority Status Visual Indicator
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityBg,
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(priorityColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${idea.priority.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Opportunity Window / Timing
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = idea.opportunityWindow.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = idea.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // One Line Thesis
            Text(
                text = idea.oneLineThesis.ifEmpty { idea.description },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: Category Chip, Tag Chip, and Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Color(0xFF162032) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF26354D) else Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = idea.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ) {
                        Text(
                            text = idea.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    if (idea.healthTrend.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (idea.healthTrend == "Improving") Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = idea.healthTrend,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
