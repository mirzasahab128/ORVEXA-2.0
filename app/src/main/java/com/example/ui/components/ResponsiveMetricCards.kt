package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.ThemeMode

@Composable
fun ResponsiveMetricCards(
    ideas: List<Idea>,
    modifier: Modifier = Modifier
) {
    val totalCount = ideas.size
    val highConvictionCount = ideas.count { it.recommendation == Recommendation.BUILD_NOW }
    val avgScore = if (ideas.isNotEmpty()) ideas.map { it.opportunityScore }.average().toInt() else 0
    val activeValidations = ideas.count { it.recommendation == Recommendation.VALIDATE_FIRST }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metrics_container")
    ) {
        val availableWidth = maxWidth

        if (availableWidth < 360.dp) {
            // Very narrow mobile layout: 2-column compact
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricTile(
                        title = "AVG SCORE",
                        value = if (totalCount > 0) "$avgScore" else "--",
                        subtext = if (totalCount > 0) "Top 15%" else "No data",
                        icon = Icons.Default.Assessment,
                        accentColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "CONVICTION",
                        value = "$highConvictionCount",
                        subtext = "Build Now",
                        icon = Icons.Default.Bolt,
                        accentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricTile(
                        title = "SAVED IDEAS",
                        value = "$totalCount",
                        subtext = "Room DB",
                        icon = Icons.Default.Storage,
                        accentColor = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "VALIDATING",
                        value = "$activeValidations",
                        subtext = "Active",
                        icon = Icons.Default.Timer,
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else if (availableWidth <= 600.dp) {
            // Standard mobile layout: 2x2 grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        title = "PORTFOLIO HEALTH",
                        value = if (totalCount > 0) "$avgScore%" else "--",
                        subtext = if (totalCount > 0) "Optimal Signal" else "Empty DB",
                        icon = Icons.Default.Assessment,
                        accentColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "HIGH CONVICTION",
                        value = "$highConvictionCount",
                        subtext = "Build Now Rec",
                        icon = Icons.Default.Bolt,
                        accentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        title = "ROOM ENTRIES",
                        value = "$totalCount",
                        subtext = "Persistent",
                        icon = Icons.Default.Storage,
                        accentColor = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "VALIDATING",
                        value = "$activeValidations",
                        subtext = "In Pipeline",
                        icon = Icons.Default.Timer,
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Wide mobile / tablet / foldable unfolded layout: 4-column row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    title = "PORTFOLIO HEALTH",
                    value = if (totalCount > 0) "$avgScore%" else "--",
                    subtext = "Optimal Signal",
                    icon = Icons.Default.Assessment,
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "HIGH CONVICTION",
                    value = "$highConvictionCount",
                    subtext = "Build Now Rec",
                    icon = Icons.Default.Bolt,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "ROOM ENTRIES",
                    value = "$totalCount",
                    subtext = "Persistent",
                    icon = Icons.Default.Storage,
                    accentColor = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "VALIDATING",
                    value = "$activeValidations",
                    subtext = "In Pipeline",
                    icon = Icons.Default.Timer,
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val themeMode = LocalThemeMode.current
    val isDark = themeMode == ThemeMode.DARK

    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
