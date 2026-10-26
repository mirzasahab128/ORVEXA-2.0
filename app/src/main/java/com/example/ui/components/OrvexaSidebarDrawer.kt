package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.*

data class NavigationDrawerItemData(
    val screen: Screen?,
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badge: String? = null,
    val isAction: Boolean = false
)

@Composable
fun OrvexaSidebarDrawerContent(
    currentScreen: Screen,
    ideaCount: Int,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSelectScreen: (Screen) -> Unit,
    onCaptureIdea: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = themeMode == ThemeMode.DARK
    val primaryOrange = OrvexaOrange
    val drawerBg = if (isDark) OrvexaDarkBackground else Color(0xFFFFFFFF)
    val borderColor = if (isDark) OrvexaDarkBorder else Color(0xFFE2E8F0)

    val navigationItems = listOf(
        NavigationDrawerItemData(
            screen = Screen.Dashboard,
            title = "Dashboard",
            icon = Icons.Default.Dashboard,
            accentColor = ModuleDashboardColor,
            badge = "$ideaCount"
        ),
        NavigationDrawerItemData(
            screen = Screen.MyIdeas,
            title = "My Ideas",
            icon = Icons.Default.Folder,
            accentColor = ModuleMyIdeasColor,
            badge = "$ideaCount"
        ),
        NavigationDrawerItemData(
            screen = null,
            title = "New Idea",
            icon = Icons.Default.AddCircle,
            accentColor = ModuleNewIdeaColor,
            isAction = true
        ),
        NavigationDrawerItemData(
            screen = Screen.AIAnalyzer(),
            title = "AI Advisor",
            icon = Icons.Default.AutoAwesome,
            accentColor = ModuleAIAdvisorColor,
            badge = "Live"
        ),
        NavigationDrawerItemData(
            screen = Screen.MarketIntelligence,
            title = "Market Intelligence",
            icon = Icons.Default.Public,
            accentColor = ModuleMarketIntelColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Experiments,
            title = "Experiments",
            icon = Icons.Default.Science,
            accentColor = ModuleExperimentsColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Actions,
            title = "Actions",
            icon = Icons.Default.TaskAlt,
            accentColor = ModuleActionsColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Reminders,
            title = "Reminders",
            icon = Icons.Default.NotificationsActive,
            accentColor = ModuleRemindersColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Insights,
            title = "Insights",
            icon = Icons.Default.Psychology,
            accentColor = ModuleInsightsColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Charts,
            title = "Charts",
            icon = Icons.Default.BarChart,
            accentColor = ModuleChartsColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Matrix,
            title = "Matrix",
            icon = Icons.Default.GridOn,
            accentColor = ModuleMatrixColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Profile,
            title = "Profile",
            icon = Icons.Default.Person,
            accentColor = ModuleProfileColor
        ),
        NavigationDrawerItemData(
            screen = Screen.Settings,
            title = "Settings",
            icon = Icons.Default.Settings,
            accentColor = ModuleSettingsColor
        )
    )

    ModalDrawerSheet(
        drawerContainerColor = drawerBg,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("orvexa_sidebar_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header with Logo and Close Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ORVEXA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrvexaOrange,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = "OS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrvexaOrange,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Idea Intelligence & Decision Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier.testTag("sidebar_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PRIMARY NAVIGATION (13 MODULES)",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryOrange,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                navigationItems.forEach { item ->
                    val isActive = item.screen != null && (
                        currentScreen == item.screen ||
                        (item.screen is Screen.AIAnalyzer && currentScreen is Screen.AIAnalyzer) ||
                        (item.screen is Screen.AIAdvisor && currentScreen is Screen.AIAnalyzer)
                    )

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (item.isAction) {
                                onCaptureIdea()
                            } else if (item.screen != null) {
                                onSelectScreen(item.screen)
                                onCloseDrawer()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) item.accentColor.copy(alpha = if (isDark) 0.2f else 0.12f) else Color.Transparent,
                        border = if (isActive) BorderStroke(1.dp, item.accentColor.copy(alpha = 0.5f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("nav_item_${item.title.lowercase().replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isActive) item.accentColor.copy(alpha = 0.25f) else Color.Transparent,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (isActive) item.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) (if (isDark) Color.White else item.accentColor) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (item.badge != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isActive) item.accentColor else (if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFE2E8F0))
                                ) {
                                    Text(
                                        text = item.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Section: Quick Action & Theme Switcher
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    onClick = onCaptureIdea,
                    shape = RoundedCornerShape(12.dp),
                    color = primaryOrange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("drawer_new_idea_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Idea",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Capture New Thesis",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Theme Mode Switch
                Surface(
                    onClick = onToggleTheme,
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF3F4F6),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = primaryOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDark) "Dark Obsidian (#120F18)" else "Light Obsidian",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Toggle",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryOrange
                        )
                    }
                }
            }
        }
    }
}
