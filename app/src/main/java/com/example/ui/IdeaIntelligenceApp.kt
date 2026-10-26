package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddIdeaBottomSheet
import com.example.ui.components.DockDestination
import com.example.ui.components.ExportIdeasDialog
import com.example.ui.components.OrbDock
import com.example.ui.components.OrbMoreSheet
import com.example.ui.components.QuickAnalyzeIdeaDialog
import com.example.ui.components.QuickPromptGeneratorDialog
import com.example.ui.screens.AIAnalyzerScreen
import com.example.ui.screens.ActionsScreen
import com.example.ui.screens.ChartsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExperimentsScreen
import com.example.ui.screens.IdeaDetailScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.MarketIntelligenceScreen
import com.example.ui.screens.MatrixScreen
import com.example.ui.screens.MyIdeasScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecycleBinScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.OrvexaTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.util.rememberHapticManager

@Composable
fun IdeaIntelligenceApp(
    viewModel: IdeaViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val ideas by viewModel.allIdeas.collectAsState()
    val deletedIdeas by viewModel.deletedIdeas.collectAsState()
    val orbState by viewModel.orbState.collectAsState()

    val hapticManager = rememberHapticManager()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var previousScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showQuickCaptureSheet by remember { mutableStateOf(false) }
    var showQuickPromptDialog by remember { mutableStateOf(false) }
    var showQuickAnalyzeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val isDark = themeMode == ThemeMode.DARK || themeMode == ThemeMode.NIGHT

    // BackHandler: dismiss sheet if open, else navigate back to previous/dashboard
    BackHandler(enabled = showMoreSheet) {
        showMoreSheet = false
    }

    BackHandler(enabled = !showMoreSheet && currentScreen != Screen.Dashboard) {
        if (currentScreen is Screen.Detail) {
            currentScreen = previousScreen
        } else {
            previousScreen = currentScreen
            currentScreen = Screen.Dashboard
        }
    }

    OrvexaTheme(themeMode = themeMode) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentScreen !is Screen.Detail) {
                    OrbDock(
                        currentDestination = when (currentScreen) {
                            is Screen.Dashboard -> DockDestination.HOME
                            is Screen.MyIdeas -> DockDestination.IDEAS
                            is Screen.MarketIntelligence -> DockDestination.INTEL
                            else -> DockDestination.HOME
                        },
                        orbState = orbState,
                        onNavigate = { destination ->
                            when (destination) {
                                DockDestination.HOME -> {
                                    if (currentScreen != Screen.Dashboard) {
                                        previousScreen = currentScreen
                                        currentScreen = Screen.Dashboard
                                    }
                                }
                                DockDestination.IDEAS -> {
                                    if (currentScreen != Screen.MyIdeas) {
                                        previousScreen = currentScreen
                                        currentScreen = Screen.MyIdeas
                                    }
                                }
                                DockDestination.INTEL -> {
                                    if (currentScreen != Screen.MarketIntelligence) {
                                        previousScreen = currentScreen
                                        currentScreen = Screen.MarketIntelligence
                                    }
                                }
                                DockDestination.MORE -> {
                                    showMoreSheet = true
                                }
                            }
                        },
                        onOrbClick = {
                            if (currentScreen != Screen.AIAdvisor) {
                                previousScreen = currentScreen
                                currentScreen = Screen.AIAdvisor
                            }
                        },
                        hapticManager = hapticManager
                    )
                }
            }
        ) { scaffoldPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
            ) {
                // Main Screen Content with Smooth Crossfade Animation
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "screen_navigation",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.Dashboard
                                    currentScreen = Screen.Detail(id)
                                },
                                onNavigateToScreen = { targetScreen ->
                                    previousScreen = Screen.Dashboard
                                    currentScreen = targetScreen
                                },
                                onOpenAdvisor = {
                                    previousScreen = Screen.Dashboard
                                    currentScreen = Screen.AIAdvisor
                                },
                                onToggleTheme = {
                                    viewModel.toggleTheme()
                                },
                                onOpenMore = {
                                    showMoreSheet = true
                                },
                                hapticManager = hapticManager
                            )
                        }
                        is Screen.MyIdeas -> {
                            MyIdeasScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.MyIdeas
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.AIAdvisor -> {
                            AIAnalyzerScreen(
                                viewModel = viewModel,
                                initialIdeaId = null,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.AIAdvisor
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                },
                                onBack = {
                                    currentScreen = Screen.Dashboard
                                }
                            )
                        }
                        is Screen.MarketIntelligence -> {
                            MarketIntelligenceScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.MarketIntelligence
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Experiments -> {
                            ExperimentsScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.Experiments
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Actions -> {
                            ActionsScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.Actions
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Reminders -> {
                            RemindersScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.Reminders
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Insights -> {
                            InsightsScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.Insights
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Charts -> {
                            ChartsScreen(
                                viewModel = viewModel,
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Matrix -> {
                            MatrixScreen(
                                viewModel = viewModel,
                                onNavigateToIdea = { id ->
                                    previousScreen = Screen.Matrix
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Profile -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.Settings -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onOpenDrawer = {
                                    showMoreSheet = true
                                }
                            )
                        }
                        is Screen.RecycleBin -> {
                            RecycleBinScreen(
                                deletedIdeas = deletedIdeas,
                                onRestoreIdea = { viewModel.restoreIdea(it) },
                                onDeletePermanently = { viewModel.deleteIdeaPermanently(it) },
                                onEmptyRecycleBin = { viewModel.emptyRecycleBin() },
                                onNavigateBack = {
                                    currentScreen = previousScreen
                                },
                                hapticManager = hapticManager
                            )
                        }
                        is Screen.AIAnalyzer -> {
                            AIAnalyzerScreen(
                                viewModel = viewModel,
                                initialIdeaId = screen.initialIdeaId,
                                onNavigateToIdea = { id ->
                                    previousScreen = currentScreen
                                    currentScreen = Screen.Detail(id)
                                },
                                onOpenDrawer = {
                                    showMoreSheet = true
                                },
                                onBack = {
                                    currentScreen = previousScreen
                                }
                            )
                        }
                        is Screen.Detail -> {
                            IdeaDetailScreen(
                                ideaId = screen.ideaId,
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = previousScreen
                                },
                                onOpenFullAnalyzer = { id ->
                                    previousScreen = currentScreen
                                    currentScreen = Screen.AIAnalyzer(id)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Spatial Glass Sheet replacing old drawer with a 3-column grid of all 13 modules
        OrbMoreSheet(
            isOpen = showMoreSheet,
            onDismiss = { showMoreSheet = false },
            onSelectScreen = { screen ->
                if (currentScreen != screen) {
                    previousScreen = currentScreen
                    currentScreen = screen
                }
                showMoreSheet = false
            },
            onNewIdeaClick = {
                showMoreSheet = false
                showQuickCaptureSheet = true
            },
            onExportClick = {
                showMoreSheet = false
                showExportDialog = true
            },
            hapticManager = hapticManager
        )

        // Quick Capture Bottom Sheet accessible from dock "New Idea" or sheet
        if (showQuickCaptureSheet) {
            AddIdeaBottomSheet(
                onDismiss = { showQuickCaptureSheet = false },
                onSaveIdea = { title, desc, cat, tag, priority, customTags ->
                    viewModel.saveIdea(title, desc, cat, tag, priority, customTags)
                }
            )
        }

        // Export Ideas Dialog
        if (showExportDialog) {
            ExportIdeasDialog(
                ideas = ideas,
                onDismiss = { showExportDialog = false }
            )
        }

        // Quick Analyze Idea Dialog
        if (showQuickAnalyzeDialog) {
            QuickAnalyzeIdeaDialog(
                ideas = ideas,
                onDismiss = { showQuickAnalyzeDialog = false },
                onSelectIdeaToAnalyze = { ideaId ->
                    previousScreen = currentScreen
                    currentScreen = Screen.AIAnalyzer(ideaId)
                },
                onOpenFullEvaluator = {
                    previousScreen = currentScreen
                    currentScreen = Screen.AIAdvisor
                },
                isDark = isDark
            )
        }

        // Quick Prompt Generator Dialog
        if (showQuickPromptDialog) {
            QuickPromptGeneratorDialog(
                onDismiss = { showQuickPromptDialog = false },
                onSelectPrompt = { promptText ->
                    viewModel.sendAdvisorMessage(
                        ideaId = ideas.firstOrNull()?.id,
                        userText = promptText,
                        scratchpadTitle = ideas.firstOrNull()?.name ?: "Venture Thesis"
                    )
                    previousScreen = currentScreen
                    currentScreen = Screen.AIAdvisor
                },
                onCustomGenerate = { keyword ->
                    viewModel.generateCustomPrompts(keyword)
                    previousScreen = currentScreen
                    currentScreen = Screen.AIAdvisor
                },
                isDark = isDark
            )
        }
    }
}
