package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IdeaTemplateLibrary
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddIdeaBottomSheet(
    onDismiss: () -> Unit,
    onSaveIdea: (title: String, description: String, category: String, tag: String, priority: String, customTags: List<String>) -> Unit
) {
    val themeMode = LocalThemeMode.current
    val isDark = themeMode == ThemeMode.DARK
    val haptic = LocalHapticFeedback.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("AI & Tech") }
    var selectedTag by remember { mutableStateOf("B2B") }
    var selectedPriority by remember { mutableStateOf("High") }
    var selectedTemplateId by remember { mutableStateOf("freeform") }
    var customTagInput by remember { mutableStateOf("") }
    val customTags = remember { mutableStateListOf<String>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Healthcare", "AI & Tech", "B2B SaaS", "Fintech", "DevTools", "Enterprise")
    val tags = listOf("B2B", "B2C", "Marketplace", "Infra", "Mobile", "AI-First", "DevTools")
    val priorities = listOf("High", "Medium", "Low")

    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F172A)
    val recordingRed = Color(0xFFEF4444)

    // Voice-to-Text Recognition integration
    val voiceController = rememberVoiceRecognition { field, text, _ ->
        when (field) {
            VoiceTargetField.TITLE -> {
                title = text
                if (errorMessage != null) errorMessage = null
            }
            VoiceTargetField.DESCRIPTION -> {
                description = text
                if (errorMessage != null) errorMessage = null
            }
            VoiceTargetField.NONE -> {}
        }
    }

    val isListening = voiceController.state.isListening
    val activeVoiceField = voiceController.state.activeField

    // Infinite breathing animation for active microphone
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isDark) Color(0xFF0E1420) else Color(0xFFFFFFFF),
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEW OPPORTUNITY THESIS",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Capture Idea into Database",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        voiceController.onStopListening()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("close_bottom_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hands-Free Voice Capture Banner
            Surface(
                onClick = {
                    if (isListening) {
                        voiceController.onStopListening()
                    } else {
                        voiceController.onStartListening(if (title.isBlank()) VoiceTargetField.TITLE else VoiceTargetField.DESCRIPTION)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = if (isListening) recordingRed.copy(alpha = 0.15f) else (if (isDark) Color(0xFF162032) else Color(0xFFEEF2FF)),
                border = BorderStroke(1.dp, if (isListening) recordingRed else primaryAccent.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("hands_free_voice_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isListening) recordingRed.copy(alpha = 0.2f) else primaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Hands-Free Voice Capture",
                            tint = if (isListening) recordingRed else primaryAccent,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(if (isListening) micScale else 1f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isListening) "Listening... Tap to Finish Dictation" else "Hands-Free Voice Capture",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isListening) recordingRed else primaryAccent
                        )
                        Text(
                            text = if (isListening) "Transcribing your thoughts in real-time..." else "Dictate ideas hands-free using device microphone while on the go",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Idea Structure Template Library Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Idea Structure Template",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${IdeaTemplateLibrary.templates.size} Templates",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IdeaTemplateLibrary.templates.forEach { template ->
                        val isSelected = selectedTemplateId == template.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) primaryAccent.copy(alpha = 0.2f) else if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) primaryAccent else MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable {
                                selectedTemplateId = template.id
                                selectedCategory = template.defaultCategory
                                selectedTag = template.defaultTag
                                selectedPriority = template.defaultPriority
                                if (template.templatePrompt.isNotBlank()) {
                                    description = template.templatePrompt
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = template.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) primaryAccent else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            voiceController.state.statusMessage?.let { status ->
                Surface(
                    color = if (isListening) {
                        if (isDark) Color(0xFF162032) else Color(0xFFEEF2FF)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isListening) primaryAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("voice_status_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(recordingRed)
                                    .scale(micScale)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isListening) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isListening) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isListening) {
                            TextButton(
                                onClick = { voiceController.onStopListening() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Done", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Error banner if validation fails
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Title Field Header with Voice Dictation Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Idea / Product Name *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                // Quick Title Voice Dictate Icon
                IconButton(
                    onClick = { voiceController.onStartListening(VoiceTargetField.TITLE, title) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("voice_dictate_title_button")
                ) {
                    val isListeningTitle = isListening && activeVoiceField == VoiceTargetField.TITLE
                    Icon(
                        imageVector = if (isListeningTitle) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Dictate Title",
                        tint = if (isListeningTitle) recordingRed else primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = { Text("e.g. ApexContract AI, Nexus PriorAuth") },
                singleLine = true,
                trailingIcon = {
                    val isListeningTitle = isListening && activeVoiceField == VoiceTargetField.TITLE
                    if (isListeningTitle) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(recordingRed)
                                .scale(micScale)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("idea_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Thesis / Problem Description Header & Voice Dictate Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "One-Line Thesis / Problem Solved *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                // Quick Dictate Badge
                Surface(
                    onClick = { voiceController.onStartListening(VoiceTargetField.DESCRIPTION, description) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isListening && activeVoiceField == VoiceTargetField.DESCRIPTION) {
                        recordingRed.copy(alpha = 0.15f)
                    } else if (isDark) {
                        Color(0xFF1E293B)
                    } else {
                        Color(0xFFEEF2FF)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isListening && activeVoiceField == VoiceTargetField.DESCRIPTION) {
                            recordingRed
                        } else {
                            primaryAccent.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier.testTag("voice_dictate_thesis_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isListeningDesc = isListening && activeVoiceField == VoiceTargetField.DESCRIPTION
                        Icon(
                            imageVector = if (isListeningDesc) Icons.Default.Mic else Icons.Default.GraphicEq,
                            contentDescription = "Dictate Thesis",
                            tint = if (isListeningDesc) recordingRed else primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isListeningDesc) "Listening..." else "Dictate Voice",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isListeningDesc) recordingRed else primaryAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    if (errorMessage != null) errorMessage = null
                },
                placeholder = {
                    Text("Dictate or type what pain point this solves and why the timing is right...")
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("idea_description_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isListening && activeVoiceField == VoiceTargetField.DESCRIPTION) recordingRed else primaryAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Prominent Voice Dictation Action Bar
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF111827) else Color(0xFFF8FAFC),
                border = BorderStroke(
                    1.dp,
                    if (isListening) primaryAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { voiceController.onStartListening(VoiceTargetField.DESCRIPTION) }
                    .testTag("voice_dictation_action_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isListening) recordingRed.copy(alpha = 0.2f) else primaryAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (isListening) recordingRed else primaryAccent,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .scale(if (isListening) micScale else 1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isListening) "Voice Input Active" else "Fast Voice-to-Text Dictation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isListening) "Speak your thoughts... Tap to stop" else "Dictate raw thoughts for rapid capture",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { voiceController.onStartListening(VoiceTargetField.DESCRIPTION) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isListening) recordingRed else primaryAccent,
                            contentColor = if (isListening) Color.White else if (isDark) Color(0xFF070A0F) else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (isListening) "Stop" else "Speak",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = "Market Sector",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = cat
                        },
                        label = { Text(cat, style = MaterialTheme.typography.bodySmall) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFF0F172A),
                            selectedLabelColor = if (isDark) primaryAccent else Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) primaryAccent else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tag & Priority in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Business Model Tag
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Model Tag",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.take(3).forEach { tag ->
                            val isSelected = selectedTag == tag
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) primaryAccent else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTag = tag
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF070A0F) else Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Priority
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        priorities.forEach { p ->
                            val isSelected = selectedPriority == p
                            val pColor = when (p) {
                                "High" -> Color(0xFFEF4444)
                                "Medium" -> Color(0xFFF59E0B)
                                else -> Color(0xFF64748B)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) pColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedPriority = p
                                }
                            ) {
                                Text(
                                    text = p,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Labels / Tags Input Section
            Text(
                text = "Custom Tags & Labels (Optional)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it },
                    placeholder = { Text("e.g. LLM-Ops, Bootstrapped, HIPAA") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_tag_input_field"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Button(
                    onClick = {
                        val trimmed = customTagInput.trim().removePrefix("#")
                        if (trimmed.isNotBlank() && !customTags.contains(trimmed)) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            customTags.add(trimmed)
                            customTagInput = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = primaryAccent
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("add_custom_tag_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tag", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tag", fontWeight = FontWeight.Bold)
                }
            }

            if (customTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    customTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove tag $tag",
                                    tint = primaryAccent,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            customTags.remove(tag)
                                        }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Submit Button
            Button(
                onClick = {
                    voiceController.onStopListening()
                    if (title.isBlank()) {
                        errorMessage = "Please enter an idea title."
                    } else if (description.isBlank()) {
                        errorMessage = "Please provide a brief thesis or description."
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSaveIdea(
                            title.trim(),
                            description.trim(),
                            selectedCategory,
                            selectedTag,
                            selectedPriority,
                            customTags.toList()
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_idea_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryAccent,
                    contentColor = if (isDark) Color(0xFF070A0F) else Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Commit to Room Database",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
