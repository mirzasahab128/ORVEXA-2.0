package com.example.data.model

data class IdeaTemplate(
    val id: String,
    val name: String,
    val iconName: String,
    val defaultCategory: String,
    val defaultTag: String,
    val defaultPriority: String,
    val templatePrompt: String,
    val description: String
)

object IdeaTemplateLibrary {
    val templates: List<IdeaTemplate> = listOf(
        IdeaTemplate(
            id = "freeform",
            name = "Quick Capture",
            iconName = "Bolt",
            defaultCategory = "AI & Tech",
            defaultTag = "B2B",
            defaultPriority = "High",
            description = "Start with a clean slate for fast thoughts and spontaneous insights.",
            templatePrompt = ""
        ),
        IdeaTemplate(
            id = "swot",
            name = "SWOT Analysis",
            iconName = "GridView",
            defaultCategory = "Enterprise",
            defaultTag = "Infra",
            defaultPriority = "High",
            description = "Evaluate internal strengths and weaknesses alongside external opportunities and threats.",
            templatePrompt = """
[STRENGTHS]: Core proprietary technology, specialized domain expertise, low-cost operations.
[WEAKNESSES]: Heavy dependency on third-party APIs, unproven enterprise sales distribution.
[OPPORTUNITIES]: Expanding regulatory compliance mandates, large underserved mid-market.
[THREATS]: Incumbent platform bundling, sudden changes in partner platform policies.
            """.trimIndent()
        ),
        IdeaTemplate(
            id = "bmc",
            name = "Business Model Canvas",
            iconName = "BusinessCenter",
            defaultCategory = "B2B SaaS",
            defaultTag = "B2B",
            defaultPriority = "High",
            description = "Deconstruct the 9 critical building blocks of venture viability.",
            templatePrompt = """
[PROBLEM]: Critical operational friction causing measurable revenue loss or compliance risk.
[CUSTOMER SEGMENT]: Clear ICP (e.g. 10-50 provider orthopedic clinics, regional freight brokers).
[UNIQUE VALUE PROP]: 10x faster workflow turnaround, guaranteed ROI within 60 days.
[SOLUTION & MVP]: Lightweight cloud automation engine with one-click existing tool sync.
[REVENUE STREAMS]: B2B subscription (\$299-\$1,200/mo) plus usage-based processing tier.
[COST STRUCTURE]: Cloud infrastructure, LLM inference costs, customer onboarding support.
[UNFAIR ADVANTAGE / MOAT]: Proprietary data fine-tuning and regulatory compliance relationship.
            """.trimIndent()
        ),
        IdeaTemplate(
            id = "lean_validation",
            name = "Lean MVP Validation",
            iconName = "Science",
            defaultCategory = "AI & Tech",
            defaultTag = "AI-First",
            defaultPriority = "High",
            description = "Focus on the riskiest assumptions and fast concierge experiments.",
            templatePrompt = """
[CORE HYPOTHESIS]: Target buyers will pay \$500/month to automate manual documentation audits.
[RISKIEST ASSUMPTION]: Operations directors will grant API access to external web tools.
[CONCIERGE MVP TEST]: Run 5 manual audits for free in exchange for video case study and LOI.
[SUCCESS CRITERIA]: At least 3 out of 5 test clinics sign paid pilot commitments within 14 days.
[NEXT 7-DAY ACTION]: Schedule discovery calls with 10 target operators on LinkedIn.
            """.trimIndent()
        ),
        IdeaTemplate(
            id = "brainstorming",
            name = "Blue Ocean Strategy",
            iconName = "Lightbulb",
            defaultCategory = "Marketplace",
            defaultTag = "Marketplace",
            defaultPriority = "Medium",
            description = "Break away from saturated competition by creating uncontested market space.",
            templatePrompt = """
[ELIMINATE]: Long enterprise implementation timelines and opaque annual contract minimums.
[REDUCE]: Feature bloat that small operators rarely touch.
[RAISE]: Response time, mobile accessibility, and real-time transparent fee auditing.
[CREATE]: Novel instant settlement escrow rails and automated workflow compliance checks.
            """.trimIndent()
        ),
        IdeaTemplate(
            id = "devtools",
            name = "DevTools & Infra",
            iconName = "Terminal",
            defaultCategory = "DevTools",
            defaultTag = "DevTools",
            defaultPriority = "High",
            description = "Engineered for technical founders building developer platforms and infrastructure.",
            templatePrompt = """
[DEVELOPER PAIN]: Engineers spend 8+ hours/week wrestling with fragile integration pipelines.
[CLI & SDK WORKFLOW]: Single command installation: `npx tool init` with zero-config defaults.
[TECHNICAL MOAT]: Distributed execution engine with deterministic local replay.
[OPEN CORE MODEL]: Open-source local runtime with hosted enterprise telemetry and SOC2 security.
            """.trimIndent()
        )
    )
}
