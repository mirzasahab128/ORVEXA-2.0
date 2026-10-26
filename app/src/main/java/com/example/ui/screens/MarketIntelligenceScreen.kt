package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.remote.GeminiKeyConfig
import com.example.data.model.CompetitorItem
import com.example.data.model.Idea
import com.example.data.model.MarketGap
import com.example.ui.IdeaViewModel
import com.example.ui.components.IntelligenceScoreCard
import com.example.ui.components.ResearchGraph
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketIntelligenceScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val allIdeas by viewModel.allIdeas.collectAsState()

    var selectedIdeaId by remember { mutableStateOf<String?>("all") }
    var selectedFilter by remember { mutableStateOf("All Gaps") }

    val hasGeminiApiKey = GeminiKeyConfig.isConfigured(BuildConfig.GEMINI_API_KEY)

    val activeIdea = remember(selectedIdeaId, allIdeas) {
        if (selectedIdeaId == "all" || selectedIdeaId == null) null
        else allIdeas.find { it.id == selectedIdeaId }
    }

    val displayCompetitors: List<Pair<CompetitorItem, String>> = remember(selectedIdeaId, allIdeas) {
        if (activeIdea != null) {
            activeIdea.competitors.map { it to activeIdea.name }
        } else {
            allIdeas.flatMap { idea -> idea.competitors.map { it to idea.name } }
        }
    }

    val displayGaps: List<Pair<MarketGap, String>> = remember(selectedIdeaId, allIdeas) {
        if (activeIdea != null) {
            activeIdea.marketGaps.map { it to activeIdea.name }
        } else {
            allIdeas.flatMap { idea -> idea.marketGaps.map { it to idea.name } }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("market_intelligence_screen"),
        containerColor = MaterialTheme.colorScheme.background,
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
                            modifier = Modifier.testTag("market_intel_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Market Intelligence",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ModuleMarketIntelColor)
                                )
                            }
                            Text(
                                text = "Competitor positioning, market gaps & buyer objections",
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
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Venture Selector Pills
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FILTER VENTURE FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ModuleMarketIntelColor,
                        letterSpacing = 1.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedIdeaId == "all",
                                onClick = { selectedIdeaId = "all" },
                                label = { Text("All Portfolio (${allIdeas.size})") },
                                leadingIcon = {
                                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ModuleMarketIntelColor.copy(alpha = 0.2f),
                                    selectedLabelColor = ModuleMarketIntelColor
                                )
                            )
                        }

                        items(allIdeas) { idea ->
                            FilterChip(
                                selected = selectedIdeaId == idea.id,
                                onClick = { selectedIdeaId = idea.id },
                                label = { Text(idea.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ModuleMarketIntelColor.copy(alpha = 0.2f),
                                    selectedLabelColor = ModuleMarketIntelColor
                                )
                            )
                        }
                    }
                }
            }

            // Interactive Topological Research Graph (Ideas, Segments, Evidence)
            item {
                ResearchGraph(
                    ideas = allIdeas,
                    onNavigateToIdea = onNavigateToIdea,
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dynamic Intelligence Potential ScoreCard
            item {
                IntelligenceScoreCard(
                    ideas = allIdeas,
                    initialSelectedIdeaId = if (selectedIdeaId == "all") null else selectedIdeaId,
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Market Timing & Macro Signals Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = ModuleMarketIntelColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TIMING TAILWINDS & OPPORTUNITY WINDOW",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ModuleMarketIntelColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ModuleMarketIntelColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = activeIdea?.opportunityWindow?.label ?: "Portfolio Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ModuleMarketIntelColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = activeIdea?.timingSignalsSummary
                                ?: "High-frequency macro tailwinds: AI developer velocity, new compliance regulations, and vertical cloud consolidation across enterprise sectors.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WHY NOW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = activeIdea?.whyNow
                                        ?: "Emerging buyer urgency and automated tooling lower customer acquisition costs.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WHY NOT (DEFENSE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF43F5E)
                                )
                                Text(
                                    text = activeIdea?.whyNot
                                        ?: "Incumbent EHR/platform lock-in; requires strict compliance posture.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Competitor Analysis Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPETITOR POSITIONING MATRIX",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${displayCompetitors.size} Mapped",
                        fontSize = 12.sp,
                        color = ModuleMarketIntelColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (displayCompetitors.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF9FAFB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No competitors recorded for this selection. Add competitors via Idea Detail or AI Advisor.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(displayCompetitors) { (competitor, ventureName) ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = competitor.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Venture: $ventureName • ${competitor.positioning}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (competitor.type.lowercase()) {
                                        "direct" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                        "indirect" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        else -> Color(0xFF6B7280).copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = competitor.type.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when (competitor.type.lowercase()) {
                                            "direct" -> Color(0xFFEF4444)
                                            "indirect" -> Color(0xFFF59E0B)
                                            else -> Color(0xFF9CA3AF)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (competitor.pricing.isNotBlank()) {
                                Text(
                                    text = "Pricing model: ${competitor.pricing}",
                                    fontSize = 12.sp,
                                    color = ModuleMarketIntelColor,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (competitor.strengths.isNotBlank()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Strengths", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        Text(competitor.strengths, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (competitor.weaknesses.isNotBlank()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Exploitable Gap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrvexaOrange)
                                        Text(competitor.weaknesses, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Market Gaps & Unmet Customer Needs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UNMET OPPORTUNITY GAPS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${displayGaps.size} Gaps",
                        fontSize = 12.sp,
                        color = ModuleMarketIntelColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(displayGaps) { (gap, ventureName) ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = gap.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OrvexaOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Urgency: ${gap.urgency}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrvexaOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gap.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Defensibility: ${gap.defensibility}",
                                fontSize = 11.sp,
                                color = ModuleMarketIntelColor,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Validation: ${gap.validationScore}",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Venture: $ventureName",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Customer Objections Ledger
            if (activeIdea != null && activeIdea.customerObjections.isNotEmpty()) {
                item {
                    Text(
                        text = "CUSTOMER OBJECTIONS & BUYER FRICTION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                items(activeIdea.customerObjections) { objection ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF9FAFB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = objection,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // API Status / Real-time Deep Research Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1E1729) else Color(0xFFF3E8FF),
                    border = BorderStroke(1.dp, ModuleMarketIntelColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ModuleMarketIntelColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REAL-TIME GEMINI MARKET SCANNER",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ModuleMarketIntelColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (hasGeminiApiKey) {
                                "Connected to Gemini Pro engine. Real-time market scanner continuously evaluates regulatory shifts, funding rounds, and competitor moves."
                            } else {
                                "Gemini API key is not detected in environment secrets. Using local heuristic benchmarking. To enable real-time multimodal market scanning, provide your key in AI Studio Secrets panel."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
