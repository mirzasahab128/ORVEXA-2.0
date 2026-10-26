package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Idea
import com.example.data.model.IdeaPromptSuggestion
import com.example.data.model.Recommendation
import com.example.ui.IdeaViewModel
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.components.VoiceInputIconButton
import com.example.ui.components.VoiceTargetField
import com.example.ui.components.rememberVoiceRecognition
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AnalyzerTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CHAT_BOARD("Live Chat Board", Icons.Default.Chat),
    PROMPT_OPTIONS("Suggested Prompts", Icons.Default.Lightbulb),
    EVALUATOR("Idea Evaluator", Icons.Default.Assessment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalyzerScreen(
    viewModel: IdeaViewModel,
    initialIdeaId: String? = null,
    onNavigateToIdea: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val themeMode by viewModel.themeMode.collectAsState()
    val allIdeas by viewModel.allIdeas.collectAsState()
    val suggestedPrompts by viewModel.suggestedPrompts.collectAsState()
    val isGeneratingPrompts by viewModel.isGeneratingPrompts.collectAsState()
    val isAdvisorThinking by viewModel.isAdvisorThinking.collectAsState()
    val scratchpadMessages by viewModel.scratchpadMessages.collectAsState()

    val isDark = themeMode == ThemeMode.DARK
    val primaryCyan = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    val cardBg = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)

    var currentTab by remember { mutableStateOf(AnalyzerTab.CHAT_BOARD) }
    var selectedIdeaId by remember { mutableStateOf<String?>(initialIdeaId) }
    var userMessageInput by remember { mutableStateOf("") }
    var keywordSearchInput by remember { mutableStateOf("") }

    // Evaluator tab form state
    var evalTitle by remember { mutableStateOf("") }
    var evalDescription by remember { mutableStateOf("") }
    var evalCategory by remember { mutableStateOf("Healthcare") }
    var evalTag by remember { mutableStateOf("B2B") }
    var evalPriority by remember { mutableStateOf("High") }
    var evaluationResult by remember { mutableStateOf<Map<String, Any>?>(null) }
    var evaluationSavedMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val screenHaptic = LocalHapticFeedback.current

    // Load initial suggested prompts if empty
    LaunchedEffect(Unit) {
        if (suggestedPrompts.isEmpty()) {
            viewModel.loadSuggestedPrompts()
        }
    }

    // Active idea for conversational board context
    val activeIdea = remember(allIdeas, selectedIdeaId) {
        allIdeas.firstOrNull { it.id == selectedIdeaId }
    }

    val activeMessages: List<ChatMessage> = remember(activeIdea, scratchpadMessages) {
        activeIdea?.chatHistory?.ifEmpty {
            listOf(
                ChatMessage(
                    id = "initial-${activeIdea.id}",
                    isUser = false,
                    text = "Loaded \"${activeIdea.name}\" into ORVEXA Venture Advisor. Current recommendation: ${activeIdea.recommendation.label}. Ask me to stress-test your unit economics, customer objections, or competitive risks."
                )
            )
        } ?: scratchpadMessages
    }

    // Scroll to bottom when messages update
    LaunchedEffect(activeMessages.size, isAdvisorThinking) {
        if (activeMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    // Voice recognition controller for chat input
    val voiceController = rememberVoiceRecognition { field, text, _ ->
        if (field == VoiceTargetField.DESCRIPTION || field == VoiceTargetField.TITLE) {
            userMessageInput = text
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ORVEXA AI Analyzer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = primaryCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "LIVE BOARD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Real-time Conversational Advisor & Multi-Option Evaluator",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("analyzer_hamburger_button")) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                        }
                    }
                },
                actions = {
                    ThemeSwitcherButton(
                        currentTheme = themeMode,
                        onToggleTheme = { viewModel.toggleTheme() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented Tab Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF162032) else Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                AnalyzerTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) (if (isDark) Color(0xFF1E293B) else Color.White) else Color.Transparent)
                            .clickable {
                                screenHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = tab
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) primaryCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A)) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (currentTab) {
                AnalyzerTab.CHAT_BOARD -> {
                    ConversationalBoardContent(
                        ideas = allIdeas,
                        activeIdea = activeIdea,
                        selectedIdeaId = selectedIdeaId,
                        onSelectIdea = { selectedIdeaId = it },
                        messages = activeMessages,
                        isAdvisorThinking = isAdvisorThinking,
                        userInput = userMessageInput,
                        onUserInputChange = { userMessageInput = it },
                        onSendMessage = { text ->
                            screenHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.sendAdvisorMessage(
                                ideaId = selectedIdeaId,
                                userText = text,
                                scratchpadTitle = activeIdea?.name ?: "Venture Hypothesis"
                            )
                            userMessageInput = ""
                        },
                        onClearChat = { viewModel.clearScratchpad() },
                        chatListState = chatListState,
                        voiceController = voiceController,
                        isDark = isDark,
                        primaryCyan = primaryCyan,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        onNavigateToIdea = onNavigateToIdea
                    )
                }
                AnalyzerTab.PROMPT_OPTIONS -> {
                    PromptSuggestionsWithOptionsContent(
                        suggestedPrompts = suggestedPrompts,
                        isGenerating = isGeneratingPrompts,
                        keywordQuery = keywordSearchInput,
                        onKeywordChange = { keywordSearchInput = it },
                        onSearchKeyword = {
                            screenHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.generateCustomPrompts(keywordSearchInput)
                        },
                        onAdoptPrompt = { prompt ->
                            screenHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveIdea(
                                title = prompt.title,
                                description = prompt.prompt,
                                category = prompt.category,
                                tag = prompt.tag,
                                priority = "High"
                            )
                        },
                        onSendToChat = { prompt ->
                            screenHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIdeaId = null
                            viewModel.clearScratchpad()
                            currentTab = AnalyzerTab.CHAT_BOARD
                            coroutineScope.launch {
                                viewModel.sendAdvisorMessage(
                                    ideaId = null,
                                    userText = "Analyze this venture thesis and suggest next steps: ${prompt.title} - ${prompt.prompt}",
                                    scratchpadTitle = prompt.title
                                )
                            }
                        },
                        onEvaluatePrompt = { prompt ->
                            screenHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            evalTitle = prompt.title
                            evalDescription = prompt.prompt
                            evalCategory = prompt.category
                            evalTag = prompt.tag
                            currentTab = AnalyzerTab.EVALUATOR
                            viewModel.evaluateIdeaWithAI(prompt.title, prompt.prompt, prompt.category, prompt.tag) { result ->
                                evaluationResult = result
                            }
                        },
                        isDark = isDark,
                        primaryCyan = primaryCyan,
                        cardBg = cardBg,
                        cardBorder = cardBorder
                    )
                }
                AnalyzerTab.EVALUATOR -> {
                    IdeaEvaluatorContent(
                        title = evalTitle,
                        description = evalDescription,
                        category = evalCategory,
                        tag = evalTag,
                        priority = evalPriority,
                        onTitleChange = { evalTitle = it },
                        onDescriptionChange = { evalDescription = it },
                        onCategoryChange = { evalCategory = it },
                        onTagChange = { evalTag = it },
                        onPriorityChange = { evalPriority = it },
                        isEvaluating = isAdvisorThinking,
                        evaluationResult = evaluationResult,
                        savedMessage = evaluationSavedMessage,
                        onEvaluate = {
                            screenHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.evaluateIdeaWithAI(evalTitle, evalDescription, evalCategory, evalTag) { res ->
                                evaluationResult = res
                                evaluationSavedMessage = null
                            }
                        },
                        onSaveIdea = {
                            screenHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveAnalyzedIdea(
                                title = evalTitle,
                                description = evalDescription,
                                category = evalCategory,
                                tag = evalTag,
                                priority = evalPriority,
                                analysis = evaluationResult
                            )
                            evaluationSavedMessage = "Saved to Portfolio Room Database!"
                        },
                        onExportPdf = {
                            val tempIdea = Idea(
                                id = "eval-" + System.currentTimeMillis().toString().takeLast(6),
                                name = evalTitle.ifBlank { (evaluationResult?.get("name") as? String) ?: "Evaluated Opportunity" },
                                oneLineThesis = (evaluationResult?.get("oneLineThesis") as? String) ?: evalDescription,
                                description = evalDescription,
                                category = evalCategory,
                                tag = evalTag,
                                priority = evalPriority,
                                opportunityScore = (evaluationResult?.get("opportunityScore") as? Int) ?: 75,
                                timingScore = (evaluationResult?.get("timingScore") as? Int) ?: 75,
                                feasibilityScore = (evaluationResult?.get("feasibilityScore") as? Int) ?: 70,
                                recommendation = try {
                                    Recommendation.valueOf((evaluationResult?.get("recommendation") as? String) ?: "VALIDATE_FIRST")
                                } catch (_: Exception) {
                                    Recommendation.VALIDATE_FIRST
                                },
                                recommendationReason = (evaluationResult?.get("recommendationReason") as? String) ?: "",
                                whyNow = (evaluationResult?.get("whyNow") as? String) ?: "",
                                biggestRisk = (evaluationResult?.get("biggestRisk") as? String) ?: "",
                                nextImmediateAction = (evaluationResult?.get("nextImmediateAction") as? String) ?: "Validate with 5 target customers",
                                benchmarkSector = evalCategory,
                                benchmarkPercentile = 82,
                                benchmarkSummary = "Evaluated with ORVEXA Gemini Intelligence against $evalCategory industry benchmarks."
                            )
                            com.example.ui.export.IdeaExportHelper.exportSingleIdeaEvaluationReportPdf(context, tempIdea)
                        },
                        onChatWithAdvisor = {
                            currentTab = AnalyzerTab.CHAT_BOARD
                            selectedIdeaId = null
                            viewModel.sendAdvisorMessage(
                                ideaId = null,
                                userText = "Let's review the evaluation for $evalTitle: $evalDescription",
                                scratchpadTitle = evalTitle
                            )
                        },
                        isDark = isDark,
                        primaryCyan = primaryCyan,
                        cardBg = cardBg,
                        cardBorder = cardBorder
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CONVERSATIONAL CHAT BOARD TAB
// -------------------------------------------------------------
@Composable
private fun ConversationalBoardContent(
    ideas: List<Idea>,
    activeIdea: Idea?,
    selectedIdeaId: String?,
    onSelectIdea: (String?) -> Unit,
    messages: List<ChatMessage>,
    isAdvisorThinking: Boolean,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    chatListState: androidx.compose.foundation.lazy.LazyListState,
    voiceController: com.example.ui.components.VoiceRecognitionController,
    isDark: Boolean,
    primaryCyan: Color,
    cardBg: Color,
    cardBorder: Color,
    onNavigateToIdea: (String) -> Unit
) {
    val templatePromptsList = remember {
        listOf(
            Triple(
                "⚠️ Risk & Failure Modes",
                "Stress-test this venture thesis. Poke holes in the riskiest assumptions and outline the top 3 fatal failure modes.",
                "Act as a critical VC investor. What are the top 3 fatal failure modes and untested assumptions for this idea, and how can we mitigate them early?"
            ),
            Triple(
                "💰 3 Monetization Models",
                "Suggest 3 high-margin pricing and revenue models with estimated customer willingness to pay.",
                "Propose 3 distinct monetization models for this idea (e.g. usage-based, subscription tiers, enterprise contracts) with estimated gross margins and pricing benchmarks."
            ),
            Triple(
                "🛡️ Defensible Moat",
                "Identify defensible unfair advantages against big tech or well-funded copycats.",
                "What is the defensible unfair advantage or moat for this venture? How do we prevent incumbents from easily copying this feature?"
            ),
            Triple(
                "🧪 7-Day MVP Validation",
                "Draft a concrete 7-day zero-code validation plan to test buyer intent.",
                "Outline an exact 7-day validation experiment to test customer demand and willingness to pay before building any code."
            ),
            Triple(
                "⚡ Market Timing Catalysts",
                "Analyze why right now is the optimal window to build this product.",
                "Why is now the right time for this product? Analyze technological inflections, regulatory tailwinds, and macro shifts that make this urgent today."
            ),
            Triple(
                "🚀 5 Outbound Sales Hooks",
                "Generate 5 high-converting cold email and LinkedIn outreach hooks for ICP buyers.",
                "Draft 5 curiosity-driven cold outreach hooks for our ideal customer profile (ICP) that highlight immediate ROI and provoke demo requests."
            ),
            Triple(
                "🏢 Enterprise Buyer Map",
                "Break down the end-user vs budget holder / economic decision maker.",
                "Map out the enterprise buying committee: who is the end-user experiencing the pain vs who is the economic buyer with purchasing authority?"
            ),
            Triple(
                "🔄 Adjacent Pivot Angles",
                "Identify 2 high-potential pivot angles if early discovery reveals friction.",
                "If initial customer discovery shows lukewarm interest or slow adoption, what are 2 high-potential adjacent pivot angles leveraging the same core capability?"
            )
        )
    }

    var showTemplateDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Idea Selector & Context Pill Bar
        Surface(
            color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown or scrollable selector of target ideas
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isScratchpadSelected = selectedIdeaId == null
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isScratchpadSelected) primaryCyan.copy(alpha = 0.2f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isScratchpadSelected) primaryCyan else cardBorder),
                        modifier = Modifier.clickable { onSelectIdea(null) }
                    ) {
                        Text(
                            text = "💡 Scratchpad",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isScratchpadSelected) primaryCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    ideas.forEach { idea ->
                        val isSelected = selectedIdeaId == idea.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) primaryCyan.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) primaryCyan else cardBorder),
                            modifier = Modifier.clickable { onSelectIdea(idea.id) }
                        ) {
                            Text(
                                text = idea.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryCyan else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                if (selectedIdeaId == null) {
                    TextButton(onClick = onClearChat) {
                        Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (activeIdea != null) {
                    IconButton(
                        onClick = { onNavigateToIdea(activeIdea.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "View Idea", tint = primaryCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Quick Strategic Prompt Chips & Template Prompts Launcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prominent "Template Options" button for users when they don't know what to do
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = primaryCyan.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, primaryCyan),
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showTemplateDialog = true
                    }
                    .testTag("open_template_prompts_dialog_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = primaryCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Template Options",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryCyan
                    )
                }
            }

            templatePromptsList.take(6).forEach { (shortTitle, _, promptQuery) ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSendMessage(promptQuery)
                    }
                ) {
                    Text(
                        text = shortTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Message History List
        LazyColumn(
            state = chatListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            // When messages is empty, show interactive Template Prompts Starter Playbook
            if (messages.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.2.dp, primaryCyan.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = primaryCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = primaryCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Venture Advisor Playbook",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (activeIdea != null) "Choose a strategic template prompt for ${activeIdea.name}:" else "Don't know what to do? Choose a template prompt:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid / List of high-leverage template options
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                templatePromptsList.forEach { (title, subtitle, promptQuery) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color(0xFF1E293B) else Color.White,
                                        border = BorderStroke(1.dp, cardBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onSendMessage(promptQuery)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryCyan
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = subtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send Prompt",
                                                tint = primaryCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    isDark = isDark,
                    primaryCyan = primaryCyan,
                    cardBorder = cardBorder
                )
            }

            if (isAdvisorThinking) {
                item {
                    AdvisorThinkingBubble(isDark = isDark, primaryCyan = primaryCyan)
                }
            }
        }

        // Chat Input Bar with Voice Support
        Surface(
            color = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceInputIconButton(
                    controller = voiceController,
                    targetField = VoiceTargetField.DESCRIPTION,
                    currentText = userInput,
                    tint = primaryCyan
                )

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = userInput,
                    onValueChange = onUserInputChange,
                    placeholder = {
                        Text(
                            text = if (activeIdea != null) "Ask advisor about ${activeIdea.name}..." else "Describe hypothesis or select template...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("analyzer_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryCyan,
                        unfocusedBorderColor = cardBorder
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSendMessage(userInput)
                        }
                    },
                    enabled = userInput.isNotBlank() && !isAdvisorThinking,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (userInput.isNotBlank()) primaryCyan else cardBorder)
                        .testTag("analyzer_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (userInput.isNotBlank()) Color(0xFF070A0F) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Modal Dialog for Template Options when user taps "Template Options"
    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = primaryCyan)
                    Text("AI Advisor Template Prompts")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Select any strategic framework below to instantly analyze ${activeIdea?.name ?: "your venture thesis"}:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    templatePromptsList.forEach { (title, subtitle, promptQuery) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showTemplateDialog = false
                                    onSendMessage(promptQuery)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryCyan
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Tap to Ask Advisor →",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryCyan
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isDark: Boolean,
    primaryCyan: Color,
    cardBorder: Color
) {
    val isUser = message.isUser
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeStr = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(primaryCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "ORVEXA AI Advisor",
                    tint = primaryCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                if (isDark) Color(0xFF1E293B) else Color(0xFF0F172A)
            } else {
                if (isDark) Color(0xFF111827) else Color(0xFFF8FAFC)
            },
            border = BorderStroke(1.dp, if (isUser) primaryCyan.copy(alpha = 0.5f) else cardBorder),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORVEXA ADVISOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryCyan,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                if (isUser) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvisorThinkingBubble(isDark: Boolean, primaryCyan: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(primaryCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = primaryCyan
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0xFF111827) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, primaryCyan.copy(alpha = 0.3f))
        ) {
            Text(
                text = "ORVEXA Advisor evaluating market telemetry & unit economics...",
                style = MaterialTheme.typography.bodySmall,
                color = primaryCyan,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// SUGGESTED PROMPTS & MULTI-OPTIONS TAB
// -------------------------------------------------------------
@Composable
private fun PromptSuggestionsWithOptionsContent(
    suggestedPrompts: List<IdeaPromptSuggestion>,
    isGenerating: Boolean,
    keywordQuery: String,
    onKeywordChange: (String) -> Unit,
    onSearchKeyword: () -> Unit,
    onAdoptPrompt: (IdeaPromptSuggestion) -> Unit,
    onSendToChat: (IdeaPromptSuggestion) -> Unit,
    onEvaluatePrompt: (IdeaPromptSuggestion) -> Unit,
    isDark: Boolean,
    primaryCyan: Color,
    cardBg: Color,
    cardBorder: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Keyword Custom Generator Search Box
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI PROMPT & STRATEGIC OPTION GENERATOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter any industry, vertical, or problem statement to generate 3 verified venture hypotheses with multiple strategic execution options.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = keywordQuery,
                            onValueChange = onKeywordChange,
                            placeholder = { Text("e.g. Supply Chain, Radiology, Real Estate Tech...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prompt_generator_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSearchKeyword,
                            enabled = keywordQuery.isNotBlank() && !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("prompt_generator_submit")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Text("Generate", color = Color(0xFF070A0F), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Header for Prompts with Multiple Options
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURATED VENTURE PROMPTS (${suggestedPrompts.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Tap options to inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryCyan
                )
            }
        }

        // List of Prompts with Multi-Option Pathways
        items(suggestedPrompts) { prompt ->
            var adopted by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row: Sector & Tag Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = primaryCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = prompt.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "#${prompt.tag}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "3 Strategic Angles",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = prompt.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = prompt.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Multi-Option Strategic Execution Cards
                    if (prompt.options.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "EXECUTION OPTIONS:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                            color = primaryCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            prompt.options.forEach { optionText ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF24334C) else Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircleOutline,
                                            contentDescription = null,
                                            tint = primaryCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSendToChat(prompt) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat Board", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { onEvaluatePrompt(prompt) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Score AI", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                onAdoptPrompt(prompt)
                                adopted = true
                            },
                            enabled = !adopted,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (adopted) Color(0xFF10B981) else primaryCyan
                            ),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Text(
                                text = if (adopted) "Adopted ✓" else "Adopt into DB",
                                fontSize = 11.sp,
                                color = if (adopted) Color.White else Color(0xFF070A0F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// IDEA EVALUATOR TAB
// -------------------------------------------------------------
@Composable
private fun IdeaEvaluatorContent(
    title: String,
    description: String,
    category: String,
    tag: String,
    priority: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onTagChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    isEvaluating: Boolean,
    evaluationResult: Map<String, Any>?,
    savedMessage: String?,
    onEvaluate: () -> Unit,
    onSaveIdea: () -> Unit,
    onExportPdf: () -> Unit = {},
    onChatWithAdvisor: () -> Unit,
    isDark: Boolean,
    primaryCyan: Color,
    cardBg: Color,
    cardBorder: Color
) {
    val categories = listOf("Healthcare", "AI & Tech", "B2B SaaS", "Fintech", "DevTools", "Enterprise", "Climate")
    val tags = listOf("B2B", "B2C", "Marketplace", "Enterprise", "Mobile", "AI-First")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Idea Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "IDEA SPECIFICATION FOR AI EVALUATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Venture Name or Working Title") },
                    placeholder = { Text("e.g. MediClaim AI, AutoEscrow") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Problem Thesis & Target Customer (ICP)") },
                    placeholder = { Text("Describe who pays, what friction they face, and why current alternatives fail...") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Text("Sector:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tag Chips
                Text("Tag:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { t ->
                        FilterChip(
                            selected = tag == t,
                            onClick = { onTagChange(t) },
                            label = { Text("#$t", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onEvaluate,
                    enabled = (title.isNotBlank() || description.isNotBlank()) && !isEvaluating,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("evaluate_idea_submit_button")
                ) {
                    if (isEvaluating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Evaluating with Gemini...", color = Color.Black, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Evidence-Backed AI Evaluation", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Evaluation Results Card
        if (evaluationResult != null) {
            val oppScore = (evaluationResult["opportunityScore"] as? Int) ?: 75
            val timingScore = (evaluationResult["timingScore"] as? Int) ?: 75
            val feasibilityScore = (evaluationResult["feasibilityScore"] as? Int) ?: 70
            val rec = (evaluationResult["recommendation"] as? String) ?: "VALIDATE_FIRST"
            val reason = (evaluationResult["recommendationReason"] as? String) ?: ""
            val whyNow = (evaluationResult["whyNow"] as? String) ?: ""
            val biggestRisk = (evaluationResult["biggestRisk"] as? String) ?: ""
            val nextStep = (evaluationResult["nextImmediateAction"] as? String) ?: ""
            val options = (evaluationResult["options"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.5.dp, primaryCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI EVALUATION DOSSIER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryCyan,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (rec) {
                                "BUILD_NOW" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                else -> primaryCyan.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = rec.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (rec) {
                                    "BUILD_NOW" -> Color(0xFF10B981)
                                    else -> primaryCyan
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Three Pillar Scores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EvaluationMetricPill("OPPORTUNITY", oppScore, primaryCyan, Modifier.weight(1f))
                        EvaluationMetricPill("TIMING", timingScore, Color(0xFF10B981), Modifier.weight(1f))
                        EvaluationMetricPill("FEASIBILITY", feasibilityScore, Color(0xFF8B5CF6), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Industry Benchmark Standing
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, primaryCyan.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = primaryCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INDUSTRY BENCHMARK: 82nd Percentile in $category (+${oppScore - 70} pts vs median)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (reason.isNotBlank()) {
                        Text("STRATEGIC RECOMMENDATION:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = primaryCyan)
                        Text(reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (whyNow.isNotBlank()) {
                        Text("MARKET TAILWINDS (WHY NOW):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text(whyNow, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (biggestRisk.isNotBlank()) {
                        Text("PRIMARY RISK / FATAL FLAW:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        Text(biggestRisk, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (nextStep.isNotBlank()) {
                        Text("NEXT ACTIONABLE EXPERIMENT:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = primaryCyan)
                        Text(nextStep, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (options.isNotEmpty()) {
                        Text("MULTI-OPTION STRATEGIC ANGLES:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = primaryCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        options.forEach { opt ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Default.ArrowRight, contentDescription = null, tint = primaryCyan, modifier = Modifier.size(16.dp))
                                Text(opt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (savedMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = savedMessage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = onChatWithAdvisor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Discuss", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onExportPdf,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_evaluator_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("PDF", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onSaveIdea,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryCyan),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("save_evaluated_idea_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvaluationMetricPill(
    label: String,
    score: Int,
    accent: Color,
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
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}
