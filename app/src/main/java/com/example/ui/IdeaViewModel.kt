package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.IdeaIntelligenceDatabase
import com.example.data.model.Idea
import com.example.data.repository.IdeaRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IdeaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = IdeaIntelligenceDatabase.getDatabase(application)
    private val repository = IdeaRepository(database.ideaDao())

    val allIdeas: StateFlow<List<Idea>> = repository.allIdeas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firebaseSyncManager = com.example.data.remote.FirebaseSyncManager(application)
    val currentUser = firebaseSyncManager.currentUser
    val syncStatus = firebaseSyncManager.syncStatus
    val lastSyncMessage = firebaseSyncManager.lastSyncMessage

    fun signInWithGoogle(
        onSuccess: (com.google.firebase.auth.FirebaseUser) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            firebaseSyncManager.signInWithGoogle(
                onSuccess = { user ->
                    syncAllIdeasToCloud()
                    onSuccess(user)
                },
                onError = onError
            )
        }
    }

    fun signOut() {
        firebaseSyncManager.signOut()
    }

    fun syncAllIdeasToCloud() {
        viewModelScope.launch {
            repository.reconcileWithCloud(firebaseSyncManager)
        }
    }

    // Background auto-sync on connectivity changes. FINALIZE FIX
    // (2026-10-26): AutoSyncService previously had no creation site
    // anywhere, so automatic sync never ran at all.
    private val autoSyncService = com.example.data.remote.AutoSyncService(
        application, repository, firebaseSyncManager
    )

    override fun onCleared() {
        autoSyncService.stop()
        super.onCleared()
    }

    val themeMode = MutableStateFlow(ThemeMode.DARK)
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedTag = MutableStateFlow("All")
    val selectedPriority = MutableStateFlow("All")

    val orbState = MutableStateFlow(com.example.ui.components.OrbState.IDLE)
    val orbRmsLevel = MutableStateFlow(0f)

    fun setOrbState(state: com.example.ui.components.OrbState) {
        orbState.value = state
    }

    fun setOrbRmsLevel(level: Float) {
        orbRmsLevel.value = level
    }

    val deletedIdeas = MutableStateFlow<List<Idea>>(emptyList())

    private val _lastDeletedIdea = MutableStateFlow<Idea?>(null)
    val lastDeletedIdea: StateFlow<Idea?> = _lastDeletedIdea.asStateFlow()

    val allTags: StateFlow<List<String>> = allIdeas.map { ideas ->
        val tags = mutableSetOf<String>()
        ideas.forEach { idea ->
            if (idea.tag.isNotBlank()) tags.add(idea.tag)
            idea.categories.forEach { if (it.isNotBlank() && it != idea.category) tags.add(it) }
        }
        listOf("All") + tags.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val filteredIdeas: StateFlow<List<Idea>> = combine(
        allIdeas,
        searchQuery,
        selectedCategory,
        selectedTag,
        selectedPriority
    ) { ideas, query, category, tag, priority ->
        var list = ideas

        if (category != "All") {
            list = list.filter {
                it.category.equals(category, ignoreCase = true) ||
                it.categories.any { c -> c.equals(category, ignoreCase = true) }
            }
        }

        if (tag != "All") {
            list = list.filter {
                it.tag.equals(tag, ignoreCase = true) ||
                it.categories.any { c -> c.equals(tag, ignoreCase = true) }
            }
        }

        if (priority != "All") {
            list = list.filter {
                it.priority.equals(priority, ignoreCase = true)
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { idea ->
                idea.name.lowercase().contains(q) ||
                idea.oneLineThesis.lowercase().contains(q) ||
                idea.description.lowercase().contains(q) ||
                idea.category.lowercase().contains(q) ||
                idea.tag.lowercase().contains(q) ||
                idea.priority.lowercase().contains(q) ||
                idea.categories.any { it.lowercase().contains(q) } ||
                idea.recommendation.label.lowercase().contains(q)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun toggleTheme() {
        themeMode.value = if (themeMode.value == ThemeMode.DARK) {
            ThemeMode.LIGHT_OBSIDIAN
        } else {
            ThemeMode.DARK
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onCategoryFilterChanged(category: String) {
        selectedCategory.value = category
    }

    fun onTagFilterChanged(tag: String) {
        selectedTag.value = tag
    }

    fun onPriorityFilterChanged(priority: String) {
        selectedPriority.value = priority
    }

    fun clearAllFilters() {
        searchQuery.value = ""
        selectedCategory.value = "All"
        selectedTag.value = "All"
        selectedPriority.value = "All"
    }

    fun deleteIdea(idea: Idea) {
        viewModelScope.launch {
            _lastDeletedIdea.value = idea
            deletedIdeas.value = deletedIdeas.value.filter { it.id != idea.id } + idea
            repository.deleteIdea(idea.id)
            firebaseSyncManager.deleteIdeaFromCloud(idea.id)
        }
    }

    fun undoDelete() {
        val ideaToRestore = _lastDeletedIdea.value ?: return
        _lastDeletedIdea.value = null
        viewModelScope.launch {
            deletedIdeas.value = deletedIdeas.value.filter { it.id != ideaToRestore.id }
            repository.restoreIdea(ideaToRestore)
            firebaseSyncManager.syncIdeaToCloud(ideaToRestore)
        }
    }

    fun restoreIdea(idea: Idea) {
        viewModelScope.launch {
            deletedIdeas.value = deletedIdeas.value.filter { it.id != idea.id }
            repository.restoreIdea(idea)
            firebaseSyncManager.syncIdeaToCloud(idea)
        }
    }

    fun deleteIdeaPermanently(idea: Idea) {
        viewModelScope.launch {
            deletedIdeas.value = deletedIdeas.value.filter { it.id != idea.id }
            firebaseSyncManager.deleteIdeaFromCloud(idea.id)
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            val list = deletedIdeas.value
            deletedIdeas.value = emptyList()
            for (idea in list) {
                firebaseSyncManager.deleteIdeaFromCloud(idea.id)
            }
        }
    }

    fun clearUndo() {
        _lastDeletedIdea.value = null
    }

    fun saveIdea(
        title: String,
        description: String,
        category: String,
        tag: String,
        priority: String,
        customTags: List<String> = emptyList(),
        strategicHypothesis: String = ""
    ) {
        viewModelScope.launch {
            val allCustomCategories = (listOf(category, tag) + customTags).distinct().filter { it.isNotBlank() }
            val savedIdea = repository.saveIdeaLocally(
                title = title,
                description = description,
                category = category,
                tag = tag,
                priority = priority,
                categories = allCustomCategories,
                strategicHypothesis = strategicHypothesis
            )
            firebaseSyncManager.syncIdeaToCloud(savedIdea)
        }
    }

    private val _isAnalyzingRiskReward = MutableStateFlow(false)
    val isAnalyzingRiskReward: StateFlow<Boolean> = _isAnalyzingRiskReward.asStateFlow()

    private val _lastRiskRewardAnalysis = MutableStateFlow<com.example.data.model.IdeaRiskRewardAnalysis?>(null)
    val lastRiskRewardAnalysis: StateFlow<com.example.data.model.IdeaRiskRewardAnalysis?> = _lastRiskRewardAnalysis.asStateFlow()

    fun runRiskVsRewardAnalysis(ideaId: String, onComplete: ((com.example.data.model.IdeaRiskRewardAnalysis?) -> Unit)? = null) {
        viewModelScope.launch {
            _isAnalyzingRiskReward.value = true
            try {
                val result = repository.analyzeAndApplyRiskVsReward(ideaId)
                _lastRiskRewardAnalysis.value = result
                if (result != null) {
                    val updated = repository.getIdea(ideaId).firstOrNull()
                    if (updated != null) {
                        firebaseSyncManager.syncIdeaToCloud(updated)
                    }
                }
                onComplete?.invoke(result)
            } finally {
                _isAnalyzingRiskReward.value = false
            }
        }
    }

    fun resetToSeedData() {
        viewModelScope.launch {
            repository.resetToSeedData()
        }
    }

    fun clearAllIdeas() {
        viewModelScope.launch {
            database.ideaDao().clearAllIdeas()
        }
    }

    fun getIdeaById(id: String): Flow<Idea?> {
        return repository.getIdea(id)
    }

    // AI Analyzer & Conversational Advisor State
    private val _suggestedPrompts = MutableStateFlow<List<com.example.data.model.IdeaPromptSuggestion>>(emptyList())
    val suggestedPrompts: StateFlow<List<com.example.data.model.IdeaPromptSuggestion>> = _suggestedPrompts.asStateFlow()

    private val _isGeneratingPrompts = MutableStateFlow(false)
    val isGeneratingPrompts: StateFlow<Boolean> = _isGeneratingPrompts.asStateFlow()

    private val _isAdvisorThinking = MutableStateFlow(false)
    val isAdvisorThinking: StateFlow<Boolean> = _isAdvisorThinking.asStateFlow()

    private val _scratchpadMessages = MutableStateFlow<List<com.example.data.model.ChatMessage>>(
        listOf(
            com.example.data.model.ChatMessage(
                id = "welcome-scratchpad",
                isUser = false,
                text = "Welcome to the ORVEXA Venture Intelligence Board. I am your evidence-backed strategic copilot. Share an idea thesis, stress-test an existing venture, or request prompts with multi-option execution paths."
            )
        )
    )
    val scratchpadMessages: StateFlow<List<com.example.data.model.ChatMessage>> = _scratchpadMessages.asStateFlow()

    fun loadSuggestedPrompts() {
        viewModelScope.launch {
            _isGeneratingPrompts.value = true
            try {
                _suggestedPrompts.value = repository.getSuggestedPrompts()
            } finally {
                _isGeneratingPrompts.value = false
            }
        }
    }

    fun generateCustomPrompts(keyword: String) {
        viewModelScope.launch {
            _isGeneratingPrompts.value = true
            try {
                val results = repository.generateCustomPrompts(keyword)
                _suggestedPrompts.value = results
            } finally {
                _isGeneratingPrompts.value = false
            }
        }
    }

    fun sendAdvisorMessage(ideaId: String?, userText: String, scratchpadTitle: String = "Venture Thesis") {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _isAdvisorThinking.value = true
            try {
                if (ideaId != null) {
                    repository.sendAdvisorMessage(ideaId, userText)
                } else {
                    val userMsg = com.example.data.model.ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        isUser = true,
                        text = userText
                    )
                    _scratchpadMessages.value = _scratchpadMessages.value + userMsg
                    val reply = repository.askAdvisorScratchpad(scratchpadTitle, _scratchpadMessages.value, userText)
                    val advisorMsg = com.example.data.model.ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        isUser = false,
                        text = reply
                    )
                    _scratchpadMessages.value = _scratchpadMessages.value + advisorMsg
                }
            } finally {
                _isAdvisorThinking.value = false
            }
        }
    }

    fun clearScratchpad() {
        _scratchpadMessages.value = listOf(
            com.example.data.model.ChatMessage(
                id = "welcome-scratchpad",
                isUser = false,
                text = "Intelligence scratchpad reset. Enter a new thesis or prompt to begin analysis."
            )
        )
    }

    fun evaluateIdeaWithAI(
        title: String,
        description: String,
        category: String,
        tag: String,
        onResult: (Map<String, Any>) -> Unit
    ) {
        viewModelScope.launch {
            _isAdvisorThinking.value = true
            try {
                val result = repository.analyzeIdeaRaw(
                    input = if (description.isNotBlank()) description else title,
                    category = category,
                    tag = tag
                )
                onResult(result)
            } finally {
                _isAdvisorThinking.value = false
            }
        }
    }

    fun saveAnalyzedIdea(
        title: String,
        description: String,
        category: String,
        tag: String,
        priority: String,
        analysis: Map<String, Any>? = null
    ) {
        viewModelScope.launch {
            if (analysis != null) {
                val oppScore = (analysis["opportunityScore"] as? Int) ?: 75
                val timingScore = (analysis["timingScore"] as? Int) ?: 75
                val feasibilityScore = (analysis["feasibilityScore"] as? Int) ?: 70
                val recStr = (analysis["recommendation"] as? String) ?: "VALIDATE_FIRST"
                val rec = try {
                    com.example.data.model.Recommendation.valueOf(recStr)
                } catch (_: Exception) {
                    com.example.data.model.Recommendation.VALIDATE_FIRST
                }

                val newId = "idea-" + java.util.UUID.randomUUID().toString().take(8)
                val newIdea = Idea(
                    id = newId,
                    name = title.ifBlank { (analysis["name"] as? String) ?: "New Venture" },
                    oneLineThesis = (analysis["oneLineThesis"] as? String) ?: description,
                    description = description,
                    category = category,
                    tag = tag,
                    categories = listOf(category, tag),
                    priority = priority,
                    opportunityScore = oppScore,
                    marketScore = oppScore,
                    timingScore = timingScore,
                    feasibilityScore = feasibilityScore,
                    healthScore = oppScore,
                    recommendation = rec,
                    recommendationReason = (analysis["recommendationReason"] as? String) ?: "",
                    whyNow = (analysis["whyNow"] as? String) ?: "",
                    biggestRisk = (analysis["biggestRisk"] as? String) ?: "",
                    nextImmediateAction = (analysis["nextImmediateAction"] as? String) ?: "Interview 5 target customers",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                database.ideaDao().insertIdea(newIdea)
            } else {
                saveIdea(title, description, category, tag, priority)
            }
        }
    }

    fun reanalyzeIdea(ideaId: String) {
        viewModelScope.launch {
            _isAdvisorThinking.value = true
            try {
                repository.reanalyzeIdea(ideaId)
            } finally {
                _isAdvisorThinking.value = false
            }
        }
    }

    // Benchmark Scoring State & Functions
    private val _isBenchmarkScoring = MutableStateFlow(false)
    val isBenchmarkScoring: StateFlow<Boolean> = _isBenchmarkScoring.asStateFlow()

    private val _benchmarkEvaluationMessage = MutableStateFlow<String?>(null)
    val benchmarkEvaluationMessage: StateFlow<String?> = _benchmarkEvaluationMessage.asStateFlow()

    fun evaluateIdeaAgainstBenchmarks(ideaId: String, onComplete: ((Idea) -> Unit)? = null) {
        viewModelScope.launch {
            _isBenchmarkScoring.value = true
            _benchmarkEvaluationMessage.value = "Evaluating feasibility and opportunity against industry benchmarks with Gemini..."
            try {
                val updated = repository.evaluateIdeaAgainstBenchmarks(ideaId)
                if (updated != null) {
                    _benchmarkEvaluationMessage.value = "Benchmark scores saved to Room: ${updated.benchmarkPercentile}th percentile (${updated.benchmarkSector})"
                    onComplete?.invoke(updated)
                } else {
                    _benchmarkEvaluationMessage.value = "Could not find idea in database."
                }
            } catch (e: Exception) {
                _benchmarkEvaluationMessage.value = "Benchmark scoring error: ${e.localizedMessage}"
            } finally {
                _isBenchmarkScoring.value = false
            }
        }
    }

    fun batchEvaluateBenchmarks(ideaIds: List<String>, onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            _isBenchmarkScoring.value = true
            _benchmarkEvaluationMessage.value = "Benchmarking ${ideaIds.size} ideas against industry datasets..."
            try {
                val updated = repository.batchEvaluateBenchmarks(ideaIds)
                _benchmarkEvaluationMessage.value = "Successfully benchmarked ${updated.size} ideas."
                onComplete?.invoke(updated.size)
            } catch (e: Exception) {
                _benchmarkEvaluationMessage.value = "Batch benchmark error: ${e.localizedMessage}"
            } finally {
                _isBenchmarkScoring.value = false
            }
        }
    }

    fun clearBenchmarkMessage() {
        _benchmarkEvaluationMessage.value = null
    }

    fun toggleActionItem(ideaId: String, actionId: String) {
        viewModelScope.launch {
            repository.toggleActionItem(ideaId, actionId)
        }
    }

    fun updateExperimentStatus(ideaId: String, experimentId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateExperimentStatus(ideaId, experimentId, newStatus)
        }
    }

    fun toggleReminder(ideaId: String, reminderId: String) {
        viewModelScope.launch {
            val idea = repository.getIdea(ideaId).firstOrNull() ?: return@launch
            val updated = idea.reminders.map {
                if (it.id == reminderId) it.copy(isTriggered = !it.isTriggered) else it
            }
            repository.updateIdea(idea.copy(reminders = updated))
        }
    }

    fun addActionItem(ideaId: String, task: String, priority: String = "P1", timeline: String = "7 Days") {
        viewModelScope.launch {
            val idea = repository.getIdea(ideaId).firstOrNull() ?: return@launch
            val newAction = com.example.data.model.ActionItem(
                id = "act-" + java.util.UUID.randomUUID().toString().take(6),
                task = task.trim(),
                priority = priority,
                timeline = timeline,
                completed = false
            )
            repository.updateIdea(idea.copy(actionItems = idea.actionItems + newAction))
        }
    }

    fun addExperiment(ideaId: String, assumption: String, type: String = "Concierge MVP", criteria: String = "Validation metric") {
        viewModelScope.launch {
            val idea = repository.getIdea(ideaId).firstOrNull() ?: return@launch
            val newExp = com.example.data.model.ValidationExperiment(
                id = "exp-" + java.util.UUID.randomUUID().toString().take(6),
                assumption = assumption.trim(),
                experimentType = type,
                successCriteria = criteria.trim(),
                status = "PENDING"
            )
            repository.updateIdea(idea.copy(experiments = idea.experiments + newExp))
        }
    }

    fun addReminder(ideaId: String, title: String, type: String = "Market", triggerCondition: String = "Quarterly review", dueDate: String = "In 14 days") {
        viewModelScope.launch {
            val idea = repository.getIdea(ideaId).firstOrNull() ?: return@launch
            val newReminder = com.example.data.model.SmartReminder(
                id = "rem-" + java.util.UUID.randomUUID().toString().take(6),
                title = title.trim(),
                type = type,
                triggerCondition = triggerCondition.trim(),
                dueDate = dueDate.trim(),
                isTriggered = false
            )
            repository.updateIdea(idea.copy(reminders = idea.reminders + newReminder))
        }
    }
}
