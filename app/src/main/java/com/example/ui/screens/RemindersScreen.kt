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
import com.example.data.model.SmartReminder
import com.example.ui.IdeaViewModel
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val allIdeas by viewModel.allIdeas.collectAsState()

    var typeFilter by remember { mutableStateOf("ALL") }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    data class ReminderEntry(val reminder: SmartReminder, val ideaId: String, val ideaName: String)

    val allReminders = remember(allIdeas) {
        allIdeas.flatMap { idea ->
            idea.reminders.map { rem -> ReminderEntry(rem, idea.id, idea.name) }
        }
    }

    val filteredReminders = remember(allReminders, typeFilter) {
        if (typeFilter == "ALL") allReminders
        else allReminders.filter { it.reminder.type.equals(typeFilter, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("reminders_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddReminderDialog = true },
                containerColor = ModuleRemindersColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 76.dp)
                    .testTag("add_reminder_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddAlarm, contentDescription = "Add Reminder")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Reminder", fontWeight = FontWeight.Bold)
                }
            }
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
                            modifier = Modifier.testTag("reminders_drawer_button")
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
                                    text = "Reminders",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ModuleRemindersColor)
                                )
                            }
                            Text(
                                text = "Time-critical triggers & regulatory watch-points",
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Time Critical Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ModuleRemindersColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ModuleRemindersColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "${allReminders.size} Scheduled Venture Triggers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Monitors regulatory filings, quarterly review dates, and competitor milestones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Type Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val types = listOf("ALL", "Regulation", "Market", "Competitor")
                    items(types) { t ->
                        val isSelected = typeFilter == t
                        FilterChip(
                            selected = isSelected,
                            onClick = { typeFilter = t },
                            label = { Text(if (t == "ALL") "All Types" else t) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ModuleRemindersColor.copy(alpha = 0.2f),
                                selectedLabelColor = ModuleRemindersColor
                            )
                        )
                    }
                }
            }

            // Reminders List
            if (filteredReminders.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF9FAFB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlarmOn,
                                contentDescription = null,
                                tint = ModuleRemindersColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No reminders scheduled in this view.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Use 'New Reminder' to schedule a market review alert.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredReminders, key = { it.reminder.id + it.ideaId }) { entry ->
                    val rem = entry.reminder
                    val isTriggered = rem.isTriggered

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
                                    color = ModuleRemindersColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = rem.type.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ModuleRemindersColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    onClick = { viewModel.toggleReminder(entry.ideaId, rem.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isTriggered) Color(0xFF10B981).copy(alpha = 0.15f) else (if (isDark) OrvexaDarkSurfaceVariant else Color(0xFFF3F4F6))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isTriggered) Icons.Default.Check else Icons.Default.Done,
                                            contentDescription = null,
                                            tint = if (isTriggered) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isTriggered) "Reviewed" else "Mark Reviewed",
                                            fontSize = 11.sp,
                                            color = if (isTriggered) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = rem.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Trigger condition: ${rem.triggerCondition}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = ModuleRemindersColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = rem.dueDate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ModuleRemindersColor
                                    )
                                }

                                Text(
                                    text = "Venture: ${entry.ideaName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        var inputTitle by remember { mutableStateOf("") }
        var inputType by remember { mutableStateOf("Market") }
        var inputCondition by remember { mutableStateOf("") }
        var inputDueDate by remember { mutableStateOf("In 14 days") }
        var selectedIdeaForReminder by remember { mutableStateOf(allIdeas.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text("Add Smart Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target Venture:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allIdeas) { idea ->
                            FilterChip(
                                selected = selectedIdeaForReminder == idea.id,
                                onClick = { selectedIdeaForReminder = idea.id },
                                label = { Text(idea.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Reminder Title / Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Market", "Regulation", "Competitor").forEach { t ->
                            FilterChip(
                                selected = inputType == t,
                                onClick = { inputType = t },
                                label = { Text(t) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputCondition,
                        onValueChange = { inputCondition = it },
                        label = { Text("Trigger Condition (e.g. Monthly audit)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputDueDate,
                        onValueChange = { inputDueDate = it },
                        label = { Text("Due Date / Horizon (e.g. Nov 15, 2026)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTitle.isNotBlank() && selectedIdeaForReminder.isNotBlank()) {
                            viewModel.addReminder(
                                ideaId = selectedIdeaForReminder,
                                title = inputTitle,
                                type = inputType,
                                triggerCondition = inputCondition.ifBlank { "Periodic trigger review" },
                                dueDate = inputDueDate.ifBlank { "In 14 days" }
                            )
                            showAddReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ModuleRemindersColor)
                ) {
                    Text("Add Reminder", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
