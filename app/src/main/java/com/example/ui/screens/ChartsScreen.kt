package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.data.model.IdeaPromptSuggestion
import com.example.ui.IdeaViewModel
import com.example.ui.components.AddIdeaBottomSheet
import com.example.ui.components.IntelligenceScoreCard
import com.example.ui.components.OrvexaObsidianFab
import com.example.ui.components.ResearchGraph
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class TimeHorizon(val label: String) {
    MONTHLY("Monthly"),
    WEEKLY("Weekly"),
    QUARTERLY("Quarterly")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChartsScreen(
    viewModel: IdeaViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val ideas by viewModel.allIdeas.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedTimeHorizon by remember { mutableStateOf(TimeHorizon.MONTHLY) }
    var prefilledIdeaData by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var showAiSuggestions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isDark = themeMode == ThemeMode.DARK
    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    // Palette for categories
    val categoryColors = remember {
        mapOf(
            "Healthcare" to Color(0xFF00D4B2),
            "Marketplace" to Color(0xFFF59E0B),
            "Fintech" to Color(0xFF00B4D8),
            "AI" to Color(0xFF8B5CF6),
            "AI & Tech" to Color(0xFF8B5CF6),
            "SaaS" to Color(0xFFEC4899),
            "Enterprise" to Color(0xFF3B82F6),
            "Climate" to Color(0xFF10B981)
        )
    }

    fun getColorForCategory(cat: String): Color {
        return categoryColors[cat] ?: Color(0xFF6366F1)
    }

    // Analytics Metrics
    val totalIdeas = ideas.size
    val categoriesCount = ideas.map { it.category }.distinct().size
    val allActiveTags = remember(ideas) {
        ideas.flatMap { idea ->
            if (idea.categories.isNotEmpty()) idea.categories else listOf(idea.category, idea.tag)
        }.distinct().filter { it.isNotBlank() }
    }
    val topCategory = remember(ideas) {
        ideas.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key ?: "None"
    }

    // Category distribution calculations
    val categoryDistribution = remember(ideas) {
        val total = ideas.size.coerceAtLeast(1)
        ideas.groupBy { it.category }.map { (category, list) ->
            val count = list.size
            val percentage = (count.toFloat() / total) * 100f
            val avgScore = list.map { it.opportunityScore }.average().toInt()
            val tags = list.flatMap { it.categories.ifEmpty { listOf(it.tag) } }.distinct().take(4)
            CategoryShareItem(
                category = category,
                count = count,
                percentage = percentage,
                averageScore = avgScore,
                tags = tags,
                color = getColorForCategory(category)
            )
        }.sortedByDescending { it.count }
    }

    // Time buckets calculation for Category Frequency Breakdown Over Time
    val timeBuckets = remember(ideas, selectedTimeHorizon) {
        calculateTimeBuckets(ideas, selectedTimeHorizon)
    }

    // Cumulative capture points
    val cumulativePoints = remember(ideas, selectedTimeHorizon) {
        calculateCumulativePoints(ideas, selectedTimeHorizon)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            OrvexaObsidianFab(
                onClick = {
                    prefilledIdeaData = null
                    showBottomSheet = true
                },
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
                                text = "Analytics & Trends",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Visual telemetry, capture velocity & distributions",
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
                .testTag("charts_lazy_column"),
            contentPadding = PaddingValues(top = 10.dp, bottom = 110.dp)
        ) {

            // Top Header Hero Card matching Screenshot 2026-10-13-20-53-58-59
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ANALYTICS & TRENDS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = "$totalIdeas Ideas Tracked",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Idea Frequency by Category Over Time",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Track your ideation velocity, category distribution shifts, and spark fresh directions with Gemini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mini metric stat chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatChip(
                                label = "Top Category",
                                value = if (topCategory.length > 8) topCategory.take(7) + "..." else topCategory,
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                            StatChip(
                                label = "Categories",
                                value = "$categoriesCount Active",
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                            StatChip(
                                label = "Active Tags",
                                value = "${allActiveTags.size} Tags",
                                modifier = Modifier.weight(1f),
                                isDark = isDark
                            )
                        }
                    }
                }
            }

            // Dynamic IntelligenceScoreCard (Radar & Progress visualization with Compose Animation)
            item {
                IntelligenceScoreCard(
                    ideas = ideas,
                    isDark = isDark,
                    onTriggerAiEvaluation = { idea ->
                        viewModel.sendAdvisorMessage(
                            ideaId = idea.id,
                            userText = "Conduct a multi-criteria audit of ${idea.name} assessing market demand, feasibility, risk mitigation, and unit economics."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // D3.js Force & Native Canvas Research Graph (Connections between ideas, segments, and evidence)
            item {
                ResearchGraph(
                    ideas = ideas,
                    isDark = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Time Horizon Controls
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time Horizon:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TimeHorizon.values().forEach { horizon ->
                            val isSelected = selectedTimeHorizon == horizon
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                                } else {
                                    Color.Transparent
                                },
                                border = if (isSelected) BorderStroke(1.dp, primaryAccent) else null,
                                modifier = Modifier.clickable { selectedTimeHorizon = horizon }
                            ) {
                                Text(
                                    text = horizon.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Category Frequency Breakdown Over Time Chart Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Category Frequency Breakdown Over Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stacked Bar Chart View
                        StackedFrequencyChart(
                            buckets = timeBuckets,
                            isDark = isDark,
                            categoryColors = categoryColors
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = cardBorder, thickness = 0.5.dp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Categories in Chart:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Legend row with colored dots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            categoryDistribution.forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(item.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${item.category} (${item.count})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Share & Distribution Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Category Share & Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Proportion of total ideation volume across disciplines",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Multi-segment horizontal progress bar
                        MultiSegmentProgressBar(
                            distribution = categoryDistribution,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category items breakdown list
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            categoryDistribution.forEach { item ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(item.color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.category,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${item.count} ideas (${String.format(Locale.US, "%.1f", item.percentage)}%)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = item.color.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${item.averageScore} pts",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = item.color,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (item.tags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.tags.joinToString(" ") { "#$it" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cumulative Capture Velocity Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Cumulative Capture Velocity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Cumulative idea generation volume progression over time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = selectedTimeHorizon.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Velocity Line Chart
                        CumulativeVelocityChart(
                            points = cumulativePoints,
                            isDark = isDark
                        )
                    }
                }
            }

            // AI-Powered Idea Inspiration Card (Screenshot 2026-10-13-20-53-58-59)
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF0D1B2A) else Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini Idea Inspiration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "GEMINI 3.5",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Stuck on what to explore next? Let Gemini suggest high-conviction startup ideas tailored specifically to your active tags: ${allActiveTags.take(4).joinToString(", ") { "#$it" }}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showAiSuggestions = !showAiSuggestions },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF1E40AF) else Color(0xFF2563EB),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (showAiSuggestions) Icons.Default.KeyboardArrowUp else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showAiSuggestions) "Hide Tailored Ideas" else "Generate High-Conviction Idea Directions",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AnimatedVisibility(visible = showAiSuggestions) {
                            Column(
                                modifier = Modifier.padding(top = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val suggestions = listOf(
                                    IdeaPromptSuggestion(
                                        title = "CareSync AI Prior Auth",
                                        category = "Healthcare",
                                        tag = "B2B",
                                        prompt = "Zero-touch prior authorization automation extracting EHR clinical evidence to eliminate care delays."
                                    ),
                                    IdeaPromptSuggestion(
                                        title = "EscrowShield Global",
                                        category = "Fintech",
                                        tag = "Enterprise",
                                        prompt = "Programmable milestone escrow rails with real-time mid-market FX auditing for international trade contracts."
                                    ),
                                    IdeaPromptSuggestion(
                                        title = "BuildProof Dispatch",
                                        category = "Marketplace",
                                        tag = "Mobile",
                                        prompt = "Computer-vision subcontractor work verification releasing immediate draw payments upon job inspection."
                                    )
                                )

                                suggestions.forEach { suggestion ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color(0xFF111827) else Color.White,
                                        border = BorderStroke(1.dp, cardBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                prefilledIdeaData = Triple(suggestion.title, suggestion.prompt, suggestion.category)
                                                showBottomSheet = true
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = suggestion.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = getColorForCategory(suggestion.category).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = suggestion.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = getColorForCategory(suggestion.category),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = suggestion.prompt,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Tap to Adopt Idea →",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AddIdeaBottomSheet(
            onDismiss = {
                showBottomSheet = false
                prefilledIdeaData = null
            },
            onSaveIdea = { title, desc, cat, tag, priority, customTags ->
                viewModel.saveIdea(title, desc, cat, tag, priority, customTags)
                prefilledIdeaData = null
            }
        )
    }
}

// -------------------------------------------------------------
// Data Classes & Calculation Helpers
// -------------------------------------------------------------

data class CategoryShareItem(
    val category: String,
    val count: Int,
    val percentage: Float,
    val averageScore: Int,
    val tags: List<String>,
    val color: Color
)

data class TimeBucket(
    val label: String,
    val categoryCounts: Map<String, Int>,
    val totalCount: Int
)

data class CumulativePoint(
    val label: String,
    val cumulativeCount: Int
)

private fun calculateTimeBuckets(ideas: List<Idea>, horizon: TimeHorizon): List<TimeBucket> {
    if (ideas.isEmpty()) {
        return listOf(
            TimeBucket("Jul", emptyMap(), 0),
            TimeBucket("Aug", emptyMap(), 0),
            TimeBucket("Sep", emptyMap(), 0),
            TimeBucket("Oct", emptyMap(), 0)
        )
    }

    val monthFormat = SimpleDateFormat("MMM", Locale.US)
    val sortedIdeas = ideas.sortedBy { it.createdAt }

    // Map month names
    val monthsOrder = listOf("Jul", "Aug", "Sep", "Oct")
    val buckets = mutableListOf<TimeBucket>()

    monthsOrder.forEach { monthLabel ->
        val matchingIdeas = sortedIdeas.filter { idea ->
            val ideaMonth = monthFormat.format(Date(idea.createdAt))
            ideaMonth.equals(monthLabel, ignoreCase = true)
        }
        val counts = matchingIdeas.groupingBy { it.category }.eachCount()
        buckets.add(TimeBucket(monthLabel, counts, matchingIdeas.size))
    }

    return buckets
}

private fun calculateCumulativePoints(ideas: List<Idea>, horizon: TimeHorizon): List<CumulativePoint> {
    val monthsOrder = listOf("Jul", "Aug", "Sep", "Oct")
    val monthFormat = SimpleDateFormat("MMM", Locale.US)
    val points = mutableListOf<CumulativePoint>()
    var runningTotal = 0

    monthsOrder.forEach { monthLabel ->
        val countInMonth = ideas.count { idea ->
            monthFormat.format(Date(idea.createdAt)).equals(monthLabel, ignoreCase = true)
        }
        runningTotal += countInMonth
        points.add(CumulativePoint(monthLabel, runningTotal))
    }

    return points
}

// -------------------------------------------------------------
// UI Drawing & Chart Composables
// -------------------------------------------------------------

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MultiSegmentProgressBar(
    distribution: List<CategoryShareItem>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        if (distribution.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        } else {
            distribution.forEach { item ->
                if (item.percentage > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(item.percentage.coerceAtLeast(0.1f))
                            .background(item.color)
                    )
                }
            }
        }
    }
}

@Composable
private fun StackedFrequencyChart(
    buckets: List<TimeBucket>,
    isDark: Boolean,
    categoryColors: Map<String, Color>
) {
    val maxBarHeight = 160.dp
    val gridColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val maxCount = (buckets.maxOfOrNull { it.totalCount } ?: 1).coerceAtLeast(3)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxBarHeight)
        ) {
            // Horizontal reference grid lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lines = 3
                for (i in 0..lines) {
                    val y = size.height * (i.toFloat() / lines)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }
            }

            // Stacked Columns
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                buckets.forEach { bucket ->
                    val total = bucket.totalCount
                    val barFraction = if (maxCount > 0) total.toFloat() / maxCount.toFloat() else 0f

                    Column(
                        modifier = Modifier.width(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (total > 0) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .fillMaxHeight(barFraction.coerceIn(0.08f, 1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    bucket.categoryCounts.entries.forEach { (cat, count) ->
                                        val color = categoryColors[cat] ?: Color(0xFF6366F1)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(count.toFloat())
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Flat baseline for 0 count
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(3.dp)
                                    .background(gridColor)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Month Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            buckets.forEach { bucket ->
                Text(
                    text = bucket.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CumulativeVelocityChart(
    points: List<CumulativePoint>,
    isDark: Boolean
) {
    val chartHeight = 130.dp
    val primaryBlue = Color(0xFF3B82F6)
    val lightBlue = Color(0xFF60A5FA)
    val maxCount = (points.maxOfOrNull { it.cumulativeCount } ?: 5).coerceAtLeast(5)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.size < 2) return@Canvas

                val width = size.width
                val height = size.height
                val paddingX = 30f
                val availableWidth = width - 2 * paddingX
                val stepX = availableWidth / (points.size - 1)

                val coords = points.mapIndexed { index, p ->
                    val x = paddingX + index * stepX
                    val y = height - (p.cumulativeCount.toFloat() / maxCount.toFloat()) * (height - 30f) - 15f
                    Offset(x, y)
                }

                // Shaded gradient fill underneath
                val fillPath = Path().apply {
                    moveTo(coords.first().x, height)
                    coords.forEach { lineTo(it.x, it.y) }
                    lineTo(coords.last().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryBlue.copy(alpha = 0.35f),
                            primaryBlue.copy(alpha = 0.02f)
                        )
                    )
                )

                // Smooth connecting line
                val strokePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        lineTo(coords[i].x, coords[i].y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = lightBlue,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw node dots
                coords.forEach { pt ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = primaryBlue,
                        radius = 2.5.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month labels under curve
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
