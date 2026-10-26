package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.HapticManager

/**
 * Filter bar supporting tag-based and category-based filtering of saved ideas
 * stored in the local Room database.
 */
@Composable
fun TagFilterBar(
    availableCategories: List<String>,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    availableTags: List<String>,
    selectedTag: String?,
    onSelectTag: (String?) -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        val isAllSelected = selectedCategory == null && selectedTag == null
        FilterChip(
            selected = isAllSelected,
            onClick = {
                hapticManager?.performTick()
                onSelectCategory(null)
                onSelectTag(null)
            },
            label = {
                Text(
                    text = "All Ideas",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                )
            },
            leadingIcon = if (isAllSelected) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else null,
            shape = RoundedCornerShape(10.dp)
        )

        // Categories
        availableCategories.forEach { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = {
                    hapticManager?.performTick()
                    if (isSelected) onSelectCategory(null) else onSelectCategory(category)
                },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Tags
        availableTags.forEach { tag ->
            val isSelected = selectedTag == tag
            FilterChip(
                selected = isSelected,
                onClick = {
                    hapticManager?.performTick()
                    if (isSelected) onSelectTag(null) else onSelectTag(tag)
                },
                label = {
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}
