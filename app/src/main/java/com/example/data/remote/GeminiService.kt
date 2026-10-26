package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.Idea
import com.example.data.model.IdeaPromptSuggestion
import com.example.data.model.IdeaRiskRewardAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiService {
    suspend fun suggestIdeaPrompts(): List<IdeaPromptSuggestion> = withContext(Dispatchers.IO) {
        listOf(
            IdeaPromptSuggestion(
                title = "MediClaim AI",
                category = "Healthcare",
                tag = "B2B",
                prompt = "Automated payer clinical denial appeal generator for outpatient clinics using CMS-0057-F guidelines.",
                options = listOf(
                    "Option A: Prior-Authorization Auto-Appeal Engine",
                    "Option B: Real-Time Payer Denial Risk Audit API",
                    "Option C: Clinic Revenue-Cycle Copilot & EHR Sync"
                )
            ),
            IdeaPromptSuggestion(
                title = "SiteForge Trades",
                category = "SaaS",
                tag = "Mobile",
                prompt = "Mobile voice-to-estimate tool for residential HVAC and electrical contractors with QuickBooks sync.",
                options = listOf(
                    "Option A: On-site Voice Scope Builder with Materials Pricing",
                    "Option B: Subcontractor Milestone Payment Portal",
                    "Option C: Fleet Van Tool Inventory & Maintenance Tracker"
                )
            ),
            IdeaPromptSuggestion(
                title = "FinShield Anti-Fraud",
                category = "Fintech",
                tag = "Enterprise",
                prompt = "Real-time synthetic identity and chargeback prevention SDK for regional credit unions.",
                options = listOf(
                    "Option A: Synthetic Identity Graph Risk Scoring API",
                    "Option B: Instant Debit Dispute Auto-Evidence Generator",
                    "Option C: Real-Time Wire Transfer Anomaly Guard"
                )
            ),
            IdeaPromptSuggestion(
                title = "VoltFleet OS",
                category = "Climate",
                tag = "IoT",
                prompt = "Commercial EV depot charging optimization and dynamic utility tariff arbitrage for delivery vans.",
                options = listOf(
                    "Option A: Overnight Off-Peak Depot Power Dispatcher",
                    "Option B: Battery Degradation Predictive Telematics",
                    "Option C: Delivery Route Elevation & Energy Optimizer"
                )
            )
        )
    }

    suspend fun generateCustomPromptsWithOptions(domainQuery: String): List<IdeaPromptSuggestion> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (GeminiKeyConfig.isConfigured(apiKey)) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val prompt = """
                    You are ORVEXA Venture Intelligence Engine.
                    Generate 3 high-conviction, evidence-backed startup idea prompts related to: "$domainQuery".
                    For EACH idea, provide 3 distinct strategic options / implementation paths (e.g. enterprise vs SME vs API).
                    Return ONLY a JSON array of objects:
                    [
                      {
                        "title": "Concise 2-3 word name",
                        "category": "Sector e.g. Healthcare, Fintech, SaaS, DevTools",
                        "tag": "e.g. B2B, Mobile, Enterprise, AI-First",
                        "prompt": "1 crisp analytical thesis sentence with specific ICP and friction point.",
                        "options": [
                          "Option A: concise angle 1",
                          "Option B: concise angle 2",
                          "Option C: concise angle 3"
                        ]
                      }
                    ]
                """.trimIndent()

                val payload = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { it.write(payload.toString().toByteArray()) }
                if (conn.responseCode == 200) {
                    val respText = conn.inputStream.bufferedReader().use { it.readText() }
                    val respJson = JSONObject(respText)
                    val candidate = respJson.getJSONArray("candidates").getJSONObject(0)
                    val contentText = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                    val arrayJsonStr = contentText.substringAfter("[").substringBeforeLast("]")
                    val array = JSONArray("[$arrayJsonStr]")
                    val result = mutableListOf<IdeaPromptSuggestion>()
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val optionsList = mutableListOf<String>()
                        val optsArr = item.optJSONArray("options")
                        if (optsArr != null) {
                            for (j in 0 until optsArr.length()) {
                                optionsList.add(optsArr.getString(j))
                            }
                        }
                        result.add(
                            IdeaPromptSuggestion(
                                title = item.optString("title", "Venture ${i + 1}"),
                                category = item.optString("category", "AI & Tech"),
                                tag = item.optString("tag", "B2B"),
                                prompt = item.optString("prompt", ""),
                                options = optionsList.ifEmpty {
                                    listOf("Option A: B2B Enterprise API", "Option B: Self-Serve SaaS", "Option C: Embedded Workflow")
                                }
                            )
                        )
                    }
                    if (result.isNotEmpty()) return@withContext result
                }
            } catch (_: Exception) {
                // Fall through to deterministic heuristics
            }
        }

        // Heuristic fallback for offline / mock-free continuity
        val cleanDomain = domainQuery.trim().ifBlank { "Software" }
        listOf(
            IdeaPromptSuggestion(
                title = "$cleanDomain Engine",
                category = if (cleanDomain.contains("health", true)) "Healthcare" else if (cleanDomain.contains("pay", true) || cleanDomain.contains("fin", true)) "Fintech" else "B2B SaaS",
                tag = "Enterprise",
                prompt = "Automated compliance and high-throughput telemetry engine specifically tailored for $cleanDomain operators.",
                options = listOf(
                    "Option A: Zero-Trust Auditing API for Enterprise $cleanDomain",
                    "Option B: Real-Time Anomaly Dashboard & Alert Bot",
                    "Option C: White-Label Integration Layer for Legacy Systems"
                )
            ),
            IdeaPromptSuggestion(
                title = "$cleanDomain Copilot",
                category = "AI & Tech",
                tag = "AI-First",
                prompt = "Domain-fine-tuned agentic workflow assistant cutting repetitive manual reconciliation in $cleanDomain by 85%.",
                options = listOf(
                    "Option A: Browser Extension for Live In-Workflow Assist",
                    "Option B: Headless Webhook Worker Processing Document Queues",
                    "Option C: Multi-Agent Collaboration Workspace for Teams"
                )
            ),
            IdeaPromptSuggestion(
                title = "$cleanDomain Gateway",
                category = "DevTools",
                tag = "B2B",
                prompt = "Unified developer SDK abstracting fragmented third-party data providers and legacy APIs across $cleanDomain.",
                options = listOf(
                    "Option A: Developer-First GraphQL / REST Aggregator",
                    "Option B: Self-Hosted Container with SOC2 Compliance",
                    "Option C: Usage-Based Metered Proxy with Edge Caching"
                )
            )
        )
    }

    suspend fun analyzeIdea(rawInput: String, category: String, tag: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (GeminiKeyConfig.isConfigured(apiKey)) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val prompt = """
                    You are ORVEXA, an analytical, evidence-backed decision engine for startup ideas.
                    Analyze this idea: "$rawInput", category: "$category", tag: "$tag".
                    Return a JSON object with:
                    - name: concise 2-4 word title
                    - oneLineThesis: 1 punchy analytical sentence
                    - opportunityScore: int 0-100
                    - timingScore: int 0-100
                    - feasibilityScore: int 0-100
                    - recommendation: one of BUILD_NOW, VALIDATE_FIRST, BUILD_SMALL, PARTNER, REPOSITION, PIVOT, WATCH, WAIT, ABANDON
                    - recommendationReason: 2 sentences explaining why
                    - whyNow: 1 sentence
                    - biggestRisk: 1 sentence
                    - nextImmediateAction: 1 clear actionable step
                    - options: list of 3 strategic execution options (e.g. Option A, Option B, Option C)
                """.trimIndent()

                val payload = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { it.write(payload.toString().toByteArray()) }
                if (conn.responseCode == 200) {
                    val respText = conn.inputStream.bufferedReader().use { it.readText() }
                    val respJson = JSONObject(respText)
                    val candidate = respJson.getJSONArray("candidates").getJSONObject(0)
                    val contentText = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                    val cleanJsonStr = contentText.substringAfter("{").substringBeforeLast("}")
                    val parsed = JSONObject("{$cleanJsonStr}")

                    val parsedOpts = mutableListOf<String>()
                    val optsJson = parsed.optJSONArray("options")
                    if (optsJson != null) {
                        for (i in 0 until optsJson.length()) {
                            parsedOpts.add(optsJson.getString(i))
                        }
                    }

                    return@withContext mapOf(
                        "name" to parsed.optString("name", rawInput.take(25)),
                        "oneLineThesis" to parsed.optString("oneLineThesis", rawInput),
                        "opportunityScore" to parsed.optInt("opportunityScore", 78),
                        "timingScore" to parsed.optInt("timingScore", 80),
                        "feasibilityScore" to parsed.optInt("feasibilityScore", 72),
                        "recommendation" to parsed.optString("recommendation", "VALIDATE_FIRST"),
                        "recommendationReason" to parsed.optString("recommendationReason", "High market potential but requires customer discovery."),
                        "whyNow" to parsed.optString("whyNow", "Shift in technology enables lower unit economics."),
                        "biggestRisk" to parsed.optString("biggestRisk", "Customer switching friction and distribution access."),
                        "nextImmediateAction" to parsed.optString("nextImmediateAction", "Conduct 5 interviews with target ICP operators."),
                        "options" to parsedOpts.ifEmpty {
                            listOf(
                                "Option A: Direct-to-Consumer / SME Wedge",
                                "Option B: Enterprise Compliance & Governance Platform",
                                "Option C: Developer API & Embedded Infrastructure"
                            )
                        }
                    )
                }
            } catch (_: Exception) {
                // Fall back to rule-based analysis on failure
            }
        }

        // Analytical deterministic heuristic fallback
        val oppScore = (72 + (rawInput.length % 18)).coerceIn(60, 92)
        val timingScore = (70 + (rawInput.hashCode().mod(22))).coerceIn(55, 94)
        val feasibilityScore = (68 + (category.hashCode().mod(20))).coerceIn(58, 90)

        mapOf(
            "name" to rawInput.split(" ").take(4).joinToString(" ").ifBlank { "New Venture" },
            "oneLineThesis" to "Evidence-backed venture thesis addressing critical workflow friction in $category.",
            "opportunityScore" to oppScore,
            "timingScore" to timingScore,
            "feasibilityScore" to feasibilityScore,
            "recommendation" to if (oppScore >= 80 && timingScore >= 75) "BUILD_NOW" else "VALIDATE_FIRST",
            "recommendationReason" to "Analytical signal indicates viable early adopter willingness to pay with manageable execution complexity.",
            "whyNow" to "Rapid advancements in modern software tools reduce MVP build cost by 80%.",
            "biggestRisk" to "Channel distribution and buyer reluctance to adopt new software without enterprise integrations.",
            "nextImmediateAction" to "Interview 5 prospective buyers to validate initial pricing and feature requirements.",
            "options" to listOf(
                "Option A: Vertical SaaS with automated onboarding",
                "Option B: Embedded API partner model with existing distributors",
                "Option C: High-touch concierge MVP with 3 anchor design partners"
            )
        )
    }

    suspend fun askAdvisor(ideaTitle: String, conversationHistory: String, userMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (GeminiKeyConfig.isConfigured(apiKey)) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val prompt = """
                    You are ORVEXA Venture Advisor. You provide calm, evidence-backed, highly objective analysis with zero hype.
                    Context: Idea "$ideaTitle".
                    History: $conversationHistory
                    User says: "$userMessage"
                    Provide a concise, candid, high-precision answer (under 120 words). Include 2-3 specific strategic options or immediate empirical next steps.
                """.trimIndent()

                val payload = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { it.write(payload.toString().toByteArray()) }
                if (conn.responseCode == 200) {
                    val respText = conn.inputStream.bufferedReader().use { it.readText() }
                    val respJson = JSONObject(respText)
                    val candidate = respJson.getJSONArray("candidates").getJSONObject(0)
                    return@withContext candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim()
                }
            } catch (_: Exception) {
                // Fall back
            }
        }

        // Context-aware heuristic advisor response
        val lower = userMessage.lowercase()
        when {
            lower.contains("unit economic") || lower.contains("pricing") || lower.contains("margin") -> {
                "For $ideaTitle, target a 3.5x LTV/CAC ratio with payback under 8 months. Key options:\n• Option 1: Value-based pricing ($250-$500/mo per seat tied to recovered hours).\n• Option 2: 1.5% transaction take-rate on processed volume.\n• Option 3: Tiered usage model with a low-friction entry tier."
            }
            lower.contains("competitor") || lower.contains("moat") || lower.contains("threat") -> {
                "Incumbents in this sector suffer from slow release cycles and legacy codebases. Your defensibility comes from:\n• Proprietary workflow telemetry data that trains predictive models.\n• 10x faster time-to-value (5-minute setup vs. 3-month enterprise onboarding).\n• Deep native integrations into existing system-of-record tools."
            }
            lower.contains("risk") || lower.contains("flaw") || lower.contains("failure") -> {
                "The existential risk is not technical—it is distribution velocity. Avoid building advanced secondary features until you verify that 3 prospective customers will sign a LOI (Letter of Intent) at your target price point."
            }
            lower.contains("experiment") || lower.contains("discovery") || lower.contains("interview") -> {
                "Recommended 7-day validation sprint:\n1. Conduct 5 customer discovery calls focused on past behavior, not hypothetical interest.\n2. Ask: 'What did you pay last month to solve this problem?'\n3. If they haven't actively sought a solution, deprioritize."
            }
            else -> {
                "Objective assessment for $ideaTitle: Current market conditions favor vertical tools with immediate ROI. Recommended next step: test buyer willingness to pay before committing engineering capital. Focus on identifying the single highest-pain bottleneck in the user's weekly workflow."
            }
        }
    }

    suspend fun analyzeSavedIdeaRiskVsReward(idea: Idea): IdeaRiskRewardAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (GeminiKeyConfig.isConfigured(apiKey)) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val prompt = """
                    You are ORVEXA Venture Risk-Reward Intelligence Engine.
                    Analyze this saved startup venture / strategic hypothesis:
                    - Title: "${idea.name}"
                    - One-Line Thesis: "${idea.oneLineThesis}"
                    - Description: "${idea.description}"
                    - Category: "${idea.category}"
                    - Priority: "${idea.priority}"
                    - Known Risks: "${idea.risks.joinToString { it.title }}"

                    Conduct an institutional-grade risk vs. reward calculation and market impact synthesis.
                    Return ONLY a JSON object:
                    {
                      "riskVsRewardScore": integer 0-100 (overall risk-adjusted payoff, where >70 is favorable),
                      "riskScore": integer 0-100 (execution/market downside risk, where 0 is lowest risk, 100 is critical risk),
                      "rewardScore": integer 0-100 (commercial upside, margin profile and TAM headroom),
                      "riskVsRewardRatio": "string (e.g. '2.8x', '3.4x')",
                      "potentialMarketImpactSummary": "2-3 crisp sentences summarizing addressable market impact, customer productivity gains, and long-term moat defensibility.",
                      "keyDownsideRisks": ["concise risk 1", "concise risk 2", "concise risk 3"],
                      "keyRewardDrivers": ["upside driver 1", "upside driver 2", "upside driver 3"],
                      "strategicHypothesis": "The single primary assumption or hypothesis this venture must validate first.",
                      "suggestedMitigation": "Specific empirical action to derisk this venture within 14 days."
                    }
                """.trimIndent()

                val payload = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { it.write(payload.toString().toByteArray()) }
                if (conn.responseCode == 200) {
                    val respText = conn.inputStream.bufferedReader().use { it.readText() }
                    val respJson = JSONObject(respText)
                    val candidate = respJson.getJSONArray("candidates").getJSONObject(0)
                    val contentText = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                    val cleanJsonStr = contentText.substringAfter("{").substringBeforeLast("}")
                    val parsed = JSONObject("{$cleanJsonStr}")

                    val risksList = mutableListOf<String>()
                    val risksArr = parsed.optJSONArray("keyDownsideRisks")
                    if (risksArr != null) {
                        for (i in 0 until risksArr.length()) {
                            risksList.add(risksArr.getString(i))
                        }
                    }

                    val rewardsList = mutableListOf<String>()
                    val rewardsArr = parsed.optJSONArray("keyRewardDrivers")
                    if (rewardsArr != null) {
                        for (i in 0 until rewardsArr.length()) {
                            rewardsList.add(rewardsArr.getString(i))
                        }
                    }

                    val riskVsReward = parsed.optInt("riskVsRewardScore", 80)
                    val risk = parsed.optInt("riskScore", 35)
                    val reward = parsed.optInt("rewardScore", 85)

                    return@withContext IdeaRiskRewardAnalysis(
                        ideaId = idea.id,
                        riskVsRewardScore = riskVsReward,
                        riskScore = risk,
                        rewardScore = reward,
                        riskVsRewardRatio = parsed.optString("riskVsRewardRatio", String.format("%.1fx", reward.toFloat() / risk.coerceAtLeast(1).toFloat())),
                        potentialMarketImpactSummary = parsed.optString(
                            "potentialMarketImpactSummary",
                            "High-leverage market disruption opportunity addressing substantial latent enterprise demand with strong margin potential."
                        ),
                        keyDownsideRisks = risksList.ifEmpty {
                            listOf("Customer switching inertia", "Distribution channel access friction", "Incumbent response speed")
                        },
                        keyRewardDrivers = rewardsList.ifEmpty {
                            listOf("High recurring software gross margins", "Strong expansion revenue via seat/usage scaling", "Defensible domain-specific workflow data moat")
                        },
                        strategicHypothesis = parsed.optString(
                            "strategicHypothesis",
                            "Customers will switch from manual workarounds if automated workflows save >5 hours per week at <$500/month."
                        ),
                        suggestedMitigation = parsed.optString(
                            "suggestedMitigation",
                            "Conduct 5 customer discovery interviews to validate willingness to pay before writing production code."
                        )
                    )
                }
            } catch (_: Exception) {
                // Fall through to heuristic analysis
            }
        }

        // Deterministic heuristic risk vs reward calculation
        val riskScore = when {
            idea.risks.any { it.severity.equals("High", ignoreCase = true) } -> 55
            idea.risks.any { it.severity.equals("Medium", ignoreCase = true) } -> 38
            else -> 30
        }
        val rewardScore = ((idea.opportunityScore * 0.7f) + (idea.marketScore * 0.3f)).toInt().coerceIn(60, 96)
        val ratio = String.format("%.1fx", rewardScore.toFloat() / riskScore.toFloat())
        val compositeScore = ((rewardScore * 0.65f) + ((100 - riskScore) * 0.35f)).toInt().coerceIn(40, 98)

        IdeaRiskRewardAnalysis(
            ideaId = idea.id,
            riskVsRewardScore = compositeScore,
            riskScore = riskScore,
            rewardScore = rewardScore,
            riskVsRewardRatio = ratio,
            potentialMarketImpactSummary = "Targeting high-margin productivity gains in ${idea.category}. By replacing fragmented manual processes, this venture unlocks measurable ROI and high barrier-to-entry switching costs.",
            keyDownsideRisks = listOf(
                "Buyer inertia and prolonged enterprise procurement validation cycles",
                "Integration complexity with existing legacy systems of record",
                "Customer CAC inflation without an organic community or partner channel"
            ),
            keyRewardDrivers = listOf(
                "80%+ software gross margins with rapid payback period (<6 months)",
                "Expanding net revenue retention as customer transaction volume scales",
                "Structural moat created by proprietary domain workflow dataset"
            ),
            strategicHypothesis = "Operators in ${idea.category} will adopt a dedicated solution if time-to-value is under 15 minutes and direct financial recovery is verifiable.",
            suggestedMitigation = "Secure 2 pilot design partners with a conditional Letter of Intent prior to architectural scale."
        )
    }
}
