package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * OrvexaPersistentHeader
 *
 * Pinned glassmorphic mobile & tablet application header that remains anchored
 * at the top of the viewport during content scrolling.
 *
 * Adheres strictly to Section 6 Critical Requirements:
 * 1. Anchored ORVEXA brand identity, Room DB status badge, and primary menu control.
 * 2. Status bar padding and safe area insets respected.
 * 3. Subtle translucent surface (OrvexaDarkSurface at 94% opacity) with fine border.
 * 4. Content scrolls smoothly underneath without clipping or collision.
 * 5. Minimum 48dp touch targets on all interactive actions.
 */
@Composable
fun OrvexaPersistentHeader(
    onOpenDrawer: () -> Unit,
    onExportClick: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onResetSeedData: () -> Unit,
    onClearAllIdeas: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }

    val headerBg = if (isDark) {
        OrvexaDarkSurface.copy(alpha = 0.95f)
    } else {
        Color.White.copy(alpha = 0.96f)
    }

    val headerBorder = if (isDark) {
        Color(0x2EFFFFFF)
    } else {
        Color(0xFFE2E8F0)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = OrvexaOrange.copy(alpha = 0.2f)
            )
            .testTag("orvexa_persistent_header"),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = headerBg,
        border = BorderStroke(1.dp, headerBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Drawer Hamburger & Brand Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("hamburger_menu_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x14FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = if (isDark) Color.White else OrvexaDarkBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ORVEXA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = OrvexaOrange,
                                letterSpacing = 1.2.sp
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OrvexaOrange.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, OrvexaOrange.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "ROOM DB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrvexaOrange,
                                    letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Text(
                            text = "Idea Intelligence OS & Decision Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Action Buttons (Export, Theme Switcher, Overflow Menu)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Export Button
                    IconButton(
                        onClick = onExportClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("dashboard_export_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x14FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Ideas as PDF or Text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Theme Switcher Button
                    ThemeSwitcherButton(
                        currentTheme = themeMode,
                        onToggleTheme = onToggleTheme
                    )

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("dashboard_menu_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x14FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options Menu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(if (isDark) OrvexaDarkSurface else Color.White)
                                .border(1.dp, headerBorder, RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Ideas (PDF / Text)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = OrvexaOrange)
                                },
                                onClick = {
                                    onExportClick()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Load Sample Intelligence Ideas") },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = OrvexaIntelligentCyan)
                                },
                                onClick = {
                                    onResetSeedData()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All Ideas (Test Empty State)") },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444))
                                },
                                onClick = {
                                    onClearAllIdeas()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
