package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Idea
import com.example.ui.IdeaViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyIdeasScreen(
    viewModel: IdeaViewModel,
    onNavigateToIdea: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val allIdeas by viewModel.allIdeas.collectAsState()
    val filteredIdeas by viewModel.filteredIdeas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val lastDeletedIdea by viewModel.lastDeletedIdea.collectAsState()
    val isBenchmarkScoring by viewModel.isBenchmarkScoring.collectAsState()
    val benchmarkMessage by viewModel.benchmarkEvaluationMessage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Score") } // Score, Date, Name
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedIdeaIds = remember { mutableStateListOf<String>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isDark = themeMode == ThemeMode.DARK
    val primaryAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF0F172A)
    val categories = listOf("All", "Healthcare", "AI & Tech", "B2B SaaS", "Fintech", "Enterprise")
    val priorities = listOf("All", "High", "Medium", "Low")

    // Handle Benchmark Message Snackbar
    LaunchedEffect(benchmarkMessage) {
        benchmarkMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearBenchmarkMessage()
            }
        }
    }

    // Handle Undo Snackbar
    LaunchedEffect(lastDeletedIdea) {
        val deleted = lastDeletedIdea ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "\"${deleted.name}\" deleted from database",
            actionLabel = "UNDO",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        } else {
            viewModel.clearUndo()
        }
    }

    val sortedIdeas = remember(filteredIdeas, sortBy) {
        when (sortBy) {
            "Score" -> filteredIdeas.sortedByDescending { it.opportunityScore }
            "Name" -> filteredIdeas.sortedBy { it.name.lowercase() }
            else -> filteredIdeas.sortedByDescending { it.updatedAt }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFF0F172A),
                    contentColor = Color.White,
                    actionColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF38BDF8),
                    shape = RoundedCornerShape(12.dp)
                )
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
                    if (isSelectionMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isSelectionMode = false
                                    selectedIdeaIds.clear()
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel Selection")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${selectedIdeaIds.size} Selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    if (selectedIdeaIds.size == sortedIdeas.size) {
                                        selectedIdeaIds.clear()
                                    } else {
                                        selectedIdeaIds.clear()
                                        selectedIdeaIds.addAll(sortedIdeas.map { it.id })
                                    }
                                },
                                modifier = Modifier.testTag("select_all_ideas_button")
                            ) {
                                Text(
                                    text = if (selectedIdeaIds.size == sortedIdeas.size) "Deselect All" else "Select All",
                                    color = primaryAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier.testTag("hamburger_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Navigation Menu",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "My Ideas",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDark) Color(0xFF162032) else Color(0xFFE2E8F0)
                                    ) {
                                        Text(
                                            text = "${allIdeas.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Manage and organize saved opportunities",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Select mode toggle
                            IconButton(
                                onClick = { isSelectionMode = true },
                                modifier = Modifier.testTag("toggle_selection_mode_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = "Select Ideas for PDF Export",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier.testTag("my_ideas_export_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export Ideas as PDF or Text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            ThemeSwitcherButton(
                                currentTheme = themeMode,
                                onToggleTheme = { viewModel.toggleTheme() }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (isSelectionMode) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF0F1A2E) else Color(0xFFFFFFFF),
                    border = BorderStroke(1.5.dp, primaryAccent),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val selectedIdeas = allIdeas.filter { it.id in selectedIdeaIds }
                                com.example.ui.export.IdeaExportHelper.exportSelectedIdeasPdf(context, selectedIdeas, "Selected Opportunities Dossier")
                            },
                            enabled = selectedIdeaIds.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                            modifier = Modifier.testTag("export_selected_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF (${selectedIdeaIds.size})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.batchEvaluateBenchmarks(selectedIdeaIds.toList())
                            },
                            enabled = selectedIdeaIds.isNotEmpty() && !isBenchmarkScoring,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("batch_benchmark_button")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Benchmark (${selectedIdeaIds.size})", fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedIdeaIds.clear()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Selection Mode", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                OrvexaObsidianFab(
                    onClick = { showBottomSheet = true },
                    extended = true
                )
            }
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .testTag("my_ideas_lazy_column"),
            contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
        ) {

            // Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    OrvexaSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        totalMatchingCount = filteredIdeas.size,
                        isSearching = searchQuery.isNotBlank()
                    )
                }
            }

            // Category & Priority Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategoryFilterChanged(cat) },
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

                    // Priority Chips & Tags
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        priorities.forEach { p ->
                            val isSelected = selectedPriority == p
                            val pColor = when (p) {
                                "High" -> Color(0xFFEF4444)
                                "Medium" -> Color(0xFFF59E0B)
                                "Low" -> Color(0xFF64748B)
                                else -> primaryAccent
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onPriorityFilterChanged(p) },
                                label = { Text(if (p == "All") "All" else p, style = MaterialTheme.typography.bodySmall) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = pColor.copy(alpha = if (isDark) 0.25f else 0.15f),
                                    selectedLabelColor = pColor,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) pColor else MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        if (allTags.size > 1) {
                            Text(
                                text = "Tag:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            allTags.take(6).forEach { tag ->
                                val isSelected = selectedTag == tag
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onTagFilterChanged(tag) },
                                    label = { Text(if (tag == "All") "All Tags" else "#$tag", style = MaterialTheme.typography.bodySmall) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryAccent.copy(alpha = 0.2f),
                                        selectedLabelColor = primaryAccent,
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
                    }
                }
            }

            // Sort Selector Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Score", "Date", "Name").forEach { s ->
                            val active = sortBy == s
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (active) primaryAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (active) BorderStroke(1.dp, primaryAccent) else null,
                                modifier = Modifier.clickable { sortBy = s }
                            ) {
                                Text(
                                    text = s,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) primaryAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (allIdeas.isEmpty()) {
                item {
                    OrvexaEmptyState(
                        isSearchEmpty = false,
                        onCaptureIdea = { showBottomSheet = true },
                        onLoadSampleData = { viewModel.resetToSeedData() }
                    )
                }
            } else if (sortedIdeas.isEmpty()) {
                item {
                    OrvexaEmptyState(
                        isSearchEmpty = true,
                        searchQuery = searchQuery,
                        onCaptureIdea = { showBottomSheet = true },
                        onClearSearch = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onCategoryFilterChanged("All")
                        }
                    )
                }
            } else {
                items(sortedIdeas, key = { "${it.id}_${it.updatedAt}" }) { idea ->
                    val isSelected = idea.id in selectedIdeaIds
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        if (isSelectionMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedIdeaIds.remove(idea.id) else selectedIdeaIds.add(idea.id)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedIdeaIds.add(idea.id) else selectedIdeaIds.remove(idea.id)
                                    },
                                    modifier = Modifier.testTag("select_idea_checkbox_${idea.id}")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    IdeaCard(
                                        idea = idea,
                                        onClick = {
                                            if (isSelected) selectedIdeaIds.remove(idea.id) else selectedIdeaIds.add(idea.id)
                                        }
                                    )
                                }
                            }
                        } else {
                            SwipeToDeleteIdeaItem(
                                idea = idea,
                                onDelete = { viewModel.deleteIdea(it) }
                            ) {
                                IdeaCard(
                                    idea = idea,
                                    onClick = { onNavigateToIdea(idea.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AddIdeaBottomSheet(
            onDismiss = { showBottomSheet = false },
            onSaveIdea = { title, desc, cat, tag, priority, customTags ->
                viewModel.saveIdea(title, desc, cat, tag, priority, customTags)
            }
        )
    }

    if (showExportDialog) {
        val ideasToExport = if (selectedIdeaIds.isNotEmpty()) {
            allIdeas.filter { it.id in selectedIdeaIds }
        } else {
            allIdeas
        }
        ExportIdeasDialog(
            ideas = ideasToExport,
            onDismiss = { showExportDialog = false }
        )
    }
}
