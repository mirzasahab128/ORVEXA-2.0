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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.IdeaViewModel
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.*

/**
 * NewIdeaScreen
 *
 * Dedicated, institutional-grade Compose UI for capturing and storing new business ideas
 * and strategic hypotheses with category, tags, and priority levels directly into Room SQLite.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewIdeaScreen(
    viewModel: IdeaViewModel,
    onBack: () -> Unit,
    onIdeaSaved: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var strategicHypothesis by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("AI & Tech") }
    var selectedTag by remember { mutableStateOf("B2B") }
    var selectedPriority by remember { mutableStateOf("High") }
    var customTagInput by remember { mutableStateOf("") }
    val customTags = remember { mutableStateListOf<String>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successSavedMessage by remember { mutableStateOf(false) }

    val categories = listOf("AI & Tech", "Healthcare", "B2B SaaS", "Fintech", "DevTools", "Climate", "Enterprise", "Consumer")
    val tags = listOf("B2B", "B2C", "Marketplace", "API-First", "Mobile", "Infrastructure", "AI-First")
    val priorities = listOf("High", "Medium", "Low")

    val primaryCyan = if (isDark) OrvexaIntelligentCyan else Color(0xFF0284C7)
    val orvexaOrange = OrvexaOrange
    val cardBg = if (isDark) OrvexaDarkSurface else Color.White
    val cardBorder = if (isDark) Color(0x2EFFFFFF) else Color(0x33000000)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("new_idea_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Capture New Idea",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Strategic hypothesis & Room database persistence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        },
                        modifier = Modifier.testTag("new_idea_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    ThemeSwitcherButton(
                        currentTheme = themeMode,
                        onToggleTheme = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleTheme()
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Hero Intro Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0xFF1A1624) else Color(0xFFF8FAFC),
                border = BorderStroke(1.2.dp, orvexaOrange.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(orvexaOrange, OrvexaElectricViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STRATEGIC HYPOTHESIS INGESTION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = orvexaOrange,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "OFFLINE SQLITE",
                                    color = Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Frame your business opportunity around an explicit, falsifiable strategic hypothesis with categorized tags and priority tier.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 1: Core Idea Name & Priority
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. VENTURE TITLE & PRIORITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = null
                        },
                        label = { Text("Venture Name or Working Title *") },
                        placeholder = { Text("e.g., MediClaim AI, SiteForge Trades") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_idea_title_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Priority Level",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        priorities.forEach { p ->
                            val isSelected = selectedPriority == p
                            val pColor = when (p) {
                                "High" -> Color(0xFFEF4444)
                                "Medium" -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedPriority = p
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) pColor.copy(alpha = 0.20f) else Color(0x10FFFFFF),
                                border = BorderStroke(1.2.dp, if (isSelected) pColor else Color(0x2EFFFFFF)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("priority_chip_$p")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = pColor,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = p,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 2: Strategic Hypothesis (Key requirement!)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. STRATEGIC HYPOTHESIS *",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = orvexaOrange,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Core Falsifiable Premise",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "What is the single foundational assumption that must be true for this venture to succeed?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = strategicHypothesis,
                        onValueChange = {
                            strategicHypothesis = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                "e.g., Specialized orthopedic clinics will pay \$500/month if algorithmic appeals recover >10x their cost with <5 min manual review.",
                                fontSize = 12.5.sp
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_idea_hypothesis_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "3. ONE-LINE THESIS / PROBLEM STATEMENT *",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                "Concise summary of the market pain point and how your solution solves it.",
                                fontSize = 12.5.sp
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_idea_description_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 3: Industry Sector & Business Model Tags
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. INDUSTRY CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedCategory = cat
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) primaryCyan.copy(alpha = 0.22f) else Color(0x10FFFFFF),
                                border = BorderStroke(1.dp, if (isSelected) primaryCyan else Color(0x2EFFFFFF)),
                                modifier = Modifier.testTag("category_chip_$cat")
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryCyan else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "5. BUSINESS MODEL / DISTRIBUTION TAG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { t ->
                            val isSelected = selectedTag == t
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedTag = t
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) orvexaOrange.copy(alpha = 0.22f) else Color(0x10FFFFFF),
                                border = BorderStroke(1.dp, if (isSelected) orvexaOrange else Color(0x2EFFFFFF)),
                                modifier = Modifier.testTag("tag_chip_$t")
                            ) {
                                Text(
                                    text = "#$t",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) orvexaOrange else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Custom Tags (Optional)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customTagInput,
                            onValueChange = { customTagInput = it },
                            placeholder = { Text("e.g. HIPAA, SOC2, Edge-AI", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val cleaned = customTagInput.trim().replace("#", "")
                                    if (cleaned.isNotBlank() && !customTags.contains(cleaned)) {
                                        customTags.add(cleaned)
                                        customTagInput = ""
                                    }
                                    focusManager.clearFocus()
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_tag_text_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val cleaned = customTagInput.trim().replace("#", "")
                                if (cleaned.isNotBlank() && !customTags.contains(cleaned)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    customTags.add(cleaned)
                                    customTagInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(orvexaOrange)
                                .testTag("add_custom_tag_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Tag", tint = Color.White)
                        }
                    }

                    if (customTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            customTags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = orvexaOrange.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, orvexaOrange.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = orvexaOrange
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Tag",
                                            tint = orvexaOrange,
                                            modifier = Modifier
                                                .size(13.dp)
                                                .clickable { customTags.remove(tag) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Error / Success Feedback banner
            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            if (successSavedMessage) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Venture thesis committed to Room Database & queued for Gemini analysis!",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Commit Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter an idea title."
                    } else if (description.isBlank() && strategicHypothesis.isBlank()) {
                        errorMessage = "Please provide a strategic hypothesis or thesis description."
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val finalDesc = description.ifBlank { strategicHypothesis }
                        val customList = customTags.toList()

                        viewModel.saveIdea(
                            title = title.trim(),
                            description = finalDesc.trim(),
                            category = selectedCategory,
                            tag = selectedTag,
                            priority = selectedPriority,
                            customTags = customList,
                            strategicHypothesis = strategicHypothesis.trim()
                        )
                        successSavedMessage = true
                        onIdeaSaved(title)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = orvexaOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(12.dp, RoundedCornerShape(14.dp))
                    .testTag("commit_idea_to_room_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Commit to Room Database",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
