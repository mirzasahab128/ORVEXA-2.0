package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class OrvexaModuleId(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val testTagId: String
) {
    DASHBOARD(
        title = "Dashboard",
        subtitle = "Portfolio pulse & strategic readiness",
        icon = Icons.Default.Dashboard,
        accentColor = ModuleDashboardColor,
        testTagId = "dock_item_dashboard"
    ),
    MY_IDEAS(
        title = "My Ideas",
        subtitle = "Curated venture repository & intelligence",
        icon = Icons.Default.Folder,
        accentColor = ModuleMyIdeasColor,
        testTagId = "dock_item_my_ideas"
    ),
    NEW_IDEA(
        title = "New Idea",
        subtitle = "Capture and evaluate a new venture thesis",
        icon = Icons.Default.AddCircle,
        accentColor = ModuleNewIdeaColor,
        testTagId = "dock_item_new_idea"
    ),
    AI_ADVISOR(
        title = "AI Advisor",
        subtitle = "Real-time conversational analyzer & prompt engine",
        icon = Icons.Default.AutoAwesome,
        accentColor = ModuleAIAdvisorColor,
        testTagId = "dock_item_ai_advisor"
    ),
    MARKET_INTEL(
        title = "Market Intel",
        subtitle = "Competitor positioning, market gaps & buyer objections",
        icon = Icons.Default.Public,
        accentColor = ModuleMarketIntelColor,
        testTagId = "dock_item_market_intel"
    ),
    EXPERIMENTS(
        title = "Experiments",
        subtitle = "Hypothesis testing & venture validation pipeline",
        icon = Icons.Default.Science,
        accentColor = ModuleExperimentsColor,
        testTagId = "dock_item_experiments"
    ),
    ACTIONS(
        title = "Actions",
        subtitle = "High-leverage execution roadmap & milestones",
        icon = Icons.Default.TaskAlt,
        accentColor = ModuleActionsColor,
        testTagId = "dock_item_actions"
    ),
    REMINDERS(
        title = "Reminders",
        subtitle = "Time-critical triggers & regulatory watch-points",
        icon = Icons.Default.NotificationsActive,
        accentColor = ModuleRemindersColor,
        testTagId = "dock_item_reminders"
    ),
    INSIGHTS(
        title = "Insights",
        subtitle = "Evidence claims, strategic pivots & portfolio signals",
        icon = Icons.Default.Psychology,
        accentColor = ModuleInsightsColor,
        testTagId = "dock_item_insights"
    ),
    CHARTS(
        title = "Charts",
        subtitle = "Visual distribution, health metrics & score analytics",
        icon = Icons.Default.BarChart,
        accentColor = ModuleChartsColor,
        testTagId = "dock_item_charts"
    ),
    MATRIX(
        title = "Matrix",
        subtitle = "2x2 Prioritization, feasibility & timing quadrants",
        icon = Icons.Default.GridOn,
        accentColor = ModuleMatrixColor,
        testTagId = "dock_item_matrix"
    ),
    PROFILE(
        title = "Profile",
        subtitle = "Founder risk profile, thesis & calibration",
        icon = Icons.Default.Person,
        accentColor = ModuleProfileColor,
        testTagId = "dock_item_profile"
    ),
    SETTINGS(
        title = "Settings",
        subtitle = "System configuration, Gemini API & data controls",
        icon = Icons.Default.Settings,
        accentColor = ModuleSettingsColor,
        testTagId = "dock_item_settings"
    );

    companion object {
        fun fromScreen(screen: Screen): OrvexaModuleId {
            return when (screen) {
                is Screen.Dashboard -> DASHBOARD
                is Screen.MyIdeas -> MY_IDEAS
                is Screen.AIAdvisor, is Screen.AIAnalyzer -> AI_ADVISOR
                is Screen.MarketIntelligence -> MARKET_INTEL
                is Screen.Experiments -> EXPERIMENTS
                is Screen.Actions -> ACTIONS
                is Screen.Reminders -> REMINDERS
                is Screen.Insights -> INSIGHTS
                is Screen.Charts -> CHARTS
                is Screen.Matrix -> MATRIX
                is Screen.Profile -> PROFILE
                is Screen.Settings -> SETTINGS
                is Screen.Detail -> MY_IDEAS
            }
        }
    }
}

