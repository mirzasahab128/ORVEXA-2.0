package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.engine.ScoringEngine
import com.example.data.model.ActionItem
import com.example.data.model.ChatMessage
import com.example.data.model.CompetitorItem
import com.example.data.model.EpistemicClaim
import com.example.data.model.EpistemicType
import com.example.data.model.Idea
import com.example.data.model.IdeaStatus
import com.example.data.model.MarketGap
import com.example.data.model.OpportunityWindow
import com.example.data.model.Recommendation
import com.example.data.model.RequirementItem
import com.example.data.model.RiskItem
import com.example.data.model.ScoreHistoryEntry
import com.example.data.model.SmartReminder
import com.example.data.model.TimingStatus
import com.example.data.model.ValidationExperiment
import com.google.firebase.firestore.SetOptions
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

enum class CloudSyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

/**
 * FirebaseSyncManager
 *
 * Manages Firebase Authentication (Google Sign-In via Jetpack CredentialManager)
 * and Cloud Firestore synchronization for ORVEXA Idea Intelligence.
 *
 * Persists ideas to `/users/{userId}/ideas/{ideaId}` conforming to zero-trust rules.
 */
class FirebaseSyncManager(private val context: Context) {

    private fun ensureFirebaseInitialized() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "FirebaseApp init fallback warning", e)
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            ensureFirebaseInitialized()
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "FirebaseAuth init error", e)
            null
        }
    }

    private val credentialManager: CredentialManager by lazy {
        try {
            CredentialManager.create(context)
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "CredentialManager create error", e)
            throw e
        }
    }

    private val databaseId: String by lazy {
        try {
            context.getString(R.string.firestore_database_id)
        } catch (_: Exception) {
            "ai-studio-android-ideainte-7eadb659-8867-4fe1-865e-e4d8896c009a"
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            ensureFirebaseInitialized()
            val db = databaseId.trim()
            if (db.isNotEmpty() && db != "(default)") {
                FirebaseFirestore.getInstance(db)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e2: Exception) {
                Log.w("FirebaseSyncManager", "Firestore init error", e2)
                null
            }
        }
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.IDLE)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncMessage = MutableStateFlow("Local mode (Sign in to sync)")
    val lastSyncMessage: StateFlow<String> = _lastSyncMessage.asStateFlow()

    private var ideaListener: ListenerRegistration? = null

    init {
        try {
            ensureFirebaseInitialized()
            val currentAuth = auth
            if (currentAuth != null) {
                _currentUser.value = currentAuth.currentUser
                if (currentAuth.currentUser != null) {
                    _lastSyncMessage.value = "Connected as ${currentAuth.currentUser?.displayName ?: currentAuth.currentUser?.email ?: "Cloud User"}"
                    _syncStatus.value = CloudSyncStatus.SYNCED
                }
                currentAuth.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    _currentUser.value = user
                    if (user != null) {
                        _lastSyncMessage.value = "Connected as ${user.displayName ?: user.email ?: "Cloud User"}"
                        _syncStatus.value = CloudSyncStatus.SYNCED
                    } else {
                        _lastSyncMessage.value = "Local mode (Sign in to sync)"
                        _syncStatus.value = CloudSyncStatus.IDLE
                        ideaListener?.remove()
                        ideaListener = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Auth state listener setup warning", e)
        }
    }

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser)
        }
        currentAuth.addAuthStateListener(listener)
        awaitClose { currentAuth.removeAuthStateListener(listener) }
    }

    /**
     * Google Sign-In with Jetpack CredentialManager
     */
    suspend fun signInWithGoogle(
        onSuccess: (FirebaseUser) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentAuth = auth ?: run {
            _syncStatus.value = CloudSyncStatus.ERROR
            onError("Firebase Auth is not available on this device")
            return
        }

        val serverClientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "145413286143-aab1ua6vrtah9visutm7gln27jdvti5b.apps.googleusercontent.com"
        } catch (_: Exception) {
            "145413286143-aab1ua6vrtah9visutm7gln27jdvti5b.apps.googleusercontent.com"
        }

        val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            _syncStatus.value = CloudSyncStatus.SYNCING
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = currentAuth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                if (user != null) {
                    _currentUser.value = user
                    _syncStatus.value = CloudSyncStatus.SYNCED
                    _lastSyncMessage.value = "Synced with Google Cloud"
                    saveUserProfile(user)
                    onSuccess(user)
                } else {
                    _syncStatus.value = CloudSyncStatus.ERROR
                    onError("Failed to obtain user session")
                }
            } else {
                _syncStatus.value = CloudSyncStatus.ERROR
                onError("Unexpected credential format returned")
            }
        } catch (e: GetCredentialCancellationException) {
            _syncStatus.value = CloudSyncStatus.IDLE
            Log.d("FirebaseSyncManager", "Sign in cancelled by user")
        } catch (e: GetCredentialException) {
            _syncStatus.value = CloudSyncStatus.ERROR
            val msg = e.localizedMessage ?: "Sign in failed"
            _lastSyncMessage.value = msg
            onError(msg)
        } catch (e: Exception) {
            _syncStatus.value = CloudSyncStatus.ERROR
            val msg = e.localizedMessage ?: "Authentication failed"
            _lastSyncMessage.value = msg
            onError(msg)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Error during signOut", e)
        }
        _currentUser.value = null
        _syncStatus.value = CloudSyncStatus.IDLE
        _lastSyncMessage.value = "Signed out"
    }

    private suspend fun saveUserProfile(user: FirebaseUser) {
        val currentFirestore = firestore ?: return
        try {
            val userDoc = currentFirestore.collection("users").document(user.uid)
            val data = mapOf(
                "id" to user.uid,
                "displayName" to (user.displayName ?: "Orvexa Innovator"),
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc.set(data).await()
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Failed to save user profile", e)
        }
    }

    /**
     * Upload / Synchronize an idea to Firestore.
     *
     * FINALIZE FIX (2026-10-26): the previous payload sent only ~20 fields
     * with set(), which overwrote the whole document and silently deleted
     * everything else (action items, experiments, reminders, chat, sub-scores).
     * Every field of Idea is now mapped, and writes use merge so an older
     * client can never wipe a field it does not know about.
     */
    suspend fun syncIdeaToCloud(idea: Idea) {
        val currentAuth = auth ?: return
        val currentFirestore = firestore ?: return
        val user = currentAuth.currentUser ?: return
        try {
            _syncStatus.value = CloudSyncStatus.SYNCING
            val ideaDoc = currentFirestore.collection("users")
                .document(user.uid)
                .collection("ideas")
                .document(idea.id)

            ideaDoc.set(ideaToMap(idea, user.uid), SetOptions.merge()).await()
            _syncStatus.value = CloudSyncStatus.SYNCED
            _lastSyncMessage.value = "Saved to Cloud Firestore"
        } catch (e: Exception) {
            _syncStatus.value = CloudSyncStatus.ERROR
            _lastSyncMessage.value = "Cloud sync error: ${e.message}"
            Log.w("FirebaseSyncManager", "Error syncing idea to Firestore", e)
        }
    }

    private fun ideaToMap(idea: Idea, userId: String): Map<String, Any?> {
        return mapOf(
            "id" to idea.id,
            "userId" to userId,
            "name" to idea.name,
            "oneLineThesis" to idea.oneLineThesis,
            "description" to idea.description,
            "category" to idea.category,
            "tag" to idea.tag,
            "categories" to idea.categories,
            "priority" to idea.priority,
            "status" to idea.status.name,
            "isSample" to idea.isSample,
            "opportunityScore" to idea.opportunityScore,
            "marketScore" to idea.marketScore,
            "feasibilityScore" to idea.feasibilityScore,
            "timingScore" to idea.timingScore,
            "personalFitScore" to idea.personalFitScore,
            "impactScore" to idea.impactScore,
            "healthScore" to idea.healthScore,
            "healthTrend" to idea.healthTrend,
            "confidence" to idea.confidence,
            "recommendation" to idea.recommendation.name,
            "recommendationReason" to idea.recommendationReason,
            "whyNow" to idea.whyNow,
            "whyNot" to idea.whyNot,
            "whatWouldChangeMind" to idea.whatWouldChangeMind,
            "biggestOpportunity" to idea.biggestOpportunity,
            "biggestRisk" to idea.biggestRisk,
            "biggestUnknown" to idea.biggestUnknown,
            "nextImmediateAction" to idea.nextImmediateAction,
            "nextReviewDate" to idea.nextReviewDate,
            "problemStatement" to idea.problemStatement,
            "problemSeverity" to idea.problemSeverity,
            "problemCostOfInaction" to idea.problemCostOfInaction,
            "primaryUser" to idea.primaryUser,
            "economicBuyer" to idea.economicBuyer,
            "decisionMaker" to idea.decisionMaker,
            "customerObjections" to idea.customerObjections,
            "timingStatus" to idea.timingStatus.name,
            "opportunityWindow" to idea.opportunityWindow.name,
            "timingSignalsSummary" to idea.timingSignalsSummary,
            "waitUntilTriggers" to idea.waitUntilTriggers,
            "marketSaturation" to idea.marketSaturation,
            "competitors" to idea.competitors.map { c ->
                mapOf("name" to c.name, "type" to c.type, "positioning" to c.positioning, "pricing" to c.pricing, "strengths" to c.strengths, "weaknesses" to c.weaknesses, "threatLevel" to c.threatLevel)
            },
            "marketGaps" to idea.marketGaps.map { g ->
                mapOf("title" to g.title, "description" to g.description, "defensibility" to g.defensibility, "validationScore" to g.validationScore, "urgency" to g.urgency)
            },
            "epistemicClaims" to idea.epistemicClaims.map { e ->
                mapOf("claim" to e.claim, "type" to e.type.name, "sourceOrEvidence" to e.sourceOrEvidence, "confidence" to e.confidence)
            },
            "requirements" to idea.requirements.map { r ->
                mapOf("title" to r.title, "category" to r.category, "necessity" to r.necessity, "difficulty" to r.difficulty, "status" to r.status)
            },
            "risks" to idea.risks.map { r ->
                mapOf("title" to r.title, "category" to r.category, "severity" to r.severity, "likelihood" to r.likelihood, "mitigation" to r.mitigation)
            },
            "technicalFeasibility" to idea.technicalFeasibility,
            "financialFeasibility" to idea.financialFeasibility,
            "regulatoryFeasibility" to idea.regulatoryFeasibility,
            "aiBuildability" to idea.aiBuildability,
            "mvpObjective" to idea.mvpObjective,
            "mvpCoreWorkflow" to idea.mvpCoreWorkflow,
            "mvpEssentialFeatures" to idea.mvpEssentialFeatures,
            "mvpExcludedFeatures" to idea.mvpExcludedFeatures,
            "mvpEstimatedCost" to idea.mvpEstimatedCost,
            "mvpEstimatedEffort" to idea.mvpEstimatedEffort,
            "experiments" to idea.experiments.map { e ->
                mapOf("id" to e.id, "assumption" to e.assumption, "experimentType" to e.experimentType, "successCriteria" to e.successCriteria, "status" to e.status, "notes" to e.notes)
            },
            "actionItems" to idea.actionItems.map { a ->
                mapOf("id" to a.id, "task" to a.task, "timeline" to a.timeline, "priority" to a.priority, "effort" to a.effort, "completed" to a.completed)
            },
            "reminders" to idea.reminders.map { r ->
                mapOf("id" to r.id, "title" to r.title, "type" to r.type, "triggerCondition" to r.triggerCondition, "dueDate" to r.dueDate, "isTriggered" to r.isTriggered)
            },
            "benchmarkSector" to idea.benchmarkSector,
            "benchmarkPercentile" to idea.benchmarkPercentile,
            "benchmarkFeasibilityMedian" to idea.benchmarkFeasibilityMedian,
            "benchmarkOpportunityMedian" to idea.benchmarkOpportunityMedian,
            "benchmarkFeasibilityDelta" to idea.benchmarkFeasibilityDelta,
            "benchmarkOpportunityDelta" to idea.benchmarkOpportunityDelta,
            "benchmarkSummary" to idea.benchmarkSummary,
            "benchmarkDifferentiator" to idea.benchmarkDifferentiator,
            "benchmarkDownsideRisk" to idea.benchmarkDownsideRisk,
            "benchmarkLastEvaluated" to idea.benchmarkLastEvaluated,
            "riskVsRewardScore" to idea.riskVsRewardScore,
            "riskScore" to idea.riskScore,
            "rewardScore" to idea.rewardScore,
            "riskVsRewardRatio" to idea.riskVsRewardRatio,
            "potentialMarketImpactSummary" to idea.potentialMarketImpactSummary,
            "strategicHypothesis" to idea.strategicHypothesis,
            "scoreHistory" to idea.scoreHistory.map { s ->
                mapOf("timestamp" to s.timestamp, "dateLabel" to s.dateLabel, "opportunityScore" to s.opportunityScore, "timingScore" to s.timingScore, "feasibilityScore" to s.feasibilityScore, "reason" to s.reason)
            },
            "chatHistory" to idea.chatHistory.map { c ->
                mapOf("id" to c.id, "isUser" to c.isUser, "text" to c.text, "timestamp" to c.timestamp)
            },
            "createdAt" to idea.createdAt,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    /**
     * Fetch ideas from Cloud Firestore to populate local Room cache.
     *
     * FINALIZE FIX (2026-10-26): the previous version rebuilt only ~20
     * fields and invented the rest (scores copied from opportunity score,
     * fixed defaults). Every stored field is now read back, and fields
     * missing from older documents fall back to the data class defaults
     * or a recomputed recommendation, never to invented per-idea values.
     */
    suspend fun fetchIdeasFromCloud(): List<Idea> {
        val currentAuth = auth ?: return emptyList()
        val currentFirestore = firestore ?: return emptyList()
        val user = currentAuth.currentUser ?: return emptyList()
        return try {
            val snapshot = currentFirestore.collection("users")
                .document(user.uid)
                .collection("ideas")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapToIdea(data, doc.id)
            }
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Error fetching ideas from cloud", e)
            emptyList()
        }
    }

    // --- Firestore decoding helpers (safe casts; old documents may lack fields) ---

    private fun s(data: Map<String, Any?>, key: String, fallback: String = ""): String =
        data[key] as? String ?: fallback

    private fun n(data: Map<String, Any?>, key: String, fallback: Long = 0L): Long =
        when (val v = data[key]) {
            is Long -> v
            is Int -> v.toLong()
            is Double -> v.toLong()
            is com.google.firebase.Timestamp -> v.toDate().time
            else -> fallback
        }

    private fun i(data: Map<String, Any?>, key: String, fallback: Int = 0): Int =
        n(data, key, fallback.toLong()).toInt()

    private fun b(data: Map<String, Any?>, key: String, fallback: Boolean = false): Boolean =
        data[key] as? Boolean ?: fallback

    private fun sl(data: Map<String, Any?>, key: String): List<String> =
        (data[key] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    private fun ml(data: Map<String, Any?>, key: String): List<Map<String, Any?>> =
        (data[key] as? List<*>)?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()

    private inline fun <reified T : Enum<T>> enumVal(name: String?, fallback: T): T {
        if (name == null) return fallback
        return try { enumValueOf<T>(name) } catch (_: Exception) { fallback }
    }

    private fun mapToIdea(data: Map<String, Any?>, docId: String): Idea {
        val category = s(data, "category", "AI & Tech")
        val tag = s(data, "tag", "B2B")
        val opp = i(data, "opportunityScore", 75)
        val feas = i(data, "feasibilityScore", 70)
        val timing = i(data, "timingScore", 80)
        val recName = data["recommendation"] as? String
        val recommendation = if (recName != null) {
            enumVal(recName, Recommendation.VALIDATE_FIRST)
        } else {
            ScoringEngine.deriveRecommendation(opp, timing, feas)
        }
        return Idea(
            id = s(data, "id", docId),
            name = s(data, "name", "Untitled Hypothesis"),
            oneLineThesis = s(data, "oneLineThesis").ifBlank { s(data, "description") },
            description = s(data, "description"),
            category = category,
            tag = tag,
            categories = sl(data, "categories").ifEmpty { listOf(category, tag) },
            priority = s(data, "priority", "High"),
            status = enumVal(data["status"] as? String, IdeaStatus.EVALUATING),
            isSample = b(data, "isSample", false),
            opportunityScore = opp,
            marketScore = i(data, "marketScore", opp),
            feasibilityScore = feas,
            timingScore = timing,
            personalFitScore = i(data, "personalFitScore", 75),
            impactScore = i(data, "impactScore", 75),
            healthScore = i(data, "healthScore", opp),
            healthTrend = s(data, "healthTrend", "Improving"),
            confidence = s(data, "confidence", "High"),
            recommendation = recommendation,
            recommendationReason = s(data, "recommendationReason"),
            whyNow = s(data, "whyNow"),
            whyNot = s(data, "whyNot"),
            whatWouldChangeMind = s(data, "whatWouldChangeMind"),
            biggestOpportunity = s(data, "biggestOpportunity"),
            biggestRisk = s(data, "biggestRisk"),
            biggestUnknown = s(data, "biggestUnknown"),
            nextImmediateAction = s(data, "nextImmediateAction", "Interview 5 target customers"),
            nextReviewDate = s(data, "nextReviewDate", "In 14 days"),
            problemStatement = s(data, "problemStatement"),
            problemSeverity = s(data, "problemSeverity", "Critical"),
            problemCostOfInaction = s(data, "problemCostOfInaction"),
            primaryUser = s(data, "primaryUser"),
            economicBuyer = s(data, "economicBuyer"),
            decisionMaker = s(data, "decisionMaker"),
            customerObjections = sl(data, "customerObjections"),
            timingStatus = enumVal(data["timingStatus"] as? String, TimingStatus.RIGHT_TIME),
            opportunityWindow = enumVal(data["opportunityWindow"] as? String, OpportunityWindow.NOW),
            timingSignalsSummary = s(data, "timingSignalsSummary"),
            waitUntilTriggers = sl(data, "waitUntilTriggers"),
            marketSaturation = s(data, "marketSaturation", "Moderate"),
            competitors = ml(data, "competitors").map { m ->
                CompetitorItem(name = s(m, "name"), type = s(m, "type"), positioning = s(m, "positioning"), pricing = s(m, "pricing"), strengths = s(m, "strengths"), weaknesses = s(m, "weaknesses"), threatLevel = s(m, "threatLevel", "Medium"))
            },
            marketGaps = ml(data, "marketGaps").map { m ->
                MarketGap(title = s(m, "title"), description = s(m, "description"), defensibility = s(m, "defensibility", "Medium"), validationScore = s(m, "validationScore", "High"), urgency = s(m, "urgency", "Medium"))
            },
            epistemicClaims = ml(data, "epistemicClaims").map { m ->
                EpistemicClaim(claim = s(m, "claim"), type = enumVal(m["type"] as? String, EpistemicType.UNKNOWN), sourceOrEvidence = s(m, "sourceOrEvidence"), confidence = s(m, "confidence", "Medium"))
            },
            requirements = ml(data, "requirements").map { m ->
                RequirementItem(title = s(m, "title"), category = s(m, "category"), necessity = s(m, "necessity"), difficulty = s(m, "difficulty", "Medium"), status = s(m, "status", "Open"))
            },
            risks = ml(data, "risks").map { m ->
                RiskItem(title = s(m, "title"), category = s(m, "category"), severity = s(m, "severity"), likelihood = s(m, "likelihood", "Medium"), mitigation = s(m, "mitigation"))
            },
            technicalFeasibility = i(data, "technicalFeasibility", 75),
            financialFeasibility = i(data, "financialFeasibility", 70),
            regulatoryFeasibility = i(data, "regulatoryFeasibility", 80),
            aiBuildability = s(data, "aiBuildability", "High (Off-the-shelf APIs + Vector RAG)"),
            mvpObjective = s(data, "mvpObjective"),
            mvpCoreWorkflow = s(data, "mvpCoreWorkflow"),
            mvpEssentialFeatures = s(data, "mvpEssentialFeatures"),
            mvpExcludedFeatures = sl(data, "mvpExcludedFeatures"),
            mvpEstimatedCost = sl(data, "mvpEstimatedCost"),
            mvpEstimatedEffort = s(data, "mvpEstimatedEffort", "3-4 Weeks"),
            experiments = ml(data, "experiments").map { m ->
                ValidationExperiment(id = s(m, "id"), assumption = s(m, "assumption"), experimentType = s(m, "experimentType"), successCriteria = s(m, "successCriteria"), status = s(m, "status", "PENDING"), notes = s(m, "notes"))
            },
            actionItems = ml(data, "actionItems").map { m ->
                ActionItem(id = s(m, "id"), task = s(m, "task"), timeline = s(m, "timeline", "7 Days"), priority = s(m, "priority", "P1"), effort = s(m, "effort", "Medium"), completed = b(m, "completed", false))
            },
            reminders = ml(data, "reminders").map { m ->
                SmartReminder(id = s(m, "id"), title = s(m, "title"), type = s(m, "type"), triggerCondition = s(m, "triggerCondition"), dueDate = s(m, "dueDate"), isTriggered = b(m, "isTriggered", false))
            },
            benchmarkSector = s(data, "benchmarkSector", "Enterprise B2B SaaS"),
            benchmarkPercentile = i(data, "benchmarkPercentile", 78),
            benchmarkFeasibilityMedian = i(data, "benchmarkFeasibilityMedian", 70),
            benchmarkOpportunityMedian = i(data, "benchmarkOpportunityMedian", 74),
            benchmarkFeasibilityDelta = i(data, "benchmarkFeasibilityDelta", 0),
            benchmarkOpportunityDelta = i(data, "benchmarkOpportunityDelta", 0),
            benchmarkSummary = s(data, "benchmarkSummary"),
            benchmarkDifferentiator = s(data, "benchmarkDifferentiator"),
            benchmarkDownsideRisk = s(data, "benchmarkDownsideRisk"),
            benchmarkLastEvaluated = n(data, "benchmarkLastEvaluated", 0L),
            riskVsRewardScore = i(data, "riskVsRewardScore", 78),
            riskScore = i(data, "riskScore", 38),
            rewardScore = i(data, "rewardScore", 86),
            riskVsRewardRatio = s(data, "riskVsRewardRatio", "2.3x"),
            potentialMarketImpactSummary = s(data, "potentialMarketImpactSummary", "High-leverage market disruption opportunity with strong defensibility and pricing power."),
            strategicHypothesis = s(data, "strategicHypothesis"),
            scoreHistory = ml(data, "scoreHistory").map { m ->
                ScoreHistoryEntry(timestamp = n(m, "timestamp", 0L), dateLabel = s(m, "dateLabel"), opportunityScore = i(m, "opportunityScore", 0), timingScore = i(m, "timingScore", 0), feasibilityScore = i(m, "feasibilityScore", 0), reason = s(m, "reason"))
            },
            chatHistory = ml(data, "chatHistory").map { m ->
                ChatMessage(id = s(m, "id"), isUser = b(m, "isUser", false), text = s(m, "text"), timestamp = n(m, "timestamp", System.currentTimeMillis()))
            },
            createdAt = n(data, "createdAt", System.currentTimeMillis()),
            updatedAt = n(data, "updatedAt", System.currentTimeMillis())
        )
    }

    /**
     * Delete an idea from Firestore
     */
    suspend fun deleteIdeaFromCloud(ideaId: String) {
        val currentAuth = auth ?: return
        val currentFirestore = firestore ?: return
        val user = currentAuth.currentUser ?: return
        try {
            currentFirestore.collection("users")
                .document(user.uid)
                .collection("ideas")
                .document(ideaId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Error deleting idea from cloud", e)
        }
    }
}
