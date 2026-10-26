package com.example.data.engine

import com.example.data.model.*

object SeedData {
    val sampleIdeas: List<Idea> = listOf(
        Idea(
            id = "mediclaim-ai",
            name = "MediClaim AI",
            oneLineThesis = "Generative denial appeal and payer policy retrieval engine for outpatient surgery clinics.",
            description = "AI-powered revenue cycle automation that cross-references denial codes with updated CMS and commercial payer clinical coverage guidelines, drafting overturn appeals in minutes.",
            category = "Healthcare",
            tag = "B2B",
            categories = listOf("Healthcare", "AI", "B2B", "SaaS"),
            priority = "High",
            status = IdeaStatus.BUILDING,
            isSample = true,
            opportunityScore = 86,
            marketScore = 90,
            feasibilityScore = 78,
            timingScore = 91,
            personalFitScore = 85,
            impactScore = 88,
            healthScore = 86,
            healthTrend = "Improving",
            confidence = "High",
            recommendation = Recommendation.BUILD_NOW,
            recommendationReason = "Healthcare API mandates (CMS-0057-F) and record clinical staff turnover create an exceptional 12-month window for automated appeal submission.",
            whyNow = "Federal CMS-0057-F interoperability enforcement began this quarter; commercial denial rates jumped from 11% to 15.3% nationwide.",
            whyNot = "Slow enterprise EHR sales cycles; HIPAA/SOC2 compliance overhead.",
            whatWouldChangeMind = "Major payers banning third-party API appeal submissions or introducing native automated denial dispute resolution without intermediaries.",
            biggestOpportunity = "$4.2B annual revenue recovery market for mid-market surgical and orthopedic clinics currently writing off claims under $1,000.",
            biggestRisk = "Payers altering prior authorization rules to reject algorithmic appeals.",
            biggestUnknown = "Whether billing managers will allow automated one-click appeal submission without line-by-line manual audit.",
            nextImmediateAction = "Review initial 18 appeal overturn outcomes from BlueCross pilot.",
            nextReviewDate = "In 7 days",
            problemStatement = "Mid-market clinics lose $120k-$340k annually in rejected claims because billers take 45+ minutes to research and contest complex clinical guidelines.",
            problemSeverity = "Critical",
            problemCostOfInaction = "$18,000/month average cash bleed per 5-doctor orthopedic practice in non-appealed denials.",
            primaryUser = "Medical Billing Specialist & Certified Coder",
            economicBuyer = "Practice Administrator / CFO",
            decisionMaker = "Managing Physician Partner",
            customerObjections = listOf(
                "Is it compliant with HIPAA Business Associate Agreements?",
                "Does it integrate directly with AthenaHealth, eClinicalWorks, and ModMed?",
                "Will our billers spend more time reviewing AI errors than writing letters?"
            ),
            timingStatus = TimingStatus.RIGHT_TIME,
            opportunityWindow = OpportunityWindow.NOW,
            timingSignalsSummary = "CMS regulatory mandate enforcement and skyrocketing claim denial rates make this the optimal timing window.",
            waitUntilTriggers = listOf("Wait if state insurance commissioner delays Prior Auth portal mandate."),
            marketSaturation = "Low in mid-market; high in legacy hospital enterprise clearinghouses.",
            competitors = listOf(
                CompetitorItem(
                    name = "Waystar",
                    type = "Direct",
                    positioning = "Legacy Enterprise Clearinghouse",
                    pricing = "$1,500/mo + transaction fees",
                    strengths = "Deep EHR market share, trusted brand",
                    weaknesses = "Rigid rules engine, minimal generative explanation",
                    threatLevel = "Incumbent giant"
                ),
                CompetitorItem(
                    name = "Cedar Health",
                    type = "Indirect",
                    positioning = "Patient Billing & Engagement",
                    pricing = "Enterprise % fee",
                    strengths = "Patient payment collection UX",
                    weaknesses = "Doesn't focus on back-office payer denials",
                    threatLevel = "Adjacent market"
                )
            ),
            marketGaps = listOf(
                MarketGap("Payer guideline diff tracking", "Payers silently alter prior authorization policies 12x/year without centralized RSS.", "High", "High", "Critical"),
                MarketGap("Mid-market clinic affordability", "Enterprise solutions require $50k annual minimums and 12-month IT integration projects.", "Medium", "High", "High")
            ),
            epistemicClaims = listOf(
                EpistemicClaim("Average US medical claim denial rate rose to 15.3% in 2025.", EpistemicType.FACT, "KFF Healthcare Analysis 2025", "High"),
                EpistemicClaim("Clinics can recover 60%+ of denied claims if appealed within 14 days.", EpistemicType.USER_INPUT, "Interviews with 5 Billing Directors", "High"),
                EpistemicClaim("LLM-assisted appeals will maintain at least 75% approval rate.", EpistemicType.ASSUMPTION, "Preliminary prototype benchmark", "Medium")
            ),
            requirements = listOf(
                RequirementItem("FHIR API & EDI 835/837 claim parser", "Technical", "MUST", "High", "Critical"),
                RequirementItem("Payer policy retrieval engine", "Functional", "MUST", "Medium", "High"),
                RequirementItem("HIPAA compliant secure cloud enclave", "Legal", "MUST", "High", "Mandatory")
            ),
            risks = listOf(
                RiskItem("Liability for erroneous medical coding advice", "Legal", "High", "Low", "Explicit disclaimers and human-in-the-loop signoff"),
                RiskItem("Integration friction with legacy on-prem EHRs", "Technical", "High", "High", "Focus first on web-native EHRs like AthenaHealth")
            ),
            technicalFeasibility = 80,
            financialFeasibility = 85,
            regulatoryFeasibility = 70,
            aiBuildability = "High (LLM parsing + RAG over payer manuals)",
            mvpObjective = "Enable a billing specialist to drag-and-drop a denial PDF and generate an overturn appeal letter in <60 seconds.",
            mvpCoreWorkflow = "Upload 835 denial -> Auto-extract code -> Fetch policy clause -> Generate appeal letter -> Review & export PDF.",
            mvpEssentialFeatures = "PDF parsing, EDI-835 reader, payer policy RAG, PDF export",
            mvpExcludedFeatures = listOf("Live telephone IVR bots", "Direct clearinghouse auto-transmit", "Patient portal"),
            mvpEstimatedCost = listOf("$4,500 cloud infra & initial compliance review"),
            mvpEstimatedEffort = "3-4 Weeks",
            experiments = listOf(
                ValidationExperiment("exp-1", "Billing directors will upload scrubbed denial files to web tool", "Concierge MVP", "3 out of 5 directors upload data within 48h", "VALIDATED", "Tested with 4 clinics; all were eager to see results."),
                ValidationExperiment("exp-2", "Generated appeals result in actual payer reimbursement", "Paid Pilot", "At least $10,000 in overturned denials in 30 days", "IN_PROGRESS", "Submissions sent for 18 denials last Tuesday.")
            ),
            actionItems = listOf(
                ActionItem("act-1", "Review initial 18 appeal outcomes from BlueCross BlueShield", "7 Days", "P0", "Low", false),
                ActionItem("act-2", "Finalize AthenaHealth Marketplace Developer App submission", "30 Days", "P1", "Medium", false)
            ),
            reminders = listOf(
                SmartReminder("rem-1", "Review CMS-0057-F quarterly enforcement guidelines", "Regulation", "Quarterly check", "Nov 1, 2026", false),
                SmartReminder("rem-2", "Audit appeal success rates across Anthem vs Aetna", "Market", "Monthly check", "Oct 25, 2026", false)
            ),
            scoreHistory = listOf(
                ScoreHistoryEntry(System.currentTimeMillis() - 2592000000L, "Sep 7, 2026", 72, 75, 70, "Initial concept ingestion and problem definition."),
                ScoreHistoryEntry(System.currentTimeMillis() - 1209600000L, "Sep 23, 2026", 81, 84, 75, "Validated clinical denial pain in 5 customer interviews."),
                ScoreHistoryEntry(System.currentTimeMillis(), "Oct 7, 2026", 86, 91, 78, "Successful pilot launch and CMS mandate acceleration.")
            ),
            createdAt = 1723723200000L, // August 2026
            updatedAt = 1723723200000L
        ),
        Idea(
            id = "siteforge-trades",
            name = "SiteForge Trades",
            oneLineThesis = "Instant quote-to-job mobile ERP for independent HVAC, plumbing, and electrical contractors.",
            description = "Lightweight field operations platform that converts customer voice memos into branded estimates, contracts, and supplier material orders.",
            category = "SaaS",
            tag = "Mobile",
            categories = listOf("SaaS", "Mobile", "Marketplace", "Workflow"),
            priority = "Medium",
            status = IdeaStatus.VALIDATING,
            isSample = true,
            opportunityScore = 64,
            marketScore = 72,
            feasibilityScore = 80,
            timingScore = 58,
            personalFitScore = 70,
            impactScore = 65,
            healthScore = 62,
            healthTrend = "Declining",
            confidence = "Medium",
            recommendation = Recommendation.REPOSITION,
            recommendationReason = "High saturation in generic contractor estimating tools; recommend pivoting to specialized geothermal or solar heat-pump contractors.",
            whyNow = "Mobile-first trades generation replacing retiring legacy business owners.",
            whyNot = "Extreme market noise from 40+ generic AI builder tools and ServiceTitan expansion into small shops.",
            whatWouldChangeMind = "Zero-churn traction in specialized trade niches with high ticket sizes.",
            biggestOpportunity = "Automating supplier wholesale inventory lookups during customer estimate creation.",
            biggestRisk = "Customer acquisition costs exceeding contractor lifetime value due to Google Ads bidding wars.",
            biggestUnknown = "Whether technicians will adopt mobile voice workflows while on job sites.",
            nextImmediateAction = "Narrow ICP to commercial refrigeration and heat-pump contractors only.",
            nextReviewDate = "In 10 days",
            problemStatement = "Solo trades operators spend 2-3 unpaid evening hours drafting estimates on clipboards or spreadsheets.",
            problemSeverity = "Moderate",
            problemCostOfInaction = "$2,500/month in delayed job bookings and unbilled material overruns.",
            primaryUser = "Independent Trades Contractor",
            economicBuyer = "Owner / Operator",
            decisionMaker = "Owner / Operator",
            customerObjections = listOf(
                "I already use QuickBooks and pen & paper.",
                "I don't have time to learn complicated software."
            ),
            timingStatus = TimingStatus.CYCLICAL,
            opportunityWindow = OpportunityWindow.OPENING,
            timingSignalsSummary = "High competition requires vertical specialization.",
            waitUntilTriggers = listOf("Monitor Home Depot / Ferguson API partnership availability."),
            marketSaturation = "High in generic residential trades; moderate in specialized commercial.",
            competitors = listOf(
                CompetitorItem("Jobber", "Direct", "All-in-one Home Service Software", "$49-$299/mo", "Comprehensive features, strong brand", "Bloated for solo operators", "Category leader"),
                CompetitorItem("ServiceTitan", "Indirect", "Enterprise Trade ERP", "$300+/technician", "Enterprise workflows, dispatching", "Far too expensive for 1-3 truck shops", "Enterprise monopoly")
            ),
            marketGaps = listOf(
                MarketGap("Voice memo to line-item quote", "Technicians want to speak into their phone in the driveway and email a contract.", "Medium", "High", "High")
            ),
            epistemicClaims = listOf(
                EpistemicClaim("Over 40 venture-backed trade SaaS tools launched in last 24 months.", EpistemicType.FACT, "Crunchbase TradeTech report", "High"),
                EpistemicClaim("Solo contractors lose 35% of bids due to slow turnaround (>24h).", EpistemicType.USER_INPUT, "Interviews with 12 contractors", "High")
            ),
            requirements = listOf(
                RequirementItem("Whisper voice memo to structured estimate parser", "Functional", "MUST", "Medium", "High"),
                RequirementItem("QuickBooks Online two-way invoice sync", "Technical", "MUST", "High", "Critical")
            ),
            risks = listOf(
                RiskItem("High churn during seasonal winter slowdown", "Market", "High", "High", "Annual upfront discounting and off-season maintenance campaign features")
            ),
            technicalFeasibility = 85,
            financialFeasibility = 70,
            regulatoryFeasibility = 90,
            aiBuildability = "Very High (Speech-to-text + JSON formatting)",
            mvpObjective = "Record 30-second audio note and send signed customer contract link via SMS in <3 minutes.",
            mvpCoreWorkflow = "Voice Note -> Structured Estimate -> SMS Link -> Digital Signature -> Deposit Payment.",
            mvpEssentialFeatures = "Voice input, material catalog, signature capture, Stripe deposits",
            mvpExcludedFeatures = listOf("GPS fleet tracking", "Inventory warehouse barcode scanning"),
            mvpEstimatedCost = listOf("$2,000 prototype dev and mobile wrapper"),
            mvpEstimatedEffort = "2-3 Weeks",
            experiments = listOf(
                ValidationExperiment("exp-1", "Contractors will text voice notes to an automated bot", "Smoke Test", "10 contractors test with at least 3 real client jobs", "IN_PROGRESS", "6 contractors onboarding this week.")
            ),
            actionItems = listOf(
                ActionItem("act-1", "Interview 5 specialized heat-pump installation contractors", "7 Days", "P0", "Medium", false)
            ),
            reminders = listOf(
                SmartReminder("rem-1", "Track HVAC seasonal spring ramp-up trends", "Market", "Seasonal review", "March 15, 2027", false)
            ),
            scoreHistory = listOf(
                ScoreHistoryEntry(System.currentTimeMillis() - 1500000000L, "Aug 20, 2026", 74, 70, 80, "Initial residential trades concept."),
                ScoreHistoryEntry(System.currentTimeMillis(), "Oct 6, 2026", 64, 58, 80, "Repositioning due to competitive saturation.")
            ),
            createdAt = 1727265600000L, // September 2026
            updatedAt = 1727265600000L
        ),
        Idea(
            id = "tradetrust-escrow",
            name = "TradeTrust Escrow",
            oneLineThesis = "Milestone-locked contractor bidding with automated video escrow releases.",
            description = "Smart escrow platform for commercial construction and home renovations protecting both homeowners and trades.",
            category = "Marketplace",
            tag = "Fintech",
            categories = listOf("Fintech", "Marketplace", "Consumer", "Mobile"),
            priority = "Medium",
            status = IdeaStatus.VALIDATING,
            isSample = true,
            opportunityScore = 74,
            marketScore = 78,
            feasibilityScore = 65,
            timingScore = 72,
            personalFitScore = 68,
            impactScore = 70,
            healthScore = 71,
            healthTrend = "Stable",
            confidence = "Medium",
            recommendation = Recommendation.VALIDATE_FIRST,
            recommendationReason = "High escrow fraud and contractor non-performance make trust mechanisms valuable, but state escrow licensing is complex.",
            whyNow = "Soaring home improvement litigation and ubiquitous smartphone video verification.",
            whyNot = "Fintech money-transmitter licensing and banking partner dependencies.",
            whatWouldChangeMind = "Banking partner providing turn-key escrow-as-a-service white-label rails.",
            biggestOpportunity = "Capturing 1.5% transaction spread on $15B in annual mid-market general contractor draws.",
            biggestRisk = "Disputed workmanship claims tying up customer funds in protracted arbitration.",
            biggestUnknown = "Willingness of general contractors to accept milestone fund lockups.",
            nextImmediateAction = "Run concierge test with 20 kitchen remodeling contractors.",
            nextReviewDate = "In 14 days",
            problemStatement = "Homeowners fear contractor ghosting; contractors fear non-payment after material outlay.",
            problemSeverity = "High",
            problemCostOfInaction = "Average $8,500 lost per disputed construction project.",
            primaryUser = "Independent General Contractor & Homeowner",
            economicBuyer = "Homeowner / Project Sponsor",
            decisionMaker = "Homeowner",
            customerObjections = listOf(
                "Is my deposit FDIC insured while locked in milestone escrow?",
                "Who decides if the milestone completion video is satisfactory?"
            ),
            timingStatus = TimingStatus.RIGHT_TIME,
            opportunityWindow = OpportunityWindow.OPENING,
            timingSignalsSummary = "Consumer distrust of contractors is at peak levels while fintech compliance APIs are maturing.",
            waitUntilTriggers = listOf("Partner with BaaS platform like Column or Treasury Prime."),
            marketSaturation = "Competitive",
            competitors = listOf(
                CompetitorItem("Escrow.com", "Direct", "Legacy Internet Escrow", "3.25% fee", "Established brand", "Horrible mobile UX, manual verification", "Incumbent")
            ),
            marketGaps = listOf(
                MarketGap("Mobile video milestone signoff", "Contractor uploads 15-second video proof, triggering instant funds release.", "High", "High", "High")
            ),
            epistemicClaims = listOf(
                EpistemicClaim("Over 38% of home renovation projects experience payment disputes.", EpistemicType.FACT, "NAHB Study 2024", "High"),
                EpistemicClaim("Contractors accept 2% platform fee in exchange for guaranteed solvency.", EpistemicType.USER_INPUT, "Interviews with 15 tradesmen", "Medium")
            ),
            technicalFeasibility = 70,
            financialFeasibility = 60,
            regulatoryFeasibility = 65,
            aiBuildability = "Moderate (Computer vision verification + payment rails)",
            mvpObjective = "Enable a contractor to create a 3-milestone escrow link and receive client funding in 5 minutes.",
            mvpCoreWorkflow = "Create Job -> Set Milestones -> Client Funds Escrow -> Video Check-in -> Payout Released.",
            mvpEssentialFeatures = "Milestone builder, video upload, Stripe Connect payout",
            mvpExcludedFeatures = listOf("Mechanics lien filing automation", "Surety bond underwriting"),
            mvpEstimatedCost = listOf("$6,000 regulatory legal review and payment setup"),
            mvpEstimatedEffort = "4 Weeks",
            experiments = listOf(
                ValidationExperiment("exp-tt1", "Contractors will quote jobs through TradeTrust link", "Smoke Test", "5 contractors send live quotes", "IN_PROGRESS", "Testing with 3 remodeling contractors.")
            ),
            actionItems = listOf(
                ActionItem("act-tt1", "Run concierge test with 20 kitchen remodeling contractors", "14 Days", "P0", "Medium", false)
            ),
            reminders = listOf(
                SmartReminder("rem-tt1", "Review state money-transmitter exemption thresholds", "Regulation", "Quarterly check", "Nov 15, 2026", false)
            ),
            scoreHistory = listOf(
                ScoreHistoryEntry(1724846400000L, "Aug 28, 2026", 74, 72, 65, "Initial concept ingestion and escrow problem definition.")
            ),
            createdAt = 1724846400000L, // August 2026
            updatedAt = 1724846400000L
        ),
        Idea(
            id = "remitclear",
            name = "RemitClear",
            oneLineThesis = "Real-time FX margin auditing and hidden fee recovery for cross-border transactions.",
            description = "Independent currency margin verification engine alerting migrant workers and digital nomads to predatory transfer markups.",
            category = "Fintech",
            tag = "Remittance",
            categories = listOf("Remittance", "Fintech", "Consumer", "Mobile"),
            priority = "High",
            status = IdeaStatus.BUILDING,
            isSample = true,
            opportunityScore = 69,
            marketScore = 76,
            feasibilityScore = 84,
            timingScore = 68,
            personalFitScore = 74,
            impactScore = 78,
            healthScore = 74,
            healthTrend = "Stable",
            confidence = "High",
            recommendation = Recommendation.BUILD_SMALL,
            recommendationReason = "High technical feasibility with public interbank rates; start as lightweight mobile auditor before building brokerage rails.",
            whyNow = "CFPB increased scrutiny on undisclosed cross-border remittance spreads.",
            whyNot = "Remittance providers actively mask real-time spreads behind dynamic fee calculators.",
            whatWouldChangeMind = "New CFPB transparency rule forcing instant upfront spread disclosure.",
            biggestOpportunity = "Affiliate routing fees from Wise and OFX for referring high-volume remitters.",
            biggestRisk = "Transfer providers blocking rate-scraping bots.",
            biggestUnknown = "Whether consumers care enough about 1.2% spreads to switch apps.",
            nextImmediateAction = "Ship iOS & Android currency audit calculator.",
            nextReviewDate = "In 7 days",
            problemStatement = "Cross-border workers lose $30B annually in undisclosed FX rate markups hidden behind 'zero commission' marketing.",
            problemSeverity = "Moderate",
            problemCostOfInaction = "$450/year in phantom fees per remitting household.",
            primaryUser = "Cross-border Contractor & Expat",
            economicBuyer = "Individual Remitter",
            decisionMaker = "Individual Remitter",
            customerObjections = listOf(
                "My current provider says transfers are free.",
                "How do you know the real mid-market interbank rate?"
            ),
            timingStatus = TimingStatus.RIGHT_TIME,
            opportunityWindow = OpportunityWindow.NOW,
            timingSignalsSummary = "High inflation and regulatory attention on deceptive remittance fees create immediate user interest.",
            waitUntilTriggers = emptyList(),
            marketSaturation = "Competitive",
            competitors = listOf(
                CompetitorItem("Monito", "Direct", "Remittance Comparison Site", "Affiliate commission", "Strong SEO", "Web only, slow rate updates", "Market leader")
            ),
            marketGaps = listOf(
                MarketGap("Live screenshot receipt audit", "User screenshots Western Union receipt and app highlights hidden $42 spread markup.", "High", "High", "Critical")
            ),
            epistemicClaims = listOf(
                EpistemicClaim("Average hidden FX markup on remittances is 3.1%.", EpistemicType.FACT, "World Bank Remittance Prices Worldwide", "High")
            ),
            technicalFeasibility = 88,
            financialFeasibility = 80,
            regulatoryFeasibility = 85,
            aiBuildability = "High (OCR receipt reading + Forex API comparisons)",
            mvpObjective = "Upload receipt screenshot and calculate true dollar markup in under 3 seconds.",
            mvpCoreWorkflow = "Upload Screenshot -> OCR FX Rate -> Compare Interbank -> Show Hidden Fee.",
            mvpEssentialFeatures = "OCR receipt scanner, live FX ticker, savings calculator",
            mvpExcludedFeatures = listOf("Direct money transfer execution"),
            mvpEstimatedCost = listOf("$1,500 Forex API subscription & hosting"),
            mvpEstimatedEffort = "2 Weeks",
            experiments = listOf(
                ValidationExperiment("exp-rc1", "Users share audited receipts on social media", "Smoke Test", "25 receipt scans in week 1", "VALIDATED", "64 receipts audited in preliminary test.")
            ),
            actionItems = listOf(
                ActionItem("act-rc1", "Ship iOS & Android currency audit calculator", "7 Days", "P0", "Low", false)
            ),
            reminders = emptyList(),
            scoreHistory = listOf(
                ScoreHistoryEntry(1725969600000L, "Sep 10, 2026", 69, 68, 84, "Initial concept ingestion and receipt audit MVP.")
            ),
            createdAt = 1725969600000L, // September 2026
            updatedAt = 1725969600000L
        ),
        Idea(
            id = "veridoc-auto",
            name = "VeriDoc Auto",
            oneLineThesis = "Autonomous provider credentialing and CAQH automation for private practice groups.",
            description = "Autonomous agent orchestrator syncing state medical license boards, malpractice policies, and hospital privilege packets.",
            category = "Healthcare",
            tag = "Enterprise",
            categories = listOf("Healthcare", "AI", "Enterprise", "B2B"),
            priority = "High",
            status = IdeaStatus.BUILDING,
            isSample = true,
            opportunityScore = 84,
            marketScore = 86,
            feasibilityScore = 85,
            timingScore = 85,
            personalFitScore = 82,
            impactScore = 85,
            healthScore = 85,
            healthTrend = "Improving",
            confidence = "High",
            recommendation = Recommendation.BUILD_NOW,
            recommendationReason = "Synergistic with MediClaim AI healthcare compliance stack and shared HIPAA hospital credentialing relationships.",
            whyNow = "Physician onboarding delays cost clinics $8,000/day in idle billing capacity.",
            whyNot = "State medical boards have disparate, legacy CAPTCHA-protected login portals.",
            whatWouldChangeMind = "National unified provider credentialing database eliminating board paperwork.",
            biggestOpportunity = "Selling bundled revenue cycle + credentialing suite to 50+ doctor multi-specialty practices.",
            biggestRisk = "Missed DEA renewal date triggering clinician suspension.",
            biggestUnknown = "Whether practice managers will entrust automated license verification submissions.",
            nextImmediateAction = "Complete BAA pilot with Apex Billing and credential 10 physicians.",
            nextReviewDate = "In 7 days",
            problemStatement = "Onboarding a new physician takes 90-120 days of manual PDF filling across 40 payer credentialing portals.",
            problemSeverity = "Critical",
            problemCostOfInaction = "$240,000 lost revenue per delayed physician onboarding.",
            primaryUser = "Medical Staff Credentialing Coordinator",
            economicBuyer = "Chief Medical Officer / Practice CEO",
            decisionMaker = "Chief Medical Officer",
            customerObjections = listOf(
                "Can your bot log into state licensing boards without triggering security blocks?",
                "Do you support hospital bylaws privilege packets?"
            ),
            timingStatus = TimingStatus.RIGHT_TIME,
            opportunityWindow = OpportunityWindow.NOW,
            timingSignalsSummary = "High physician mobility and telehealth interstate licensing created unprecedented credentialing backlog.",
            waitUntilTriggers = emptyList(),
            marketSaturation = "Growing",
            competitors = listOf(
                CompetitorItem("Modio Health", "Direct", "Credentialing Software", "$350/provider/yr", "Acquired by CHG, good database", "Clunky manual workflow, no auto-recredentialing", "Legacy leader")
            ),
            marketGaps = listOf(
                MarketGap("Automated license board scraping & alerts", "Instantly detects license sanctions or renewal windows without manual coordinator audits.", "High", "High", "Critical")
            ),
            epistemicClaims = listOf(
                EpistemicClaim("Average clinic loses 72 days waiting on commercial payer credentialing approval.", EpistemicType.FACT, "MGMA Credentialing Report", "High")
            ),
            technicalFeasibility = 85,
            financialFeasibility = 82,
            regulatoryFeasibility = 80,
            aiBuildability = "High (Browser automation + Form filling agents)",
            mvpObjective = "Auto-fill CAQH profile and 3 payer enrollment packets in <15 minutes.",
            mvpCoreWorkflow = "Upload CV & Licenses -> AI Auto-fills CAQH -> Sync State Boards -> Alert Approvals.",
            mvpEssentialFeatures = "CAQH sync, license expiration monitor, PDF form auto-filler",
            mvpExcludedFeatures = listOf("Automated background check investigation"),
            mvpEstimatedCost = listOf("$3,000 secure infrastructure and BAA legal templates"),
            mvpEstimatedEffort = "3-4 Weeks",
            experiments = listOf(
                ValidationExperiment("exp-vd1", "Credentialing coordinators will test automated packet generator", "Paid Pilot", "3 clinics test with 5 providers each", "VALIDATED", "Completed with 2 orthopedic clinics.")
            ),
            actionItems = listOf(
                ActionItem("act-vd1", "Complete BAA pilot with Apex Billing and credential 10 physicians", "7 Days", "P0", "Medium", false)
            ),
            reminders = listOf(
                SmartReminder("rem-vd1", "Audit quarterly state medical board CAPTCHA policy changes", "Regulation", "Quarterly check", "Dec 1, 2026", false)
            ),
            scoreHistory = listOf(
                ScoreHistoryEntry(1728216000000L, "Oct 6, 2026", 84, 85, 85, "Initial onboarding and credentialing pilot launch.")
            ),
            createdAt = 1728216000000L, // October 2026
            updatedAt = 1728216000000L
        )
    )
}
