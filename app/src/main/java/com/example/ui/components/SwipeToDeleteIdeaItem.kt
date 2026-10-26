package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteIdeaItem(
    idea: Idea,
    onDelete: (Idea) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 150ms animation spec compliant with guidelines
    val dismissAnimationSpec = tween<Float>(durationMillis = 150)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(idea)
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    // Reset swipe state when item is restored or re-rendered
    LaunchedEffect(idea.id, idea.updatedAt) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier
            .fillMaxWidth()
            .testTag("swipe_dismiss_${idea.id}"),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isSwipingToDelete = direction == SwipeToDismissBoxValue.EndToStart

            val color by animateColorAsState(
                targetValue = if (isSwipingToDelete) Color(0xFFDC2626) else Color(0xFF991B1B),
                animationSpec = tween(durationMillis = 150),
                label = "delete_bg_color"
            )

            val scale by animateFloatAsState(
                targetValue = if (isSwipingToDelete) 1.15f else 0.85f,
                animationSpec = tween(durationMillis = 150),
                label = "delete_icon_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.scale(scale)
                ) {
                    Text(
                        text = "DELETE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Idea",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        content = {
            content()
        }
    )
}
