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
import com.example.data.model.Idea
import com.example.data.model.ValidationExperiment
import com.example.ui.IdeaViewModel
import com.example.ui.components.ThemeSwitcherButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentsScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == ThemeMode.DARK
    val allIdeas by viewModel.allIdeas.collectAsState()

    var statusFilter by remember { mutableStateOf("ALL") }
    var selectedIdeaFilter by remember { mutableStateOf("ALL") }
    var showAddExperimentDialog by remember { mutableStateOf(false) }

    // Aggregate all experiments across portfolio ideas
    data class ExperimentEntry(val experiment: ValidationExperiment, val ideaId: String, val ideaName: String)

    val allExperiments = remember(allIdeas) {
        allIdeas.flatMap { idea ->
            idea.experiments.map { exp -> ExperimentEntry(exp, idea.id, idea.name) }
        }
    }

    val filteredExperiments = remember(allExperiments, statusFilter, selectedIdeaFilter) {
        allExperiments.filter { entry ->
            val matchStatus = if (statusFilter == "ALL") true else entry.experiment.status.equals(statusFilter, ignoreCase = true)
            val matchIdea = if (selectedIdeaFilter == "ALL") true else entry.ideaId == selectedIdeaFilter
            matchStatus && matchIdea
        }
    }

    val totalCount = allExperiments.size
    val validatedCount = allExperiments.count { it.experiment.status == "VALIDATED" }
    val inProgressCount = allExperiments.count { it.experiment.status == "IN_PROGRESS" }
    val pendingCount = allExperiments.count { it.experiment.status == "PENDING" }
    val successRate = if (totalCount > 0) ((validatedCount.toFloat() / totalCount) * 100).toInt() else 0

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("experiments_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExperimentDialog = true },
                containerColor = ModuleExperimentsColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 76.dp)
                    .testTag("add_experiment_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Experiment")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Test", fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.testTag("experiments_drawer_button")
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
                                    text = "Experiments",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ModuleExperimentsColor)
                                )
                            }
                            Text(
                                text = "Hypothesis testing & venture validation pipeline",
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

            // Summary Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("VALIDATED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text("$validatedCount", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                            Text("$successRate% win rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("RUNNING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ModuleExperimentsColor)
                            Text("$inProgressCount", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = ModuleExperimentsColor)
                            Text("Live tests", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) OrvexaDarkSurface else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDark) OrvexaDarkBorder else Color(0xFFE5E7EB)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("PENDING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text("$pendingCount", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF59E0B))
                            Text("Queued tests", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Filter Chips by Status
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statuses = listOf("ALL", "IN_PROGRESS", "PENDING", "VALIDATED", "INVALIDATED")
                    items(statuses) { status ->
                        val isSelected = statusFilter == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { statusFilter = status },
                            label = { Text(status.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ModuleExperimentsColor.copy(alpha = 0.2f),
                                selectedLabelColor = ModuleExperimentsColor
                            )
                        )
                    }
                }
            }

            // Venture Filter Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedIdeaFilter == "ALL",
                            onClick = { selectedIdeaFilter = "ALL" },
                            label = { Text("All Ventures (${allIdeas.size})") }
                        )
                    }
                    items(allIdeas) { idea ->
                        FilterChip(
                            selected = selectedIdeaFilter == idea.id,
                            onClick = { selectedIdeaFilter = idea.id },
                            label = { Text(idea.name) }
                        )
                    }
                }
            }

            // Experiments List
            if (filteredExperiments.isEmpty()) {
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
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = ModuleExperimentsColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No experiments found for this filter.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap 'New Test' to formulate a hypothesis validation test.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredExperiments, key = { it.experiment.id + it.ideaId }) { entry ->
                    val exp = entry.experiment
                    val statusColor = when (exp.status) {
                        "VALIDATED" -> Color(0xFF10B981)
                        "IN_PROGRESS" -> ModuleExperimentsColor
                        "INVALIDATED" -> Color(0xFFEF4444)
                        else -> Color(0xFFF59E0B)
                    }

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
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ModuleExperimentsColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = exp.experimentType.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ModuleExperimentsColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                // Interactive Status Cycle Menu / Chip
                                Surface(
                                    onClick = {
                                        val nextStatus = when (exp.status) {
                                            "PENDING" -> "IN_PROGRESS"
                                            "IN_PROGRESS" -> "VALIDATED"
                                            "VALIDATED" -> "INVALIDATED"
                                            else -> "PENDING"
                                        }
                                        viewModel.updateExperimentStatus(entry.ideaId, exp.id, nextStatus)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = statusColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(statusColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = exp.status.replace("_", " "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "Cycle Status",
                                            tint = statusColor,
                                            modifier = Modifier
                                                .padding(start = 4.dp)
                                                .size(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Hypothesis: ${exp.assumption}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Success Metric: ${exp.successCriteria}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (exp.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${exp.notes}",
                                    fontSize = 12.sp,
                                    color = if (isDark) SlateTextSecondary else Color(0xFF4B5563)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Venture: ${entry.ideaName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ModuleExperimentsColor
                                )

                                TextButton(
                                    onClick = { onNavigateToIdea(entry.ideaId) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Open Idea", fontSize = 11.sp)
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add New Experiment Dialog
    if (showAddExperimentDialog) {
        var inputAssumption by remember { mutableStateOf("") }
        var inputType by remember { mutableStateOf("Concierge MVP") }
        var inputCriteria by remember { mutableStateOf("") }
        var selectedIdeaForNew by remember { mutableStateOf(allIdeas.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddExperimentDialog = false },
            title = { Text("Add Validation Experiment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Associate to Venture:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allIdeas) { idea ->
                            FilterChip(
                                selected = selectedIdeaForNew == idea.id,
                                onClick = { selectedIdeaForNew = idea.id },
                                label = { Text(idea.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputAssumption,
                        onValueChange = { inputAssumption = it },
                        label = { Text("Core Hypothesis / Assumption") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputType,
                        onValueChange = { inputType = it },
                        label = { Text("Test Format (e.g. Concierge MVP, Interview)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputCriteria,
                        onValueChange = { inputCriteria = it },
                        label = { Text("Success Criteria (e.g. 5 signups in 7 days)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputAssumption.isNotBlank() && selectedIdeaForNew.isNotBlank()) {
                            viewModel.addExperiment(
                                ideaId = selectedIdeaForNew,
                                assumption = inputAssumption,
                                type = inputType.ifBlank { "Concierge MVP" },
                                criteria = inputCriteria.ifBlank { "Achieve qualitative validation" }
                            )
                            showAddExperimentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ModuleExperimentsColor)
                ) {
                    Text("Create Test", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExperimentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