/**
 * Premium glassmorphic Orvexa Dynamic Dock with glowing spring-like active indicator,
 * rounded glass surface, responsive compact/expanded states, and smooth auto-scroll.
 */
@Composable
fun OrvexaDynamicDock(
    currentScreen: Screen,
    onNavigateToScreen: (Screen) -> Unit,
    onOpenNewIdea: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val currentModule = OrvexaModuleId.fromScreen(currentScreen)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var showAllModulesSheet by remember { mutableStateOf(false) }

    val modules = remember { OrvexaModuleId.entries }
    val activeIndex = modules.indexOf(currentModule).coerceAtLeast(0)

    // Smoothly scroll active item into view when screen changes
    LaunchedEffect(currentModule) {
        if (activeIndex in 0 until modules.size) {
            coroutineScope.launch {
                listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
            }
        }
    }

    val dockBg = if (isDark) {
        Color(0xFF1A1624).copy(alpha = 0.94f) // Required: elevated surface #1A1624 with glassmorphism
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.96f)
    }

    val dockBorder = if (isDark) {
        Color(0xFF2E273D)
    } else {
        Color(0xFFE5E7EB)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val isCompact = availableWidth < 600.dp
        val isDesktop = availableWidth >= 840.dp

        Surface(
            shape = RoundedCornerShape(26.dp),
            color = dockBg,
            border = BorderStroke(1.dp, dockBorder),
            shadowElevation = 14.dp,
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxWidth(if (isDesktop) 0.88f else 1f)
                .testTag("orvexa_dynamic_dock")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Horizontally scrollable dock items
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(modules, key = { _, item -> item.name }) { index, module ->
                        val isSelected = (module == currentModule)
                        val isNewIdeaItem = (module == OrvexaModuleId.NEW_IDEA)

                        OrvexaDockItem(
                            module = module,
                            isSelected = isSelected,
                            isDark = isDark,
                            isCompact = isCompact,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (isNewIdeaItem) {
                                    onOpenNewIdea()
                                } else {
                                    val targetScreen: Screen = when (module) {
                                        OrvexaModuleId.DASHBOARD -> Screen.Dashboard
                                        OrvexaModuleId.MY_IDEAS -> Screen.MyIdeas
                                        OrvexaModuleId.AI_ADVISOR -> Screen.AIAnalyzer()
                                        OrvexaModuleId.MARKET_INTEL -> Screen.MarketIntelligence
                                        OrvexaModuleId.EXPERIMENTS -> Screen.Experiments
                                        OrvexaModuleId.ACTIONS -> Screen.Actions
                                        OrvexaModuleId.REMINDERS -> Screen.Reminders
                                        OrvexaModuleId.INSIGHTS -> Screen.Insights
                                        OrvexaModuleId.CHARTS -> Screen.Charts
                                        OrvexaModuleId.MATRIX -> Screen.Matrix
                                        OrvexaModuleId.PROFILE -> Screen.Profile
                                        OrvexaModuleId.SETTINGS -> Screen.Settings
                                        OrvexaModuleId.NEW_IDEA -> Screen.Dashboard
                                    }
                                    onNavigateToScreen(targetScreen)
                                }
                            }
                        )
                    }
                }

                // Quick overview / grid sheet launcher button for mobile accessibility
                IconButton(
                    onClick = { showAllModulesSheet = true },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(38.dp)
                        .background(
                            color = if (isDark) Color(0xFF221D30) else Color(0xFFF3F4F6),
                            shape = CircleShape
                        )
                        .testTag("dock_grid_launcher_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "All 13 Modules Grid",
                        tint = if (isDark) OrvexaOrange else Color(0xFFE4531F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Accessible Full-Screen / Modal Grid Sheet of all 13 modules
    if (showAllModulesSheet) {
        OrvexaAllModulesModalSheet(
            currentModule = currentModule,
            onDismiss = { showAllModulesSheet = false },
            onSelectModule = { selected ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showAllModulesSheet = false
                if (selected == OrvexaModuleId.NEW_IDEA) {
                    onOpenNewIdea()
                } else {
                    val targetScreen: Screen = when (selected) {
                        OrvexaModuleId.DASHBOARD -> Screen.Dashboard
                        OrvexaModuleId.MY_IDEAS -> Screen.MyIdeas
                        OrvexaModuleId.AI_ADVISOR -> Screen.AIAnalyzer()
                        OrvexaModuleId.MARKET_INTEL -> Screen.MarketIntelligence
                        OrvexaModuleId.EXPERIMENTS -> Screen.Experiments
                        OrvexaModuleId.ACTIONS -> Screen.Actions
                        OrvexaModuleId.REMINDERS -> Screen.Reminders
                        OrvexaModuleId.INSIGHTS -> Screen.Insights
                        OrvexaModuleId.CHARTS -> Screen.Charts
                        OrvexaModuleId.MATRIX -> Screen.Matrix
                        OrvexaModuleId.PROFILE -> Screen.Profile
                        OrvexaModuleId.SETTINGS -> Screen.Settings
                        OrvexaModuleId.NEW_IDEA -> Screen.Dashboard
                    }
                    onNavigateToScreen(targetScreen)
                }
            },
            isDark = isDark
        )
    }
}

/**
 * Individual item in the Orvexa Dynamic Dock with glowing pill indicator,
 * spring-based scale animation, and high-contrast accessibility.
 */
@Composable
private fun OrvexaDockItem(
    module: OrvexaModuleId,
    isSelected: Boolean,
    isDark: Boolean,
    isCompact: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Smooth spring animation for active pill scale and bounce
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dock_scale_${module.name}"
    )

    val activeGlow = module.accentColor.copy(alpha = if (isDark) 0.22f else 0.15f)
    val activeBorder = module.accentColor.copy(alpha = if (isSelected) 0.65f else 0f)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) activeGlow else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected || isFocused) 1.5.dp else 0.dp,
            color = if (isFocused) OrvexaOrange else activeBorder
        ),
        modifier = Modifier
            .height(46.dp)
            .focusable(interactionSource = interactionSource)
            .semantics {
                role = Role.Tab
                contentDescription = "${module.title}: ${module.subtitle}"
            }
            .testTag(module.testTagId)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = if (isSelected) 12.dp else 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon with glowing accent tint
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    tint = if (isSelected) module.accentColor else (if (isDark) Color(0xFF9E9AA8) else Color(0xFF6B7280)),
                    modifier = Modifier.size(if (isSelected) 21.dp else 19.dp)
                )
            }

            // Text Label: always visible on desktop/tablets; shown for active or readable on mobile
            if (isSelected || !isCompact) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = module.title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        if (isDark) Color.White else Color(0xFF111827)
                    } else {
                        if (isDark) Color(0xFF9E9AA8) else Color(0xFF4B5563)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Subtle glowing active indicator dot
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(module.accentColor)
                )
            }
        }
    }
}

