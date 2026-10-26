package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import java.util.Locale

@Composable
fun DashboardCategoryDistributionCard(
    ideas: List<Idea>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)

    val categoryColors = remember {
        mapOf(
            "Healthcare" to Color(0xFF00D4B2),
            "Marketplace" to Color(0xFFF59E0B),
            "Fintech" to Color(0xFF00B4D8),
            "AI & Tech" to Color(0xFF8B5CF6),
            "AI" to Color(0xFF8B5CF6),
            "B2B SaaS" to Color(0xFFEC4899),
            "SaaS" to Color(0xFFEC4899),
            "Enterprise" to Color(0xFF3B82F6),
            "DevTools" to Color(0xFF10B981)
        )
    }

    fun getColor(cat: String): Color = categoryColors[cat] ?: Color(0xFF6366F1)

    val totalIdeas = ideas.size.coerceAtLeast(1)

    // Compute distribution directly from Room database ideas
    val distribution = remember(ideas) {
        ideas.groupBy { it.category }.map { (category, list) ->
            val count = list.size
            val percentage = (count.toFloat() / totalIdeas) * 100f
            val avgScore = list.map { it.opportunityScore }.average().toInt()
            CategoryDistributionItem(
                category = category,
                count = count,
                percentage = percentage,
                averageScore = avgScore,
                color = getColor(category)
            )
        }.sortedByDescending { it.count }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_category_distribution_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CATEGORY DISTRIBUTION (ROOM DB)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "${distribution.size} Active Sectors",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proportional Multi-Segment Horizontal Distribution Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                if (ideas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                } else {
                    distribution.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(item.percentage.coerceAtLeast(0.1f))
                                .background(item.color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category breakdown chips / list with live counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                distribution.forEach { item ->
                    val isSelected = selectedCategory == item.category
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) item.color.copy(alpha = 0.2f) else if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelected) item.color else cardBorder),
                        modifier = Modifier.clickable {
                            onCategorySelected(if (isSelected) "All" else item.category)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.count} (${String.format(Locale.US, "%.0f", item.percentage)}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = item.color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${item.averageScore} pt",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.color,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CategoryDistributionItem(
    val category: String,
    val count: Int,
    val percentage: Float,
    val averageScore: Int,
    val color: Color
)
