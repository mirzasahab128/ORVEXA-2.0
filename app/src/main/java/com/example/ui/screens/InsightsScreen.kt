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
import com.example.data.model.EpistemicClaim
import com.example.data.model.EpistemicType
import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.ui.IdeaViewModel
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val allIdeas by viewModel.allIdeas.collectAsState()

    var selectedIdeaId by remember { mutableStateOf("ALL") }
    var epistemicTypeFilter by remember { mutableStateOf("ALL") }

    data class ClaimEntry(val claim: EpistemicClaim, val ideaId: String, val ideaName: String)

    val allClaims = remember(allIdeas) {
        allIdeas.flatMap { idea ->
            idea.epistemicClaims.map { c -> ClaimEntry(c, idea.id, idea.name) }
        }
    }

    val filteredClaims = remember(allClaims, selectedIdeaId, epistemicTypeFilter) {
        allClaims.filter { entry ->
            val matchIdea = if (selectedIdeaId == "ALL") true else entry.ideaId == selectedIdeaId
            val matchType = if (epistemicTypeFilter == "ALL") true else entry.claim.type.name.equals(epistemicTypeFilter, ignoreCase = true)
            matchIdea && matchType
        }
    }

    val buildNowIdeas = remember(allIdeas) { allIdeas.filter { it.recommendation == Recommendation.BUILD_NOW } }
    val validateFirstIdeas = remember(allIdeas) { allIdeas.filter { it.recommendation == Recommendation.VALIDATE_FIRST } }
    val pivotIdeas = remember(allIdeas) { allIdeas.filter { it.recommendation == Recommendation.PIVOT || it.recommendation == Recommendation.ABANDON } }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("insights_screen"),
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
                            modifier = Modifier.testTag("insights_drawer_button")
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
                                    text = "Strategic Insights",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ModuleInsightsColor)
                                )
                            }
                            Text(
                                text = "Evidence claims, strategic pivots & portfolio signals",
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

            // Strategic Recommendation Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PORTFOLIO STRATEGIC CONVICTION",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ModuleInsightsColor,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("BUILD NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    Text("${buildNowIdeas.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                    Text("Immediate greenlight", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("VALIDATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                    Text("${validateFirstIdeas.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF59E0B))
                                    Text("Run smoke test first", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("PIVOT / WAIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    Text("${pivotIdeas.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                                    Text("Reframe thesis", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Epistemic Claims Filter Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EPISTEMIC CLAIMS LEDGER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${filteredClaims.size} Claims",
                        fontSize = 12.sp,
                        color = ModuleInsightsColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val types = listOf("ALL", "FACT", "USER_INPUT", "ASSUMPTION", "INFERENCE")
                    items(types) { t ->
                        val isSelected = epistemicTypeFilter == t
                        FilterChip(
                            selected = isSelected,
                            onClick = { epistemicTypeFilter = t },
                            label = { Text(if (t == "ALL") "All Types" else t.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ModuleInsightsColor.copy(alpha = 0.2f),
                                selectedLabelColor = ModuleInsightsColor
                            )
                        )
                    }
                }
            }

            // Claims Items
            if (filteredClaims.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF9FAFB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No epistemic claims matched this filter.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredClaims) { entry ->
                    val claim = entry.claim
                    val typeBadgeColor = when (claim.type) {
                        EpistemicType.FACT -> Color(0xFF10B981)
                        EpistemicType.USER_INPUT -> Color(0xFF3B82F6)
                        EpistemicType.ASSUMPTION -> Color(0xFFF59E0B)
                        EpistemicType.INFERENCE -> ModuleInsightsColor
                        EpistemicType.UNKNOWN -> Color(0xFF9CA3AF)
                    }

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
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = typeBadgeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = claim.type.label.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = typeBadgeColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF3F4F6)
                                ) {
                                    Text(
                                        text = "Confidence: ${claim.confidence}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = claim.claim,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (claim.sourceOrEvidence.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Source,
                                        contentDescription = null,
                                        tint = ModuleInsightsColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Evidence: ${claim.sourceOrEvidence}",
                                        fontSize = 11.sp,
                                        color = ModuleInsightsColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Venture: ${entry.ideaName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = { onNavigateToIdea(entry.ideaId) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Analyze", fontSize = 11.sp)
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Downside Risks & Strategic Kill Triggers
            item {
                Text(
                    text = "KILL CRITERIA & STRATEGIC PIVOT TRIGGERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            items(allIdeas) { idea ->
                if (idea.whatWouldChangeMind.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = idea.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrvexaOrange
                                )
                                Text(
                                    text = "Recommendation: ${idea.recommendation.label}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "What Would Invalidate: ${idea.whatWouldChangeMind}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
