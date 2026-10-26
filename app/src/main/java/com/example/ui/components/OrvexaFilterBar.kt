package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * OrvexaFilterBar
 *
 * Contained responsive filter bar that resolves horizontal clipping defects.
 * Provides accessible, touch-friendly filter chips for Categories, Priorities, and Tags,
 * featuring an active filter indicator and one-tap Reset action.
 */
@Composable
fun OrvexaFilterBar(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    priorities: List<String>,
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit,
    allTags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit,
    onResetAllFilters: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val hasActiveFilters = selectedCategory != "All" || selectedPriority != "All" || selectedTag != "All"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("orvexa_filter_bar"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active Filter Notification / Quick Reset Row
        AnimatedVisibility(
            visible = hasActiveFilters,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = OrvexaOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Filter Applied",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrvexaOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    onClick = onResetAllFilters,
                    shape = RoundedCornerShape(8.dp),
                    color = OrvexaOrange.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, OrvexaOrange.copy(alpha = 0.45f)),
                    modifier = Modifier.testTag("reset_all_filters_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reset Filters",
                            tint = OrvexaOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset Filters",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrvexaOrange
                        )
                    }
                }
            }
        }

        // Row 1: Category Chips (Contained horizontal scroller)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { onCategorySelected(cat) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) OrvexaOrange.copy(alpha = if (isDark) 0.25f else 0.16f) else Color(0x14FFFFFF),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) OrvexaOrange else Color(0x2EFFFFFF)
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .testTag("filter_category_${cat.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) (if (isDark) Color.White else OrvexaOrange) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Row 2: Priority & Tag Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Priority:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            priorities.forEach { p ->
                val isSelected = selectedPriority == p
                val pColor = when (p) {
                    "High" -> Color(0xFFEF4444)
                    "Medium" -> Color(0xFFF59E0B)
                    "Low" -> Color(0xFF64748B)
                    else -> OrvexaIntelligentCyan
                }
                Surface(
                    onClick = { onPrioritySelected(p) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) pColor.copy(alpha = if (isDark) 0.25f else 0.16f) else Color(0x10FFFFFF),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) pColor else Color(0x2EFFFFFF)
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 34.dp)
                        .testTag("filter_priority_${p.lowercase()}")
                ) {
                    Text(
                        text = if (p == "All") "All Priorities" else p,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            if (allTags.size > 1) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tag:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                allTags.take(6).forEach { tag ->
                    val isSelected = selectedTag == tag
                    Surface(
                        onClick = { onTagSelected(tag) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) OrvexaElectricViolet.copy(alpha = 0.25f) else Color(0x10FFFFFF),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OrvexaElectricViolet else Color(0x2EFFFFFF)
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 34.dp)
                            .testTag("filter_tag_${tag.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = if (tag == "All") "All Tags" else "#$tag",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}
