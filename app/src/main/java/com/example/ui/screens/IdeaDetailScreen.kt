package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.ui.IdeaViewModel
import com.example.ui.components.VoiceInputIconButton
import com.example.ui.components.VoiceTargetField
import com.example.ui.components.rememberVoiceRecognition
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaDetailScreen(
    ideaId: String,
    viewModel: IdeaViewModel,
    onBack: () -> Unit,
    onOpenFullAnalyzer: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val idea by viewModel.getIdeaById(ideaId).collectAsState(initial = null)
    val isAdvisorThinking by viewModel.isAdvisorThinking.collectAsState()
    val isBenchmarkScoring by viewModel.isBenchmarkScoring.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }

    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)

    // Voice recognition helper
    val voiceController = rememberVoiceRecognition { _, text, _ ->
        chatInput = text
    }

    val quickDetailChips = listOf(
        "🎯 Stress-test unit economics",
        "⚡ What's our unfair moat?",
        "⚠️ Identify top 3 failure modes",
        "💰 Suggest 3 pricing models",
        "🧪 7-day validation plan"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = idea?.name ?: "Opportunity Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Export Evaluation Report as PDF
                    idea?.let { currentIdea ->
                        IconButton(
                            onClick = { com.example.ui.export.IdeaExportHelper.exportSingleIdeaEvaluationReportPdf(context, currentIdea) },
                            modifier = Modifier.testTag("detail_export_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Export Evaluation Report as PDF",
                                tint = primaryAccent
                            )
                        }
                    }

                    // Re-analyze with AI
                    IconButton(
                        onClick = { viewModel.reanalyzeIdea(ideaId) },
                        modifier = Modifier.testTag("detail_reanalyze_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Re-analyze with AI",
                            tint = primaryAccent
                        )
                    }

                    if (onOpenFullAnalyzer != null) {
                        IconButton(
                            onClick = { onOpenFullAnalyzer(ideaId) },
                            modifier = Modifier.testTag("detail_open_analyzer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Open Full Conversational Analyzer",
                                tint = primaryAccent
                            )
                        }
                    }

                    idea?.let { _ ->
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("detail_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Idea",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        val currentIdea = idea
        if (currentIdea == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Header Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF162032) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = currentIdea.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (currentIdea.recommendation) {
                                    Recommendation.BUILD_NOW -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    else -> primaryAccent.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = currentIdea.recommendation.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentIdea.recommendation) {
                                        Recommendation.BUILD_NOW -> Color(0xFF10B981)
                                        else -> primaryAccent
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentIdea.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentIdea.oneLineThesis.ifEmpty { currentIdea.description },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Score Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScorePill(
                                label = "OPPORTUNITY",
                                score = currentIdea.opportunityScore,
                                accentColor = Color(0xFF00E5FF),
                                modifier = Modifier.weight(1f)
                            )
                            ScorePill(
                                label = "TIMING",
                                score = currentIdea.timingScore,
                                accentColor = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            ScorePill(
                                label = "FEASIBILITY",
                                score = currentIdea.feasibilityScore,
                                accentColor = Color(0xFF6366F1),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section: Industry Benchmark Intelligence & Automated Scoring
                DetailSectionCard(
                    title = "INDUSTRY BENCHMARK EVALUATION (GEMINI INTELLIGENCE)",
                    icon = Icons.Default.Speed
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BENCHMARK SECTOR INDEX",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = currentIdea.benchmarkSector.ifEmpty { "Enterprise B2B Workflow SaaS" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = primaryAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${currentIdea.benchmarkPercentile}th %ile Standing",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Comparative Benchmark Deltas
                        val feasDelta = currentIdea.feasibilityScore - currentIdea.benchmarkFeasibilityMedian
                        val oppDelta = currentIdea.opportunityScore - currentIdea.benchmarkOpportunityMedian

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "FEASIBILITY vs MEDIAN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "${currentIdea.feasibilityScore}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF6366F1)
                                        )
                                        Text(
                                            text = " / ${currentIdea.benchmarkFeasibilityMedian}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (feasDelta >= 0) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (feasDelta >= 0) "+$feasDelta" else "$feasDelta",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = if (feasDelta >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "OPPORTUNITY vs MEDIAN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "${currentIdea.opportunityScore}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = primaryAccent
                                        )
                                        Text(
                                            text = " / ${currentIdea.benchmarkOpportunityMedian}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (oppDelta >= 0) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (oppDelta >= 0) "+$oppDelta" else "$oppDelta",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = if (oppDelta >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (currentIdea.benchmarkSummary.isNotEmpty()) {
                            Text(
                                text = currentIdea.benchmarkSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (currentIdea.benchmarkDifferentiator.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "Key Moat: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent
                                )
                                Text(
                                    text = currentIdea.benchmarkDifferentiator,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (currentIdea.benchmarkDownsideRisk.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "Downside Risk: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                                Text(
                                    text = currentIdea.benchmarkDownsideRisk,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.evaluateIdeaAgainstBenchmarks(currentIdea.id) },
                                enabled = !isBenchmarkScoring,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("run_benchmark_scoring_button")
                            ) {
                                if (isBenchmarkScoring) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scoring...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run Benchmark Scoring", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { com.example.ui.export.IdeaExportHelper.exportSingleIdeaEvaluationReportPdf(context, currentIdea) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_evaluation_pdf_button")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export PDF", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Section: Recommendation Reason
                if (currentIdea.recommendationReason.isNotEmpty()) {
                    DetailSectionCard(
                        title = "STRATEGIC RECOMMENDATION",
                        icon = Icons.Default.Lightbulb
                    ) {
                        Text(
                            text = currentIdea.recommendationReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Section: Why Now vs Why Not
                if (currentIdea.whyNow.isNotEmpty() || currentIdea.whyNot.isNotEmpty()) {
                    DetailSectionCard(
                        title = "TIMING DYNAMICS (WHY NOW)",
                        icon = Icons.Default.Timeline
                    ) {
                        if (currentIdea.whyNow.isNotEmpty()) {
                            Text(
                                text = "Market Tailwinds:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = currentIdea.whyNow,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        if (currentIdea.whyNot.isNotEmpty()) {
                            Text(
                                text = "Headwinds & Caveats:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = currentIdea.whyNot,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Section: Next Immediate Action
                if (currentIdea.nextImmediateAction.isNotEmpty()) {
                    DetailSectionCard(
                        title = "NEXT ACTIONABLE STEP",
                        icon = Icons.Default.CheckCircle
                    ) {
                        Text(
                            text = currentIdea.nextImmediateAction,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Section: Real-Time AI Chat Board & Venture Advisor
                DetailSectionCard(
                    title = "ORVEXA AI ADVISOR & LIVE CHAT BOARD",
                    icon = Icons.Default.Psychology
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Engage in real-time conversational analysis to stress-test unit economics, competitor threats, and go-to-market strategies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickDetailChips.forEach { chipText ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF162032) else Color(0xFFE2E8F0),
                                    border = BorderStroke(1.dp, cardBorder),
                                    modifier = Modifier.clickable {
                                        viewModel.sendAdvisorMessage(ideaId, chipText)
                                    }
                                ) {
                                    Text(
                                        text = chipText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chat History for this idea
                        if (currentIdea.chatHistory.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentIdea.chatHistory.takeLast(6).forEach { msg ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (msg.isUser) {
                                                if (isDark) Color(0xFF1E293B) else Color(0xFF0F172A)
                                            } else {
                                                if (isDark) Color(0xFF162032) else Color(0xFFE2E8F0)
                                            },
                                            modifier = Modifier.widthIn(max = 260.dp)
                                        ) {
                                            Text(
                                                text = (if (!msg.isUser) "🤖 Advisor: " else "") + msg.text,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 12.sp,
                                                color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (isAdvisorThinking) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = primaryAccent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Advisor analyzing venture telemetry...", fontSize = 11.sp, color = primaryAccent)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Message input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VoiceInputIconButton(
                                controller = voiceController,
                                targetField = VoiceTargetField.DESCRIPTION,
                                currentText = chatInput,
                                tint = primaryAccent
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Ask advisor about this idea...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        viewModel.sendAdvisorMessage(ideaId, chatInput)
                                        chatInput = ""
                                    }
                                },
                                enabled = chatInput.isNotBlank() && !isAdvisorThinking,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (chatInput.isNotBlank()) primaryAccent else cardBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (chatInput.isNotBlank()) Color.Black else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (onOpenFullAnalyzer != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            TextButton(
                                onClick = { onOpenFullAnalyzer(ideaId) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = primaryAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open in Full AI Analyzer", fontSize = 11.sp, color = primaryAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Opportunity Thesis?") },
            text = { Text("This will permanently remove \"${idea?.name}\" from the Room database. You can undo immediately after.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        idea?.let { viewModel.deleteIdea(it) }
                        showDeleteConfirm = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ScorePill(
    label: String,
    score: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