/**
 * Accessible Modal Sheet presenting all 13 primary modules in a clean responsive grid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrvexaAllModulesModalSheet(
    currentModule: OrvexaModuleId,
    onDismiss: () -> Unit,
    onSelectModule: (OrvexaModuleId) -> Unit,
    isDark: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) OrvexaDarkSurface else Color.White,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("all_modules_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDark) Color(0xFF2E273D) else Color(0xFFE5E7EB)
                        ) {
                            Text(
                                text = "13 MODULES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Venture Intelligence & Decision Navigation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid of all 13 modules
            val modules = OrvexaModuleId.entries
            val rows = modules.chunked(3)

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rows.forEach { rowModules ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowModules.forEach { module ->
                            val isSelected = (module == currentModule)
                            Surface(
                                onClick = { onSelectModule(module) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    module.accentColor.copy(alpha = if (isDark) 0.22f else 0.15f)
                                } else {
                                    if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF9FAFB)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) module.accentColor else (if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB))
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(86.dp)
                                    .testTag("modal_item_${module.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = module.icon,
                                        contentDescription = module.title,
                                        tint = if (isSelected) module.accentColor else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = module.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) module.accentColor else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        // Fill extra slot if row has fewer than 3 items
                        if (rowModules.size < 3) {
                            repeat(3 - rowModules.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
