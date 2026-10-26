package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Recommendation(val label: String) {
    BUILD_NOW("Build Now"),
    VALIDATE_FIRST("Validate First"),
    BUILD_SMALL("Build Small"),
    PARTNER("Partner"),
    REPOSITION("Reposition"),
    PIVOT("Pivot"),
    WATCH("Watch"),
    WAIT("Wait"),
    ABANDON("Abandon")
}

enum class IdeaStatus(val label: String) {
    DRAFT("Draft"),
    EVALUATING("Evaluating"),
    VALIDATING("Validating"),
    BUILDING("Building"),
    PAUSED("Paused"),
    ARCHIVED("Archived")
}

enum class TimingStatus(val label: String) {
    RIGHT_TIME("Right Time"),
    TOO_EARLY("Too Early"),
    TOO_LATE("Too Late"),
    CYCLICAL("Cyclical"),
    EVENT_DRIVEN("Event-Driven"),
    WAITING_FOR_TRIGGER("Waiting for Trigger")
}

enum class OpportunityWindow(val label: String) {
    NOW("Now (<6 mo)"),
    OPENING("Opening (6-18 mo)"),
    MATURE("Mature (18-36 mo)"),
    CLOSING("Closing Window"),
    CLOSED("Window Closed")
}

enum class EpistemicType(val label: String) {
    FACT("Fact"),
    USER_INPUT("User Input"),
    ASSUMPTION("Assumption"),
    INFERENCE("Inference"),
    UNKNOWN("Unknown")
}

data class EpistemicClaim(
    val claim: String,
    val type: EpistemicType,
    val sourceOrEvidence: String = "",
    val confidence: String = "Medium"
)

data class RequirementItem(
    val title: String,
    val category: String, // Technical, Functional, Legal, Customer
    val necessity: String, // MUST, SHOULD, COULD, WONT_NOW
    val difficulty: String = "Medium",
    val status: String = "Open"
)

data class RiskItem(
    val title: String,
    val category: String, // Market, Technical, Legal, Regulatory
    val severity: String, // High, Medium, Low
    val likelihood: String = "Medium",
    val mitigation: String = ""
)

data class CompetitorItem(
    val name: String,
    val type: String, // Direct, Indirect, DIY, Legacy
    val positioning: String = "",
    val pricing: String = "",
    val strengths: String = "",
    val weaknesses: String = "",
    val threatLevel: String = "Medium"
)

data class MarketGap(
    val title: String,
    val description: String,
    val defensibility: String = "Medium",
    val validationScore: String = "High",
    val urgency: String = "Medium"
)

data class ValidationExperiment(
    val id: String,
    val assumption: String,
    val experimentType: String, // Interview, Smoke Test, Concierge MVP, Pre-order
    val successCriteria: String,
    val status: String = "PENDING", // PENDING, IN_PROGRESS, VALIDATED, INVALIDATED
    val notes: String = ""
)

data class ActionItem(
    val id: String,
    val task: String,
    val timeline: String = "7 Days",
    val priority: String = "P1", // P0, P1, P2
    val effort: String = "Medium",
    val completed: Boolean = false
)

data class SmartReminder(
    val id: String,
    val title: String,
    val type: String, // Market, Regulation, Competitor
    val triggerCondition: String,
    val dueDate: String,
    val isTriggered: Boolean = false
)

