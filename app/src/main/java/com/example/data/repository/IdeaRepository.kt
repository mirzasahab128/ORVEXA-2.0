package com.example.data.repository

import com.example.data.engine.ScoringEngine
import com.example.data.engine.SeedData
import com.example.data.local.IdeaDao
import com.example.data.model.*
import com.example.data.remote.FirebaseSyncManager
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class IdeaRepository(private val ideaDao: IdeaDao) {
    val allIdeas: Flow<List<Idea>> = ideaDao.getAllIdeas()
    val userProfile: Flow<UserProfile?> = ideaDao.getUserProfile()
    private val geminiService = GeminiService()

    suspend fun ensureInitialized() {
        val existing = ideaDao.getAllIdeasOnce()
        if (existing.isEmpty()) {
            ideaDao.insertIdeas(SeedData.sampleIdeas)
            ideaDao.insertUserProfile(UserProfile())
        }
        // FINALIZE FIX (2026-10-26): the old else-branch re-inserted any
        // missing sample idea on every launch, so a sample the user deleted
        // resurrected on the next start. Samples are seeded once, on a
        // genuinely empty database, and never again.
    }

    /**
     * Two-way last-write-wins reconciliation with Firestore.
     *
     * Pull first: a cloud idea that is newer than the local copy replaces
     * it; a cloud idea with no local copy is inserted. Then push: a local
     * idea that is newer than its cloud copy (or has no cloud copy) uploads.
     * Sample ideas are never pushed; they are demo data, not user data.
     * Equal timestamps are stable in both directions, so a completed sync
     * does not re-upload on the next run.
     *
     * Known edge: an idea deleted while offline cannot be told apart from
     * an idea that never existed locally, so its cloud copy may return once
     * on the next sync. Deleting it again (online) removes it for good.
     */
    suspend fun reconcileWithCloud(syncManager: FirebaseSyncManager) {
        if (syncManager.currentUser.value == null) return
        try {
            val cloudIdeas = syncManager.fetchIdeasFromCloud()
            val cloudById = cloudIdeas.associateBy { it.id }
            val localIdeas = ideaDao.getAllIdeasOnce()
            val localById = localIdeas.associateBy { it.id }
            for (cloud in cloudIdeas) {
                val local = localById[cloud.id]
                if (local == null) {
                    ideaDao.insertIdea(cloud)
                } else if (cloud.updatedAt > local.updatedAt) {
                    ideaDao.insertIdea(cloud)
                }
            }
            for (local in localIdeas) {
                if (local.isSample) continue
                val cloud = cloudById[local.id]
                if (cloud == null || local.updatedAt > cloud.updatedAt) {
                    syncManager.syncIdeaToCloud(local)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("IdeaRepository", "Error reconciling with cloud", e)
        }
    }

    suspend fun resetToSeedData() {
        ideaDao.clearAllIdeas()
        ideaDao.insertIdeas(SeedData.sampleIdeas)
        ideaDao.insertUserProfile(UserProfile())
    }

    fun getIdea(id: String): Flow<Idea?> {
        return ideaDao.getIdeaById(id)
    }

    suspend fun saveIdeaLocally(
        title: String,
        description: String,
        category: String = "AI & Tech",
        tag: String = "B2B",
        priority: String = "High",
        categories: List<String> = listOf(category, tag),
        strategicHypothesis: String = ""
    ): Idea {
        val newId = "idea-" + UUID.randomUUID().toString().take(8)
        val oppScore = 75
        val timingScore = 75
        val feasibilityScore = 70
        val rec = ScoringEngine.deriveRecommendation(oppScore, timingScore, feasibilityScore)

        val newIdea = Idea(
            id = newId,
            name = title.trim(),
            oneLineThesis = description.trim(),
            description = description.trim(),
            category = category,
            tag = tag,
            categories = categories,
            priority = priority,
            strategicHypothesis = strategicHypothesis.trim(),
            status = IdeaStatus.EVALUATING,
            isSample = false,
            opportunityScore = oppScore,
            marketScore = oppScore,
            feasibilityScore = feasibilityScore,
            timingScore = timingScore,
            healthScore = oppScore,
            recommendation = rec,
            recommendationReason = "Locally captured opportunity. Evaluate target customer willingness to pay and market timing signals.",
            whyNow = "Emerging market tailwinds and rapid software tooling enable fast prototype validation.",
            whyNot = "Early stage hypothesis; buyer objections not yet fully mapped.",
            nextImmediateAction = "Conduct 3 customer discovery calls with prospective users.",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        ideaDao.insertIdea(newIdea)
        return newIdea
    }

    suspend fun analyzeAndApplyRiskVsReward(ideaId: String): IdeaRiskRewardAnalysis? {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return null
        val analysis = geminiService.analyzeSavedIdeaRiskVsReward(existing)
        val updated = existing.copy(
            riskVsRewardScore = analysis.riskVsRewardScore,
            riskScore = analysis.riskScore,
            rewardScore = analysis.rewardScore,
            riskVsRewardRatio = analysis.riskVsRewardRatio,
            potentialMarketImpactSummary = analysis.potentialMarketImpactSummary,
            strategicHypothesis = if (existing.strategicHypothesis.isBlank()) analysis.strategicHypothesis else existing.strategicHypothesis,
            updatedAt = System.currentTimeMillis()
        )
        ideaDao.updateIdea(updated)
        return analysis
    }

    suspend fun analyzeAndSaveNewIdea(
        rawIdeaInput: String,
        category: String = "AI & Tech",
        tag: String = "B2B",
        priority: String = "High"
    ): Idea {
        val analysis = geminiService.analyzeIdea(rawIdeaInput, category, tag)
        val newId = "idea-" + UUID.randomUUID().toString().take(8)
        val oppScore = (analysis["opportunityScore"] as? Int) ?: 75
        val timingScore = (analysis["timingScore"] as? Int) ?: 75
        val feasibilityScore = (analysis["feasibilityScore"] as? Int) ?: 70
        val recStr = (analysis["recommendation"] as? String) ?: "VALIDATE_FIRST"
        val rec = try { Recommendation.valueOf(recStr) } catch (_: Exception) { Recommendation.VALIDATE_FIRST }

        val newIdea = Idea(
            id = newId,
            name = (analysis["name"] as? String) ?: rawIdeaInput.take(30),
            oneLineThesis = (analysis["oneLineThesis"] as? String) ?: rawIdeaInput,
            description = rawIdeaInput,
            category = category,
            tag = tag,
            categories = listOf(category, tag),
            priority = priority,
            status = IdeaStatus.EVALUATING,
            isSample = false,
            opportunityScore = oppScore,
            marketScore = oppScore,
            feasibilityScore = feasibilityScore,
            timingScore = timingScore,
            healthScore = oppScore,
            recommendation = rec,
            recommendationReason = (analysis["recommendationReason"] as? String) ?: "",
            whyNow = (analysis["whyNow"] as? String) ?: "",
            biggestRisk = (analysis["biggestRisk"] as? String) ?: "",
            nextImmediateAction = (analysis["nextImmediateAction"] as? String) ?: "Conduct 5 customer discovery interviews.",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        ideaDao.insertIdea(newIdea)
        return newIdea
    }

    suspend fun updateIdeaPriority(ideaId: String, newPriority: String) {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return
        val updated = existing.copy(priority = newPriority, updatedAt = System.currentTimeMillis())
        ideaDao.updateIdea(updated)
    }

    suspend fun updateIdea(idea: Idea) {
        ideaDao.updateIdea(idea.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteIdea(id: String) {
        ideaDao.deleteIdeaById(id)
    }

    suspend fun restoreIdea(idea: Idea) {
        ideaDao.insertIdea(idea.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleActionItem(ideaId: String, actionId: String) {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return
        val updatedActions = existing.actionItems.map {
            if (it.id == actionId) it.copy(completed = !it.completed) else it
        }
        ideaDao.updateIdea(existing.copy(actionItems = updatedActions, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateExperimentStatus(ideaId: String, experimentId: String, newStatus: String) {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return
        val updatedExp = existing.experiments.map {
            if (it.id == experimentId) it.copy(status = newStatus) else it
        }
        ideaDao.updateIdea(existing.copy(experiments = updatedExp, updatedAt = System.currentTimeMillis()))
    }

    suspend fun sendAdvisorMessage(ideaId: String, userText: String): String {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return "Idea not found."
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = userText)
        val historySnippet = existing.chatHistory.takeLast(4).joinToString("\n") { (if (it.isUser) "User: " else "Advisor: ") + it.text }

        val replyText = geminiService.askAdvisor(existing.name, historySnippet, userText)
        val advisorMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = false, text = replyText)

        val newHistory = existing.chatHistory + userMsg + advisorMsg
        ideaDao.updateIdea(existing.copy(chatHistory = newHistory, updatedAt = System.currentTimeMillis()))
        return replyText
    }

    suspend fun applyHumanOverride(
        ideaId: String,
        newOpportunity: Int,
        newTiming: Int,
        newFeasibility: Int,
        newRecommendation: Recommendation,
        overrideReason: String
    ) {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return
        val historyEntry = ScoreHistoryEntry(
            timestamp = System.currentTimeMillis(),
            dateLabel = "Today",
            opportunityScore = newOpportunity,
            timingScore = newTiming,
            feasibilityScore = newFeasibility,
            reason = "Human Override: $overrideReason"
        )
        val updated = existing.copy(
            opportunityScore = newOpportunity,
            timingScore = newTiming,
            feasibilityScore = newFeasibility,
            recommendation = newRecommendation,
            scoreHistory = existing.scoreHistory + historyEntry,
            updatedAt = System.currentTimeMillis()
        )
        ideaDao.updateIdea(updated)
    }

    suspend fun reanalyzeIdea(ideaId: String) {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return
        val analysis = geminiService.analyzeIdea(existing.description, existing.category, existing.tag)
        val oppScore = (analysis["opportunityScore"] as? Int) ?: existing.opportunityScore
        val timingScore = (analysis["timingScore"] as? Int) ?: existing.timingScore
        val feasibilityScore = (analysis["feasibilityScore"] as? Int) ?: existing.feasibilityScore
        val recStr = (analysis["recommendation"] as? String) ?: existing.recommendation.name
        val rec = try { Recommendation.valueOf(recStr) } catch (_: Exception) { existing.recommendation }

        val updated = existing.copy(
            opportunityScore = oppScore,
            timingScore = timingScore,
            feasibilityScore = feasibilityScore,
            recommendation = rec,
            recommendationReason = (analysis["recommendationReason"] as? String) ?: existing.recommendationReason,
            updatedAt = System.currentTimeMillis()
        )
        ideaDao.updateIdea(updated)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        ideaDao.insertUserProfile(profile)
    }

    suspend fun getSuggestedPrompts(): List<IdeaPromptSuggestion> {
        return geminiService.suggestIdeaPrompts()
    }

    suspend fun generateCustomPrompts(keyword: String): List<IdeaPromptSuggestion> {
        return geminiService.generateCustomPromptsWithOptions(keyword)
    }

    suspend fun analyzeIdeaRaw(input: String, category: String, tag: String): Map<String, Any> {
        return geminiService.analyzeIdea(input, category, tag)
    }

    suspend fun askAdvisorScratchpad(title: String, history: List<ChatMessage>, userMessage: String): String {
        val snippet = history.takeLast(4).joinToString("\n") { (if (it.isUser) "User: " else "Advisor: ") + it.text }
        return geminiService.askAdvisor(title, snippet, userMessage)
    }

    private val benchmarkService = com.example.data.engine.AutomatedBenchmarkScoringService()

    suspend fun evaluateIdeaAgainstBenchmarks(ideaId: String): Idea? {
        val existing = ideaDao.getIdeaByIdOnce(ideaId) ?: return null
        return benchmarkService.evaluateAndStore(existing, ideaDao)
    }

    suspend fun evaluateIdeaDirectly(idea: Idea): Idea {
        return benchmarkService.evaluateAndStore(idea, ideaDao)
    }

    suspend fun batchEvaluateBenchmarks(ideaIds: List<String>): List<Idea> {
        val updated = mutableListOf<Idea>()
        for (id in ideaIds) {
            val idea = ideaDao.getIdeaByIdOnce(id)
            if (idea != null) {
                val evaluated = benchmarkService.evaluateAndStore(idea, ideaDao)
                updated.add(evaluated)
            }
        }
        return updated
    }
}
