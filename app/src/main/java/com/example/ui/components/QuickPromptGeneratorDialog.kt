package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

data class QuickPromptPreset(
    val title: String,
    val category: String,
    val promptText: String,
    val accentColor: Color
)

@Composable
fun QuickPromptGeneratorDialog(
    onDismiss: () -> Unit,
    onSelectPrompt: (String) -> Unit,
    onCustomGenerate: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    var customKeyword by remember { mutableStateOf("") }

    val presets = remember {
        listOf(
            QuickPromptPreset(
                title = "Stress-Test Unit Economics",
                category = "Finance & CAC",
                promptText = "Calculate the target CAC-to-LTV ratio, break-even unit volume, and identify our top three margin risks.",
                accentColor = OrvexaOrange
            ),
            QuickPromptPreset(
                title = "Uncover Hidden Competitors",
                category = "Market Intel",
                promptText = "Identify indirect incumbents and stealth startups solving this customer pain. Highlight their primary vulnerability.",
                accentColor = OrvexaIntelligentCyan
            ),
            QuickPromptPreset(
                title = "Identify Buyer Objections",
                category = "Validation",
                promptText = "Simulate the top 4 objections enterprise decision-makers will raise during procurement and provide defensive rebuttals.",
                accentColor = OrvexaElectricViolet
            ),
            QuickPromptPreset(
                title = "Formulate Go-To-Market Wedge",
                category = "Growth",
                promptText = "Propose a narrow, high-urgency wedge persona to achieve our first 100 paying customers without paid ads.",
                accentColor = ModuleExperimentsColor
            ),
            QuickPromptPreset(
                title = "Pre-Mortem Failure Analysis",
                category = "Risk Audit",
                promptText = "Fast forward 18 months: this company failed. Conduct a post-mortem explaining the #1 root failure cause.",
                accentColor = ModuleRemindersColor
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .wrapContentHeight()
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp))
                .testTag("quick_prompt_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) OrvexaDarkSurface else Color.White,
            border = BorderStroke(1.2.dp, if (isDark) Color(0x33FFFFFF) else Color(0x22000000))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(OrvexaIntelligentCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = OrvexaIntelligentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Prompt Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else OrvexaDarkBackground
                            )
                            Text(
                                text = "Select or generate strategic venture prompts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Keyword Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customKeyword,
                        onValueChange = { customKeyword = it },
                        placeholder = { Text("e.g. B2B SaaS, HealthTech pricing, churn", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_prompt_keyword_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customKeyword.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCustomGenerate(customKeyword.trim())
                                onDismiss()
                            }
                        },
                        enabled = customKeyword.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrvexaIntelligentCyan)
                    ) {
                        Text("Generate", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "STRATEGIC PROMPT PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrvexaOrange,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets) { preset ->
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectPrompt(preset.promptText)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = preset.accentColor.copy(alpha = if (isDark) 0.12f else 0.08f),
                            border = BorderStroke(1.dp, preset.accentColor.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prompt_preset_${preset.title.take(10).lowercase().replace(" ", "_")}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = preset.accentColor.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = preset.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = preset.accentColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.promptText,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
