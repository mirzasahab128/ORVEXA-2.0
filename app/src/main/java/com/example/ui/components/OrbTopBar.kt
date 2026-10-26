package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.util.HapticManager

/**
 * Pinned Glass Pill Top Bar: 56dp high, 16dp below the top inset.
 * Contains: 22dp logo orb, ORVEXA wordmark, expandable search field,
 * reminders bell with badge, theme toggle (sun/moon), and avatar.
 */
@Composable
fun OrbTopBar(
    remindersCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onRemindersClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticManager: HapticManager? = null
) {
    val tokens = AppTheme.tokens
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        GlassPill(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isSearchExpanded) {
                    // Full Width Search Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = tokens.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                color = tokens.textPrimary
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(tokens.primaryButtonFill),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search ideas, theses, tags...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            color = tokens.textSecondary.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        IconButton(
                            onClick = {
                                hapticManager?.performClick()
                                onSearchQueryChange("")
                                isSearchExpanded = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = tokens.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    // Standard Brand and Actions Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 22dp Logo Orb
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0xFF7D8CFF),
                                            Color(0xFF2A1D9C)
                                        )
                                    )
                                )
                        )

                        // Wordmark: weight 700, tracking 0.22em, 16sp
                        Text(
                            text = "ORVEXA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.5.sp
                            ),
                            color = tokens.textPrimary
                        )
                    }

                    // Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Search Button
                        IconButton(
                            onClick = {
                                hapticManager?.performClick()
                                isSearchExpanded = true
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search ideas",
                                tint = tokens.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Bell with Reminders Count
                        IconButton(
                            onClick = {
                                hapticManager?.performClick()
                                onRemindersClick()
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (remindersCount > 0) {
                                        Badge(
                                            containerColor = tokens.modeListening,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = if (remindersCount > 9) "9+" else "$remindersCount",
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Reminders ($remindersCount)",
                                    tint = tokens.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Night / Morning Theme Toggle
                        IconButton(
                            onClick = {
                                hapticManager?.performTick()
                                onToggleTheme()
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (tokens.isNight) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (tokens.isNight) "Switch to Morning theme" else "Switch to Night theme",
                                tint = tokens.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Avatar
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    hapticManager?.performClick()
                                    onAvatarClick()
                                },
                            color = tokens.inputChipFill,
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.inputChipBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = tokens.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
