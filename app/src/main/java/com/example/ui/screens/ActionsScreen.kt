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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionItem
import com.example.data.model.Idea
import com.example.ui.IdeaViewModel
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionsScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val allIdeas by viewModel.allIdeas.collectAsState()

    var statusFilter by remember { mutableStateOf("ACTIVE") }
    var priorityFilter by remember { mutableStateOf("ALL") }
    var selectedIdeaFilter by remember { mutableStateOf("ALL") }
    var showAddActionDialog by remember { mutableStateOf(false) }

    data class ActionEntry(val action: ActionItem, val ideaId: String, val ideaName: String)

    val allActions = remember(allIdeas) {
        allIdeas.flatMap { idea ->
            idea.actionItems.map { act -> ActionEntry(act, idea.id, idea.name) }
        }
    }

    val filteredActions = remember(allActions, statusFilter, priorityFilter, selectedIdeaFilter) {
        allActions.filter { entry ->
            val matchStatus = when (statusFilter) {
                "ACTIVE" -> !entry.action.completed
                "COMPLETED" -> entry.action.completed
                else -> true
            }
            val matchPriority = if (priorityFilter == "ALL") true else entry.action.priority.equals(priorityFilter, ignoreCase = true)
            val matchIdea = if (selectedIdeaFilter == "ALL") true else entry.ideaId == selectedIdeaFilter
            matchStatus && matchPriority && matchIdea
        }
    }

    val totalCount = allActions.size
    val completedCount = allActions.count { it.action.completed }
    val p0CriticalCount = allActions.count { it.action.priority == "P0" && !it.action.completed }
    val completionRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("actions_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddActionDialog = true },
                containerColor = ModuleActionsColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 76.dp)
                    .testTag("add_action_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Action")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Action", fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.testTag("actions_drawer_button")
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
                                    text = "Actions",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ModuleActionsColor)
                                )
                            }
                            Text(
                                text = "High-leverage execution roadmap & milestones",
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

            // Execution Velocity Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "EXECUTION VELOCITY",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ModuleActionsColor,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$completedCount of $totalCount completed (${(completionRatio * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (p0CriticalCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "$p0CriticalCount P0 Critical",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { completionRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ModuleActionsColor,
                            trackColor = if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)
                        )
                    }
                }
            }

            // Status Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ACTIVE" to "Active Actions", "ALL" to "All", "COMPLETED" to "Done").forEach { (key, label) ->
                        val isSelected = statusFilter == key
                        Surface(
                            onClick = { statusFilter = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ModuleActionsColor.copy(alpha = 0.2f) else (if (isDark) OrvexaDarkSurface else Color(0xFFF3F4F6)),
                            border = BorderStroke(1.dp, if (isSelected) ModuleActionsColor else Color.Transparent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ModuleActionsColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Priority Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("ALL", "P0", "P1", "P2").forEach { p ->
                        item {
                            FilterChip(
                                selected = priorityFilter == p,
                                onClick = { priorityFilter = p },
                                label = { Text(if (p == "ALL") "All Priorities" else "Priority $p") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ModuleActionsColor.copy(alpha = 0.2f),
                                    selectedLabelColor = ModuleActionsColor
                                )
                            )
                        }
                    }
                }
            }

            // Action Items List
            if (filteredActions.isEmpty()) {
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
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = ModuleActionsColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No actions match your filters.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create a high-leverage action item with 'New Action'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredActions, key = { it.action.id + it.ideaId }) { entry ->
                    val action = entry.action
                    val isCompleted = action.completed
                    val priorityColor = when (action.priority) {
                        "P0" -> Color(0xFFEF4444)
                        "P1" -> ModuleActionsColor
                        else -> Color(0xFF3B82F6)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isCompleted) (if (isDark) Color(0xFF262035) else Color(0xFFE5E7EB))
                            else (if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = {
                                    viewModel.toggleActionItem(entry.ideaId, action.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF10B981),
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = action.task,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = priorityColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = action.priority,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = priorityColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "Due: ${action.timeline}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = "• ${entry.ideaName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ModuleActionsColor
                                    )
                                }
                            }

                            IconButton(onClick = { onNavigateToIdea(entry.ideaId) }) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View Idea",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Action Dialog
    if (showAddActionDialog) {
        var inputTask by remember { mutableStateOf("") }
        var inputPriority by remember { mutableStateOf("P1") }
        var inputTimeline by remember { mutableStateOf("7 Days") }
        var selectedIdeaForAction by remember { mutableStateOf(allIdeas.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddActionDialog = false },
            title = { Text("Add High-Leverage Action") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target Venture:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allIdeas) { idea ->
                            FilterChip(
                                selected = selectedIdeaForAction == idea.id,
                                onClick = { selectedIdeaForAction = idea.id },
                                label = { Text(idea.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputTask,
                        onValueChange = { inputTask = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("P0", "P1", "P2").forEach { p ->
                            FilterChip(
                                selected = inputPriority == p,
                                onClick = { inputPriority = p },
                                label = { Text("Priority $p") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputTimeline,
                        onValueChange = { inputTimeline = it },
                        label = { Text("Timeline (e.g. 7 Days, 30 Days)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTask.isNotBlank() && selectedIdeaForAction.isNotBlank()) {
                            viewModel.addActionItem(
                                ideaId = selectedIdeaForAction,
                                task = inputTask,
                                priority = inputPriority,
                                timeline = inputTimeline.ifBlank { "7 Days" }
                            )
                            showAddActionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ModuleActionsColor)
                ) {
                    Text("Add Action", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
