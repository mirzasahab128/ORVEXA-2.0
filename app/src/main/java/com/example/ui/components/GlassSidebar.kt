package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.*

/**
 * GlassSidebarNavigationItem
 * Represents a destination item in the GlassSidebar.
 */
data class GlassSidebarNavigationItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val screen: Screen?,
    val icon: ImageVector,
    val accentColor: Color,
    val badge: String? = null,
    val isPrimaryPillar: Boolean = false,
    val isAction: Boolean = false
)

/**
 * GlassSidebar
 *
 * Glassmorphic navigation sidebar utilizing a translucent background (`rgba(255, 255, 255, 0.085)`)
 * with layered blur and depth, providing high-priority links to:
 * 1. Idea Discovery (Market Intelligence & thesis exploration)
 * 2. Analyzer (AI Advisor & deep evaluative engine)
 * 3. Portfolio (Curated ideas & venture repository)
 *
 * It seamlessly adapts to mobile viewports via a slide-out drawer pattern with scrim and gestures.
 */
@Composable
fun GlassSidebar(
    currentScreen: Screen,
    ideaCount: Int,
    themeMode: ThemeMode,
    onSelectScreen: (Screen) -> Unit,
    onCaptureIdea: () -> Unit,
    onCloseDrawer: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    isMobileDrawer: Boolean = true
) {
    val isDark = themeMode == ThemeMode.DARK

    // Requirement: rgba(255, 255, 255, 0.085)
    val glassmorphicBg = Color(0x16FFFFFF)
    val glassBorder = Color(0x2EFFFFFF) // rgba(255, 255, 255, 0.18)
    val canvasUnderlay = if (isDark) OrvexaDarkBackground.copy(alpha = 0.92f) else Color(0xFFF1F5F9).copy(alpha = 0.95f)

    // Primary Core Pillars requested by prompt
    val primaryPillars = listOf(
        GlassSidebarNavigationItem(
            id = "idea_discovery",
            title = "Idea Discovery",
            subtitle = "Market intel & thesis radar",
            screen = Screen.MarketIntelligence,
            icon = Icons.Default.Explore,
            accentColor = OrvexaIntelligentCyan,
            badge = "Radar",
            isPrimaryPillar = true
        ),
        GlassSidebarNavigationItem(
            id = "analyzer",
            title = "Analyzer",
            subtitle = "Real-time AI thesis evaluation",
            screen = Screen.AIAnalyzer(),
            icon = Icons.Default.AutoAwesome,
            accentColor = OrvexaElectricViolet,
            badge = "Gemini",
            isPrimaryPillar = true
        ),
        GlassSidebarNavigationItem(
            id = "portfolio",
            title = "Portfolio",
            subtitle = "Active ventures & validation",
            screen = Screen.MyIdeas,
            icon = Icons.Default.Workspaces,
            accentColor = OrvexaOrange,
            badge = "$ideaCount",
            isPrimaryPillar = true
        )
    )

    // Secondary / Workspace Modules (Full Orvexa Operating System Suite)
    val workspaceModules = listOf(
        GlassSidebarNavigationItem(
            id = "dashboard",
            title = "Dashboard",
            subtitle = "Executive overview",
            screen = Screen.Dashboard,
            icon = Icons.Default.Dashboard,
            accentColor = ModuleDashboardColor
        ),
        GlassSidebarNavigationItem(
            id = "matrix",
            title = "Matrix",
            subtitle = "2x2 Prioritization quadrants",
            screen = Screen.Matrix,
            icon = Icons.Default.GridOn,
            accentColor = ModuleMatrixColor
        ),
        GlassSidebarNavigationItem(
            id = "charts",
            title = "Charts",
            subtitle = "Quantitative benchmarks",
            screen = Screen.Charts,
            icon = Icons.Default.BarChart,
            accentColor = ModuleChartsColor
        ),
        GlassSidebarNavigationItem(
            id = "experiments",
            title = "Experiments",
            subtitle = "Validation pipeline",
            screen = Screen.Experiments,
            icon = Icons.Default.Science,
            accentColor = ModuleExperimentsColor
        ),
        GlassSidebarNavigationItem(
            id = "actions",
            title = "Actions",
            subtitle = "Execution roadmap",
            screen = Screen.Actions,
            icon = Icons.Default.TaskAlt,
            accentColor = ModuleActionsColor
        ),
        GlassSidebarNavigationItem(
            id = "reminders",
            title = "Reminders",
            subtitle = "Scheduled watch-points",
            screen = Screen.Reminders,
            icon = Icons.Default.NotificationsActive,
            accentColor = ModuleRemindersColor
        ),
        GlassSidebarNavigationItem(
            id = "insights",
            title = "Insights",
            subtitle = "Evidence claims ledger",
            screen = Screen.Insights,
            icon = Icons.Default.Psychology,
            accentColor = ModuleInsightsColor
        ),
        GlassSidebarNavigationItem(
            id = "profile",
            title = "Profile",
            subtitle = "Thesis calibration",
            screen = Screen.Profile,
            icon = Icons.Default.Person,
            accentColor = ModuleProfileColor
        ),
        GlassSidebarNavigationItem(
            id = "settings",
            title = "Settings",
            subtitle = "API & Preferences",
            screen = Screen.Settings,
            icon = Icons.Default.Settings,
            accentColor = ModuleSettingsColor
        )
    )

    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .width(330.dp)
            .fillMaxHeight()
            .shadow(
                elevation = 20.dp,
                shape = if (isMobileDrawer) RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp) else RoundedCornerShape(0.dp)
            )
            .testTag("glass_sidebar"),
        shape = if (isMobileDrawer) RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp) else RoundedCornerShape(0.dp),
        color = canvasUnderlay,
        border = BorderStroke(1.dp, glassBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glassmorphicBg,
                            Color(0x08FFFFFF),
                            glassmorphicBg
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 18.dp, horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Header with Glassmorphic Badge & Close Affordance
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(OrvexaOrange, OrvexaElectricViolet)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "O",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ORVEXA",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Color.White else OrvexaDarkBackground,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = OrvexaOrange.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, OrvexaOrange.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "SPATIAL",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OrvexaOrange,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "Idea Intelligence OS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isMobileDrawer) {
                            IconButton(
                                onClick = onCloseDrawer,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("glass_sidebar_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Menu",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = glassBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // 1. PRIMARY PILLARS SECTION (Idea Discovery, Analyzer, Portfolio)
                    Text(
                        text = "PRIMARY DESTINATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrvexaOrange,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )

                    primaryPillars.forEach { item ->
                        val isSelected = when (item.id) {
                            "idea_discovery" -> currentScreen is Screen.MarketIntelligence
                            "analyzer" -> currentScreen is Screen.AIAnalyzer || currentScreen is Screen.AIAdvisor
                            "portfolio" -> currentScreen is Screen.MyIdeas
                            else -> false
                        }

                        GlassSidebarItemRow(
                            item = item,
                            isSelected = isSelected,
                            isDark = isDark,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                item.screen?.let { onSelectScreen(it) }
                                if (isMobileDrawer) onCloseDrawer()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. WORKSPACE MODULES
                    Text(
                        text = "INTELLIGENCE WORKSPACE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )

                    workspaceModules.forEach { item ->
                        val isSelected = currentScreen == item.screen

                        GlassSidebarItemRow(
                            item = item,
                            isSelected = isSelected,
                            isDark = isDark,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                item.screen?.let { onSelectScreen(it) }
                                if (isMobileDrawer) onCloseDrawer()
                            }
                        )
                    }
                }

                // BOTTOM ACTION SECTION: Quick Thesis Capture & Theme Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        color = glassBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Thesis capture button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCaptureIdea()
                            if (isMobileDrawer) onCloseDrawer()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrvexaOrange,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("glass_sidebar_capture_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Capture New Idea",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Theme Mode Toggle Bar
                    Surface(
                        onClick = onToggleTheme,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x14FFFFFF),
                        border = BorderStroke(1.dp, glassBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("glass_sidebar_theme_toggle")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = OrvexaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isDark) "Dark Obsidian (#120F18)" else "Light Canvas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Toggle",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrvexaOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual row item for the GlassSidebar
 */
@Composable
private fun GlassSidebarItemRow(
    item: GlassSidebarNavigationItem,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val surfaceBg = when {
        isSelected -> item.accentColor.copy(alpha = if (isDark) 0.22f else 0.14f)
        isFocused -> Color.White.copy(alpha = 0.10f)
        else -> Color.Transparent
    }

    val surfaceBorder = when {
        isSelected -> BorderStroke(1.dp, item.accentColor.copy(alpha = 0.55f))
        isFocused -> BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
        else -> null
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = surfaceBg,
        border = surfaceBorder,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .defaultMinSize(minHeight = 48.dp)
            .focusable(interactionSource = interactionSource)
            .semantics {
                role = Role.Button
                contentDescription = "${item.title}: ${item.subtitle ?: ""}"
            }
            .testTag("sidebar_link_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Icon Box with accent tint
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) item.accentColor.copy(alpha = 0.25f)
                            else Color(0x14FFFFFF)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) item.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || item.isPrimaryPillar) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.5.sp
                    )
                    if (item.subtitle != null && item.isPrimaryPillar) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Optional Badge
            if (item.badge != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) item.accentColor else Color(0x1AFFFFFF),
                    border = BorderStroke(1.dp, if (isSelected) item.accentColor else Color(0x2EFFFFFF))
                ) {
                    Text(
                        text = item.badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
