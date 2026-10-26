package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.ui.IdeaViewModel
import com.example.ui.components.AddIdeaBottomSheet
import com.example.ui.components.OrvexaObsidianFab
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.ThemeMode
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val ideas by viewModel.allIdeas.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Toggle for Horizontal Dimension: Feasibility vs Timing
    var useFeasibilityDimension by remember { mutableStateOf(true) }

    // Selected idea for quadrant highlight and inspection bar
    var selectedIdeaId by remember { mutableStateOf<String?>(null) }

    // Auto-select first idea if none selected
    LaunchedEffect(ideas) {
        if (selectedIdeaId == null && ideas.isNotEmpty()) {
            selectedIdeaId = ideas.first().id
        }
    }

    val selectedIdea = remember(ideas, selectedIdeaId) {
        ideas.firstOrNull { it.id == selectedIdeaId } ?: ideas.firstOrNull()
    }

    val isDark = themeMode == ThemeMode.DARK
    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            OrvexaObsidianFab(
                onClick = { showBottomSheet = true },
                extended = true
            )
        },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, if (isDark) Color(0x2EFFFFFF) else Color(0xFFE2E8F0)),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = "Prioritization Matrix",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "2×2 Quadrants & Multi-factor comparison",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ThemeSwitcherButton(
                        currentTheme = themeMode,
                        onToggleTheme = { viewModel.toggleTheme() }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("matrix_lazy_column"),
            contentPadding = PaddingValues(top = 10.dp, bottom = 110.dp)
        ) {

            // Hero Banner Card matching Screenshot 2026-10-13-20-59-11-77
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF0F1E36) else Color(0xFFE0F2FE),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFBAE6FD)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Portfolio Intelligence & Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0369A1)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Compare multi-dimensional evidence, visualize 2×2 quadrants, and uncover cross-idea synergies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Visual Quadrant View Toggle Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visual Quadrant View",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (useFeasibilityDimension) "Feasibility" else "Timing",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = useFeasibilityDimension,
                            onCheckedChange = { useFeasibilityDimension = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryAccent,
                                checkedTrackColor = primaryAccent.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // 2×2 Quadrant Card with Interactive Dots
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (useFeasibilityDimension) "Opportunity × Feasibility Scatter-Plot Matrix" else "Opportunity × Timing Scatter-Plot Matrix",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(primaryAccent)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Interactive • Tap dots",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = primaryAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quadrant 2x2 Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF0B0F17) else Color(0xFFF8FAFC))
                        ) {
                            InteractiveQuadrantCanvas(
                                ideas = ideas,
                                selectedIdea = selectedIdea,
                                useFeasibility = useFeasibilityDimension,
                                isDark = isDark,
                                onSelectIdea = { selectedIdeaId = it.id }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Axis Labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "← Opportunity (0 - 100)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${if (useFeasibilityDimension) "Feasibility" else "Timing"} (0 - 100) →",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Selected Idea Inspector Bar
                        if (selectedIdea != null) {
                            val priorityColor = when (selectedIdea.priority.lowercase()) {
                                "high" -> Color(0xFFEF4444)
                                "medium" -> Color(0xFFF59E0B)
                                else -> Color(0xFF3B82F6)
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToIdea(selectedIdea.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedIdea.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = priorityColor.copy(alpha = 0.15f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(priorityColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = selectedIdea.priority.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = priorityColor
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = "Opp: ${selectedIdea.opportunityScore}   Feas: ${selectedIdea.feasibilityScore}   ${selectedIdea.recommendation.label.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = primaryAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = { onNavigateToIdea(selectedIdea.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "View Idea Details",
                                            tint = primaryAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizontal selectable pills for all ideas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ideas.forEach { idea ->
                                val isSelected = idea.id == selectedIdeaId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) primaryAccent.copy(alpha = 0.2f) else if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) primaryAccent else cardBorder),
                                    modifier = Modifier.clickable { selectedIdeaId = idea.id }
                                ) {
                                    Text(
                                        text = idea.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) primaryAccent else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Side-by-Side Factor Comparison Table Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Side-by-Side Factor Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Comprehensive multi-idea matrix (Rule #51)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Side-by-Side Factor Comparison Table Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    FactorComparisonTable(
                        ideas = ideas,
                        isDark = isDark,
                        primaryAccent = primaryAccent,
                        onSelectIdea = onNavigateToIdea
                    )
                }
            }

            // Cross-Idea Intelligence & Sequencing Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Cross-Idea Intelligence & Sequencing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Shared synergies and optimal roadmap sequencing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cross-Idea Synergy Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SynergyCard(
                        title = "Shared Healthcare Compliance Stack",
                        description = "MediClaim AI and VeriDoc Auto share HIPAA BAA enclaves and hospital credentialing networks. Pursuing MediClaim unlocks hospital compliance relationships for VeriDoc.",
                        isDark = isDark
                    )

                    SynergyCard(
                        title = "Cross-Border Escrow & Currency Margin Verification",
                        description = "TradeTrust Escrow and RemitClear share payment rail verification and anti-fraud telemetry. RemitClear FX spread audits can be integrated into TradeTrust international supplier payouts.",
                        isDark = isDark
                    )

                    SynergyCard(
                        title = "Mobile Contractor Workflow & Instant Escrow Sequencing",
                        description = "SiteForge Trades mobile quote generation pairs directly with TradeTrust milestone escrow, allowing contractors to convert accepted estimates into protected escrow contracts with one tap.",
                        isDark = isDark
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        AddIdeaBottomSheet(
            onDismiss = { showBottomSheet = false },
            onSaveIdea = { title, desc, cat, tag, priority, customTags ->
                viewModel.saveIdea(title, desc, cat, tag, priority, customTags)
            }
        )
    }
}

// -------------------------------------------------------------
// Interactive 2x2 Quadrant Canvas
// -------------------------------------------------------------

@Composable
private fun InteractiveQuadrantCanvas(
    ideas: List<Idea>,
    selectedIdea: Idea?,
    useFeasibility: Boolean,
    isDark: Boolean,
    onSelectIdea: (Idea) -> Unit
) {
    val axisColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val quadrantLabelColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val buildNowColor = Color(0xFF10B981)
    val validateColor = Color(0xFF00E5FF)
    val partnerColor = Color(0xFF8B5CF6)
    val watchColor = Color(0xFFF59E0B)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Quadrant Background Labels
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PARTNER / SCALE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = quadrantLabelColor,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "BUILD NOW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = buildNowColor,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "WATCH / DROP",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = quadrantLabelColor,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "QUICK TEST",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = quadrantLabelColor,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Draw Canvas with axes, gridlines, and interactive scatter points
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(ideas, useFeasibility) {
                    detectTapGestures { tapOffset ->
                        val padding = 28f
                        val plotW = size.width - 2 * padding
                        val plotH = size.height - 2 * padding

                        // Find closest idea dot to tap
                        val closest = ideas.minByOrNull { idea ->
                            val xVal = if (useFeasibility) idea.feasibilityScore else idea.timingScore
                            val yVal = idea.opportunityScore
                            val px = padding + (xVal / 100f) * plotW
                            val py = size.height - (padding + (yVal / 100f) * plotH)
                            hypot(tapOffset.x - px, tapOffset.y - py)
                        }

                        if (closest != null) {
                            onSelectIdea(closest)
                        }
                    }
                }
        ) {
            val padding = 28f
            val plotW = size.width - 2 * padding
            val plotH = size.height - 2 * padding
            val midX = size.width / 2f
            val midY = size.height / 2f

            // Subtle reference gridlines at 25%, 50%, 75%
            val gridFractions = listOf(0.25f, 0.5f, 0.75f)
            gridFractions.forEach { frac ->
                val gx = padding + frac * plotW
                val gy = size.height - (padding + frac * plotH)

                // Vertical gridline
                drawLine(
                    color = if (frac == 0.5f) axisColor else axisColor.copy(alpha = 0.35f),
                    start = Offset(gx, padding),
                    end = Offset(gx, size.height - padding),
                    strokeWidth = if (frac == 0.5f) 1.5f else 0.8f
                )

                // Horizontal gridline
                drawLine(
                    color = if (frac == 0.5f) axisColor else axisColor.copy(alpha = 0.35f),
                    start = Offset(padding, gy),
                    end = Offset(size.width - padding, gy),
                    strokeWidth = if (frac == 0.5f) 1.5f else 0.8f
                )
            }

            // Outer bounding box for scatter plot
            drawRect(
                color = axisColor.copy(alpha = 0.5f),
                topLeft = Offset(padding, padding),
                size = Size(plotW, plotH),
                style = Stroke(width = 1f)
            )

            // Plot Idea Scatter Points
            ideas.forEach { idea ->
                val xVal = if (useFeasibility) idea.feasibilityScore else idea.timingScore
                val yVal = idea.opportunityScore
                val px = padding + (xVal / 100f) * plotW
                val py = size.height - (padding + (yVal / 100f) * plotH)

                val dotColor = when (idea.recommendation) {
                    Recommendation.BUILD_NOW -> Color(0xFF10B981)
                    Recommendation.VALIDATE_FIRST -> Color(0xFF00E5FF)
                    Recommendation.BUILD_SMALL, Recommendation.PARTNER -> Color(0xFFF59E0B)
                    Recommendation.REPOSITION -> Color(0xFFF97316)
                    else -> Color(0xFF6366F1)
                }

                val isSelected = idea.id == selectedIdea?.id

                if (isSelected) {
                    // Outer glowing cyan selection ring
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 12.dp.toPx(),
                        center = Offset(px, py),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Inner glowing circle
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                        radius = 8.dp.toPx(),
                        center = Offset(px, py)
                    )
                    // Core dot
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )

                    // Draw crosshair projection to axes for selected scatter point
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                        start = Offset(px, py),
                        end = Offset(px, size.height - padding),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                        start = Offset(px, py),
                        end = Offset(padding, py),
                        strokeWidth = 1f
                    )
                } else {
                    // Regular scatter point with border
                    drawCircle(
                        color = dotColor,
                        radius = 5.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 5.5.dp.toPx(),
                        center = Offset(px, py),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Side-by-Side Factor Comparison Table Composable
// -------------------------------------------------------------

@Composable
private fun FactorComparisonTable(
    ideas: List<Idea>,
    isDark: Boolean,
    primaryAccent: Color,
    onSelectIdea: (String) -> Unit
) {
    val tableBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val headerBg = if (isDark) Color(0xFF162032) else Color(0xFFF1F5F9)

    val rows = listOf(
        "Recommendation" to { idea: Idea ->
            val label = idea.recommendation.label.uppercase()
            val color = when (idea.recommendation) {
                Recommendation.BUILD_NOW -> Color(0xFF10B981)
                Recommendation.VALIDATE_FIRST -> Color(0xFF00E5FF)
                Recommendation.BUILD_SMALL -> Color(0xFF38BDF8)
                Recommendation.REPOSITION -> Color(0xFFF59E0B)
                else -> Color(0xFF94A3B8)
            }
            label to color
        },
        "Opportunity" to { idea: Idea -> "${idea.opportunityScore}/100" to Color.Unspecified },
        "Timing" to { idea: Idea -> "${idea.timingScore}/100" to Color.Unspecified },
        "Feasibility" to { idea: Idea -> "${idea.feasibilityScore}/100" to Color.Unspecified },
        "Market" to { idea: Idea -> "${idea.marketScore}/100" to Color.Unspecified },
        "Personal Fit" to { idea: Idea -> "${idea.personalFitScore}/100" to Color.Unspecified },
        "Health" to { idea: Idea ->
            val trend = when {
                idea.healthTrend.startsWith("Imp") -> "Impr"
                idea.healthTrend.startsWith("Dec") -> "Decl"
                else -> "Stable"
            }
            "${idea.healthScore} ($trend)" to Color.Unspecified
        },
        "Saturation" to { idea: Idea ->
            idea.marketSaturation.take(11) to Color.Unspecified
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .background(headerBg, RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Text(
                text = "Factor",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(130.dp)
            )

            ideas.forEach { idea ->
                Text(
                    text = idea.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryAccent,
                    modifier = Modifier
                        .width(135.dp)
                        .clickable { onSelectIdea(idea.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Table Rows
        rows.forEachIndexed { index, (factorName, extractor) ->
            val rowBg = if (index % 2 == 1) {
                if (isDark) Color(0xFF0D131F) else Color(0xFFF8FAFC)
            } else {
                Color.Transparent
            }

            Row(
                modifier = Modifier
                    .background(rowBg, RoundedCornerShape(6.dp))
                    .padding(vertical = 9.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = factorName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(130.dp)
                )

                ideas.forEach { idea ->
                    val (value, customColor) = extractor(idea)
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (factorName == "Recommendation") FontWeight.Bold else FontWeight.Normal,
                        color = if (customColor != Color.Unspecified) customColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(135.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Cross-Idea Synergy Card Composable
// -------------------------------------------------------------

@Composable
private fun SynergyCard(
    title: String,
    description: String,
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF0F1E36) else Color(0xFFF0FDF4),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFBBF7D0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFF60A5FA) else Color(0xFF166534)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
