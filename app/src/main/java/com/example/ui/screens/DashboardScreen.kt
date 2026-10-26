package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionItem
import com.example.data.model.Idea
import com.example.ui.IdeaViewModel
import com.example.ui.Screen
import com.example.ui.components.EnhancedIdeaCard
import com.example.ui.components.GlassBackdrop
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassTextField
import com.example.ui.components.OrbCore
import com.example.ui.components.OrbMode
import com.example.ui.components.OrbState
import com.example.ui.components.OrbTopBar
import com.example.ui.components.ReasoningStep
import com.example.ui.components.RingMeter
import com.example.ui.components.SegmentedModes
import com.example.ui.components.StatTile
import com.example.ui.components.StepStatus
import com.example.ui.components.UndoSnackbarListener
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.util.HapticManager
import kotlinx.coroutines.launch

/**
 * ORVEXA Home Dashboard: The Signature Orb Core Spatial Experience.
 * Features 220dp Hero Orb, live segmented modes, live reasoning log,
 * portfolio pulse concentric rings, 2-column bento tiles, and module previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit,
    onNavigateToScreen: (Screen) -> Unit,
    onOpenAdvisor: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenMore: () -> Unit,
    hapticManager: HapticManager? = null,
    modifier: Modifier = Modifier
) {
    val tokens = AppTheme.tokens
    val allIdeas by viewModel.allIdeas.collectAsState()
    val filteredIdeas by viewModel.filteredIdeas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lastDeletedIdea by viewModel.lastDeletedIdea.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var orbMode by remember { mutableStateOf(OrbMode.ANALYZE) }
    var ideaInputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var rmsLevel by remember { mutableFloatStateOf(0f) }

    val activeOrbState = when {
        isListening -> OrbState.LISTENING
        orbMode == OrbMode.ANALYZE -> OrbState.ANALYZING
        orbMode == OrbMode.RESEARCH -> OrbState.RESEARCHING
        orbMode == OrbMode.COMPARE -> OrbState.COMPARING
        orbMode == OrbMode.VALIDATE -> OrbState.VALIDATING
        else -> OrbState.IDLE
    }

    // Reasoning pipeline steps per mode
    val reasoningSteps = remember(orbMode) {
        when (orbMode) {
            OrbMode.ANALYZE -> listOf(
                "Problem statement extracted",
                "Target customer segmented",
                "Revenue model mapped",
                "Core assumptions listed"
            )
            OrbMode.RESEARCH -> listOf(
                "Scanning market sources",
                "Collecting competitor signals",
                "Reading customer discussions",
                "Logging search signals"
            )
            OrbMode.COMPARE -> listOf(
                "Matching similar products",
                "Scoring feature overlap",
                "Finding pricing gaps",
                "Ranking defensibility position"
            )
            OrbMode.VALIDATE -> listOf(
                "Testing demand claims",
                "Checking evidence strength",
                "Flagging weak assumptions",
                "Preparing execution verdict"
            )
        }
    }

    // 5-second persistent Undo Snackbar
    UndoSnackbarListener(
        lastDeletedIdea = lastDeletedIdea,
        snackbarHostState = snackbarHostState,
        onUndo = { viewModel.undoDelete() },
        onDismiss = { viewModel.clearUndo() },
        hapticManager = hapticManager
    )

    GlassBackdrop(modifier = modifier) {
        Scaffold(
            topBar = {
                OrbTopBar(
                    remindersCount = 3,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onToggleTheme = onToggleTheme,
                    onRemindersClick = { onNavigateToScreen(Screen.Reminders) },
                    onAvatarClick = { onNavigateToScreen(Screen.Profile) },
                    hapticManager = hapticManager
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HERO CARD (Radius 36dp, Centered OrbCore 220dp)
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 36.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Orb Core (220dp)
                            Box(
                                modifier = Modifier
                                    .size(252.dp)
                                    .clickable {
                                        hapticManager?.performClick()
                                        onOpenAdvisor()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                OrbCore(
                                    state = activeOrbState,
                                    size = 220.dp,
                                    rmsLevel = rmsLevel,
                                    enableParallax = true
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Status Label
                            Text(
                                text = "ORVEXA IS ${orbMode.name}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.18.sp
                                ),
                                color = tokens.textLabel
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Title (30sp)
                            Text(
                                text = orbMode.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.02).sp
                                ),
                                color = tokens.textPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Description (15sp, two lines at most)
                            Text(
                                text = orbMode.desc,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                ),
                                color = tokens.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            // Mode Selector (Segmented 4 items, 48dp height)
                            SegmentedModes(
                                selectedMode = orbMode,
                                onModeSelected = { newMode ->
                                    orbMode = newMode
                                },
                                hapticManager = hapticManager
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Input Row (56dp Glass Pill, mic & send buttons)
                            GlassTextField(
                                value = ideaInputText,
                                onValueChange = { ideaInputText = it },
                                placeholder = "Describe your idea...",
                                trailingAction = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Voice Dictation Button (48dp, orange while listening)
                                        IconButton(
                                            onClick = {
                                                hapticManager?.performClick()
                                                isListening = !isListening
                                                rmsLevel = if (isListening) 5.5f else 0f
                                            },
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isListening) tokens.modeListening else tokens.inputChipFill
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = if (isListening) "Stop voice input" else "Start voice input",
                                                tint = if (isListening) Color.White else tokens.textPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Send Button (48dp)
                                        IconButton(
                                            onClick = {
                                                if (ideaInputText.isNotBlank()) {
                                                    hapticManager?.performSuccess()
                                                    viewModel.saveIdea(
                                                        title = ideaInputText.trim(),
                                                        description = "Initiated from Orb Core Hero input in ${orbMode.label} mode.",
                                                        customTags = listOf(orbMode.label),
                                                        strategicHypothesis = orbMode.desc
                                                    )
                                                    ideaInputText = ""
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Idea saved & queued for evaluation")
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(tokens.primaryButtonFill)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Submit idea",
                                                tint = tokens.primaryButtonText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // 2. REASONING LOG CARD
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "REASONING LOG",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.16.sp
                                    ),
                                    color = tokens.textLabel
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = tokens.modeValidate.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Active Pipeline",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = tokens.modeValidate,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            reasoningSteps.forEachIndexed { index, stepText ->
                                val status = when {
                                    index < 2 -> StepStatus.DONE
                                    index == 2 -> StepStatus.ACTIVE
                                    else -> StepStatus.PENDING
                                }
                                ReasoningStep(
                                    stepNumber = index + 1,
                                    text = stepText,
                                    status = status,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 3. IDEA SNAPSHOT CARD (3 Meters)
                item {
                    val latestIdea = allIdeas.firstOrNull()
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "IDEA SNAPSHOT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.16.sp
                                ),
                                color = tokens.textLabel
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = latestIdea?.name ?: "No idea selected",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tokens.textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Meter 1: Demand Signal
                            SnapshotMeter(
                                label = "Demand Signal",
                                statusText = if (latestIdea != null) "Verified (78%)" else "No data yet",
                                progress = if (latestIdea != null) 0.78f else 0f,
                                gradient = listOf(Color(0xFF7D8CFF), Color(0xFF5EF0E0))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Meter 2: Competition
                            SnapshotMeter(
                                label = "Competition",
                                statusText = if (latestIdea != null) "Moderate (48%)" else "No data yet",
                                progress = if (latestIdea != null) 0.48f else 0f,
                                gradient = listOf(Color(0xFFFF7AC6), Color(0xFFFF9A4D))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Meter 3: Evidence Strength
                            SnapshotMeter(
                                label = "Evidence Strength",
                                statusText = if (latestIdea != null) "Strong (65%)" else "No data yet",
                                progress = if (latestIdea != null) 0.65f else 0f,
                                gradient = listOf(Color(0xFF7DFFB2), Color(0xFF18A96A))
                            )
                        }
                    }
                }

                // 4. PORTFOLIO PULSE CARD (3 Concentric Rings)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PORTFOLIO PULSE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.16.sp
                                    ),
                                    color = tokens.textLabel
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                PulseStatRow("Validation", "82%", tokens.modeValidate)
                                PulseStatRow("Execution", "64%", tokens.modeResearch)
                                PulseStatRow("Readiness", "71%", tokens.modeAnalyze)
                            }

                            RingMeter(
                                progressValues = listOf(0.82f, 0.64f, 0.71f),
                                colors = listOf(tokens.modeValidate, tokens.modeResearch, tokens.modeAnalyze),
                                size = 96.dp,
                                strokeWidth = 5.dp
                            )
                        }
                    }
                }

                // 5. BENTO TILES (2 Columns, Average Intelligence tile spans 2)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Spanning Tile: Average Intelligence Score
                        StatTile(
                            title = "Average Intelligence Score",
                            value = if (allIdeas.isNotEmpty()) "${(allIdeas.map { it.feasibilityScore }.average()).toInt()}%" else "78%",
                            subtitle = "Calculated across ${allIdeas.size} saved concepts with AI validation",
                            icon = Icons.Default.AutoAwesome,
                            onClick = { onNavigateToScreen(Screen.Charts) }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatTile(
                                title = "Total Ideas",
                                value = "${allIdeas.size}",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToScreen(Screen.MyIdeas) }
                            )
                            StatTile(
                                title = "Build Now",
                                value = "${allIdeas.count { it.status.name == "BUILDING" || it.opportunityScore > 75 }}",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToScreen(Screen.Actions) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatTile(
                                title = "Active Tests",
                                value = "4",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToScreen(Screen.Experiments) }
                            )
                            StatTile(
                                title = "Open Actions",
                                value = "7",
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToScreen(Screen.Actions) }
                            )
                        }
                    }
                }

                // 6. TOP IDEAS CAROUSEL (Horizontal Scroll)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOP IDEAS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.16.sp
                                ),
                                color = tokens.textLabel
                            )
                            Text(
                                text = "View All (${allIdeas.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = tokens.modeAnalyze,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.clickable { onNavigateToScreen(Screen.MyIdeas) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (allIdeas.isEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No ideas saved yet. Enter an idea above to begin.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.textSecondary
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(end = 12.dp)
                            ) {
                                items(allIdeas.take(5), key = { it.id }) { idea ->
                                    EnhancedIdeaCard(
                                        idea = idea,
                                        onClick = { onNavigateToIdea(idea.id) },
                                        hapticManager = hapticManager,
                                        modifier = Modifier.width(300.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. MATRIX PREVIEW (2x2 Quadrant)
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToScreen(Screen.Matrix) }
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EFFORT VS IMPACT MATRIX",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.16.sp
                                    ),
                                    color = tokens.textLabel
                                )
                                Icon(
                                    imageVector = Icons.Default.ViewQuilt,
                                    contentDescription = null,
                                    tint = tokens.modeCompare,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "4 Quick Wins identified in high impact / low effort quadrant.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnapshotMeter(
    label: String,
    statusText: String,
    progress: Float,
    gradient: List<Color>
) {
    val tokens = AppTheme.tokens
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = tokens.textPrimary
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(tokens.inputChipFill)
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(brush = Brush.horizontalGradient(gradient))
                )
            }
        }
    }
}

@Composable
private fun PulseStatRow(label: String, value: String, accentColor: Color) {
    val tokens = AppTheme.tokens
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = tokens.textPrimary
        )
    }
}
