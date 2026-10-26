package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.util.HapticManager

enum class DockDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    IDEAS("Ideas", Icons.Default.Lightbulb),
    INTEL("Intel", Icons.Default.TrendingUp),
    MORE("More", Icons.Default.GridView)
}

/**
 * Floating Glass Dock: 64dp high, 20dp side margins, 12dp above bottom inset.
 * Center orb button (48dp) mirrors the live OrbState and opens AI Advisor.
 */
@Composable
fun OrbDock(
    currentDestination: DockDestination,
    orbState: OrbState,
    onNavigate: (DockDestination) -> Unit,
    onOrbClick: () -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier,
    hapticManager: HapticManager? = null
) {
    val tokens = AppTheme.tokens
    val shape = CircleShape

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f)
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = tokens.glassShadowOffsetY,
                        shape = shape,
                        spotColor = tokens.glassShadow
                    ),
                shape = shape,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(tokens.glassFillTop, tokens.glassFillBottom)
                            )
                        )
                        .border(1.dp, tokens.glassBorder, shape)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Home
                        DockItem(
                            destination = DockDestination.HOME,
                            isSelected = currentDestination == DockDestination.HOME,
                            onClick = {
                                hapticManager?.performTick()
                                onNavigate(DockDestination.HOME)
                            }
                        )

                        // 2. Ideas
                        DockItem(
                            destination = DockDestination.IDEAS,
                            isSelected = currentDestination == DockDestination.IDEAS,
                            onClick = {
                                hapticManager?.performTick()
                                onNavigate(DockDestination.IDEAS)
                            }
                        )

                        // 3. Center Live Orb Button (48dp, opens AI Advisor)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable {
                                    hapticManager?.performClick()
                                    onOrbClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            OrbCore(
                                state = orbState,
                                size = 48.dp,
                                isSimplified = true
                            )
                        }

                        // 4. Intel
                        DockItem(
                            destination = DockDestination.INTEL,
                            isSelected = currentDestination == DockDestination.INTEL,
                            onClick = {
                                hapticManager?.performTick()
                                onNavigate(DockDestination.INTEL)
                            }
                        )

                        // 5. More
                        DockItem(
                            destination = DockDestination.MORE,
                            isSelected = currentDestination == DockDestination.MORE,
                            onClick = {
                                hapticManager?.performTick()
                                onNavigate(DockDestination.MORE)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    destination: DockDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = AppTheme.tokens
    val pillShape = CircleShape

    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(pillShape)
            .background(if (isSelected) tokens.selectedPillFill else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) tokens.selectedPillHighlight else Color.Transparent,
                shape = pillShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            if (isSelected) {
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = tokens.textPrimary
                )
            }
        }
    }
}