data class ScoreHistoryEntry(
    val timestamp: Long,
    val dateLabel: String,
    val opportunityScore: Int,
    val timingScore: Int,
    val feasibilityScore: Int,
    val reason: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScoringWeights(
    val marketTimingWeight: Float = 0.30f,
    val feasibilityWeight: Float = 0.25f,
    val economicAdvantageWeight: Float = 0.25f,
    val founderFitWeight: Float = 0.20f
)

data class IdeaPromptSuggestion(
    val title: String,
    val category: String,
    val tag: String,
    val prompt: String,
    val options: List<String> = emptyList()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val skills: String = "Full-stack Engineering, Cloud Infrastructure, Product Strategy",
    val capitalBudget: String = "$10k - $50k",
    val availableHoursPerWeek: Int = 25,
    val domainExperience: String = "Healthcare IT, Enterprise SaaS, AI Workflows",
    val primaryGoal: String = "High-margin B2B Cashflow",
    val technicalProficiency: String = "Expert (Can build production systems)",
    val networkStrength: String = "Strong in Tech, Moderate in Enterprise Sales"
)

@Entity(tableName = "ideas")
data class Idea(
    @PrimaryKey val id: String,
    val name: String,
    val oneLineThesis: String,
    val description: String,
    val category: String,
    val tag: String,
    val categories: List<String> = emptyList(),
    val priority: String = "High",
    val status: IdeaStatus = IdeaStatus.EVALUATING,
    val isSample: Boolean = false,

    // Core Scores (0 - 100)
    val opportunityScore: Int = 75,
    val marketScore: Int = 75,
    val feasibilityScore: Int = 70,
    val timingScore: Int = 80,
    val personalFitScore: Int = 75,
    val impactScore: Int = 75,
    val healthScore: Int = 75,
    val healthTrend: String = "Improving", // Improving, Stable, Declining
    val confidence: String = "High", // High, Medium, Speculative

    // Core Recommendation
    val recommendation: Recommendation = Recommendation.VALIDATE_FIRST,
    val recommendationReason: String = "",
    val whyNow: String = "",
    val whyNot: String = "",
    val whatWouldChangeMind: String = "",
    val biggestOpportunity: String = "",
    val biggestRisk: String = "",
    val biggestUnknown: String = "",
    val nextImmediateAction: String = "Interview 5 target customers",
    val nextReviewDate: String = "In 14 days",

    // Problem & Customer Thesis
    val problemStatement: String = "",
    val problemSeverity: String = "Critical", // Critical, High, Moderate, Low
    val problemCostOfInaction: String = "",
    val primaryUser: String = "",
    val economicBuyer: String = "",
    val decisionMaker: String = "",
    val customerObjections: List<String> = emptyList(),

    // Market Timing & Dynamics
    val timingStatus: TimingStatus = TimingStatus.RIGHT_TIME,
    val opportunityWindow: OpportunityWindow = OpportunityWindow.NOW,
    val timingSignalsSummary: String = "",
    val waitUntilTriggers: List<String> = emptyList(),
    val marketSaturation: String = "Moderate",
    val competitors: List<CompetitorItem> = emptyList(),
    val marketGaps: List<MarketGap> = emptyList(),

    // Evidence & Epistemic Decomposition
    val epistemicClaims: List<EpistemicClaim> = emptyList(),

    // Requirements & Risks
    val requirements: List<RequirementItem> = emptyList(),
    val risks: List<RiskItem> = emptyList(),

    // Feasibility Sub-Scores (0-100)
    val technicalFeasibility: Int = 75,
    val financialFeasibility: Int = 70,
    val regulatoryFeasibility: Int = 80,
    val aiBuildability: String = "High (Off-the-shelf APIs + Vector RAG)",

    // MVP & Execution Architecture
    val mvpObjective: String = "",
    val mvpCoreWorkflow: String = "",
    val mvpEssentialFeatures: String = "",
    val mvpExcludedFeatures: List<String> = emptyList(),
    val mvpEstimatedCost: List<String> = emptyList(),
    val mvpEstimatedEffort: String = "3-4 Weeks",

    // Experiments & Action Items
    val experiments: List<ValidationExperiment> = emptyList(),
    val actionItems: List<ActionItem> = emptyList(),
    val reminders: List<SmartReminder> = emptyList(),

    // Industry Benchmark Intelligence
    val benchmarkSector: String = "Enterprise B2B SaaS",
    val benchmarkPercentile: Int = 78,
    val benchmarkFeasibilityMedian: Int = 70,
    val benchmarkOpportunityMedian: Int = 74,
    val benchmarkFeasibilityDelta: Int = 0,
    val benchmarkOpportunityDelta: Int = 0,
    val benchmarkSummary: String = "",
    val benchmarkDifferentiator: String = "",
    val benchmarkDownsideRisk: String = "",
    val benchmarkLastEvaluated: Long = 0L,

    // Risk vs Reward & Market Impact Intelligence (Gemini Analyzed)
    val riskVsRewardScore: Int = 78,
    val riskScore: Int = 38,
    val rewardScore: Int = 86,
    val riskVsRewardRatio: String = "2.3x",
    val potentialMarketImpactSummary: String = "High-leverage market disruption opportunity with strong defensibility and pricing power.",
    val strategicHypothesis: String = "",

    // Historical Audit Trail & Advisor Chat
    val scoreHistory: List<ScoreHistoryEntry> = emptyList(),
    val chatHistory: List<ChatMessage> = emptyList(),

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class IdeaRiskRewardAnalysis(
    val ideaId: String,
    val riskVsRewardScore: Int,
    val riskScore: Int,
    val rewardScore: Int,
    val riskVsRewardRatio: String,
    val potentialMarketImpactSummary: String,
    val keyDownsideRisks: List<String> = emptyList(),
    val keyRewardDrivers: List<String> = emptyList(),
    val strategicHypothesis: String = "",
    val suggestedMitigation: String = ""
)

data class BenchmarkEvaluationResult(
    val ideaId: String,
    val sector: String,
    val opportunityScore: Int,
    val feasibilityScore: Int,
    val timingScore: Int,
    val technicalFeasibility: Int = 75,
    val financialFeasibility: Int = 70,
    val regulatoryFeasibility: Int = 75,
    val percentile: Int,
    val feasibilityMedian: Int,
    val opportunityMedian: Int,
    val feasibilityDelta: Int,
    val opportunityDelta: Int,
    val summary: String,
    val differentiator: String,
    val downsideRisk: String,
    val recommendation: Recommendation,
    val recommendationReason: String,
    val whyNow: String
)

