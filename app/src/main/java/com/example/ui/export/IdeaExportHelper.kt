package com.example.ui.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Idea
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object IdeaExportHelper {

    fun generateTextReport(ideas: List<Idea>): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val exportDate = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("ORVEXA IDEA INTELLIGENCE OS — PORTFOLIO REPORT")
        sb.appendLine("Exported: $exportDate")
        sb.appendLine("Total Ideas: ${ideas.size}")
        val avgScore = if (ideas.isNotEmpty()) (ideas.map { it.opportunityScore }.average()).toInt() else 0
        sb.appendLine("Average Opportunity Score: $avgScore/100")
        sb.appendLine("==================================================")
        sb.appendLine()

        ideas.forEachIndexed { index, idea ->
            sb.appendLine("[${index + 1}] ${idea.name.uppercase()}")
            sb.appendLine("Opportunity Score: ${idea.opportunityScore}/100 | Feasibility: ${idea.feasibilityScore} | Timing: ${idea.timingScore}")
            sb.appendLine("Strategic Recommendation: ${idea.recommendation.label}")
            sb.appendLine("Priority: ${idea.priority} | Sector: ${idea.category} | Model Tag: #${idea.tag}")
            sb.appendLine("Benchmark Percentile: ${idea.benchmarkPercentile}th percentile (${idea.benchmarkSector})")
            if (idea.categories.isNotEmpty()) {
                sb.appendLine("Tags: ${idea.categories.joinToString(", ") { "#$it" }}")
            }
            sb.appendLine("Thesis: ${idea.oneLineThesis.ifEmpty { idea.description }}")
            if (idea.benchmarkSummary.isNotEmpty()) {
                sb.appendLine("Benchmark Analysis: ${idea.benchmarkSummary}")
            }
            if (idea.whyNow.isNotEmpty()) {
                sb.appendLine("Market Tailwinds (Why Now): ${idea.whyNow}")
            }
            if (idea.nextImmediateAction.isNotEmpty()) {
                sb.appendLine("Next Immediate Action: ${idea.nextImmediateAction}")
            }
            sb.appendLine("--------------------------------------------------")
            sb.appendLine()
        }
        return sb.toString()
    }

    fun exportAsText(context: Context, ideas: List<Idea>) {
        if (ideas.isEmpty()) {
            Toast.makeText(context, "No saved ideas to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val exportText = generateTextReport(ideas)
            val file = File(context.cacheDir, "orvexa_ideas_export.txt")
            file.writeText(exportText)

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Orvexa Ideas Portfolio Report")
                putExtra(Intent.EXTRA_TEXT, exportText)
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            val chooser = Intent.createChooser(sendIntent, "Export Ideas as Text File")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exports a structured, executive evaluation report for a single selected idea.
     * Includes scores, benchmark comparisons, feasibility breakdown, risk mitigations, and next steps.
     */
    fun exportSingleIdeaEvaluationReportPdf(context: Context, idea: Idea) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            val titlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 17f
                color = Color.rgb(15, 23, 42) // Dark Slate
            }
            val subPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 9.5f
                color = Color.rgb(100, 116, 139)
            }
            val sectionPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 11f
                color = Color.rgb(2, 132, 199) // Cyan Accent
            }
            val boldPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 11f
                color = Color.rgb(15, 23, 42)
            }
            val bodyPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 9.5f
                color = Color.rgb(51, 65, 85)
            }
            val headerBg = Paint().apply { color = Color.rgb(241, 245, 249) }
            val cardBg = Paint().apply { color = Color.rgb(248, 250, 252) }
            val cardBorder = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.rgb(226, 232, 240)
            }
            val cyanPillBg = Paint().apply { color = Color.rgb(224, 242, 254) }
            val greenPillBg = Paint().apply { color = Color.rgb(220, 252, 231) }
            val purplePillBg = Paint().apply { color = Color.rgb(243, 232, 255) }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            // ---------------- PAGE 1: EXECUTIVE BRIEF & BENCHMARK SCORECARD ----------------
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            var y = 35f

            // Top Header Banner
            canvas.drawRect(24f, y, (pageWidth - 24).toFloat(), y + 62f, headerBg)
            canvas.drawText("ORVEXA IDEA INTELLIGENCE OS", 38f, y + 25f, titlePaint)
            canvas.drawText("CONFIDENTIAL VENTURE EVALUATION REPORT • $dateStr", 38f, y + 42f, subPaint)
            canvas.drawText("OPPORTUNITY ID: ${idea.id.uppercase()}  |  LIFECYCLE STATUS: ${idea.status.label.uppercase()}", 38f, y + 54f, subPaint)

            y += 76f

            // Idea Header Card
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + 74f, 8f, 8f, cardBg)
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + 74f, 8f, 8f, cardBorder)
            canvas.drawText(idea.name.uppercase(), 38f, y + 24f, titlePaint)
            canvas.drawText("SECTOR: ${idea.category.uppercase()}   •   TAG: #${idea.tag.uppercase()}   •   PRIORITY: ${idea.priority.uppercase()}", 38f, y + 40f, subPaint)

            val thesisText = idea.oneLineThesis.ifEmpty { idea.description }
            val thesisTrunc = if (thesisText.length > 88) thesisText.take(85) + "..." else thesisText
            canvas.drawText("Thesis: $thesisTrunc", 38f, y + 60f, bodyPaint)

            y += 88f

            // Core Scorecards Row (4 pills)
            val boxWidth = (pageWidth - 48f - 30f) / 4f
            val scoreBoxHeight = 56f

            fun drawScoreBox(left: Float, label: String, score: String, bg: Paint, textCol: Int) {
                canvas.drawRoundRect(left, y, left + boxWidth, y + scoreBoxHeight, 8f, 8f, bg)
                canvas.drawRoundRect(left, y, left + boxWidth, y + scoreBoxHeight, 8f, 8f, cardBorder)
                canvas.drawText(label, left + 10f, y + 18f, subPaint)
                val p = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 18f
                    color = textCol
                }
                canvas.drawText(score, left + 10f, y + 44f, p)
            }

            drawScoreBox(24f, "OPPORTUNITY", "${idea.opportunityScore}/100", cyanPillBg, Color.rgb(2, 132, 199))
            drawScoreBox(24f + boxWidth + 10f, "FEASIBILITY", "${idea.feasibilityScore}/100", purplePillBg, Color.rgb(126, 34, 206))
            drawScoreBox(24f + (boxWidth + 10f) * 2, "TIMING", "${idea.timingScore}/100", greenPillBg, Color.rgb(22, 101, 52))
            drawScoreBox(24f + (boxWidth + 10f) * 3, "VERDICT", idea.recommendation.label.take(10), headerBg, Color.rgb(15, 23, 42))

            y += scoreBoxHeight + 16f

            // Industry Benchmark Analysis Card
            val benchmarkCardHeight = 152f
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + benchmarkCardHeight, 8f, 8f, cardBg)
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + benchmarkCardHeight, 8f, 8f, cardBorder)

            canvas.drawText("INDUSTRY BENCHMARK EVALUATION (GEMINI INTELLIGENCE)", 38f, y + 22f, sectionPaint)
            val sectorStr = idea.benchmarkSector.ifEmpty { "Enterprise B2B Workflow SaaS" }
            canvas.drawText("BENCHMARK INDEX: $sectorStr", 38f, y + 40f, boldPaint)
            val percentileStr = "${idea.benchmarkPercentile}th Percentile"
            canvas.drawText("SECTOR STANDING: $percentileStr", (pageWidth - 210).toFloat(), y + 40f, sectionPaint)

            val feasDelta = idea.feasibilityScore - idea.benchmarkFeasibilityMedian
            val oppDelta = idea.opportunityScore - idea.benchmarkOpportunityMedian
            val feasDeltaStr = if (feasDelta >= 0) "+$feasDelta pts" else "$feasDelta pts"
            val oppDeltaStr = if (oppDelta >= 0) "+$oppDelta pts" else "$oppDelta pts"

            canvas.drawText("Feasibility Delta: $feasDeltaStr vs Median (${idea.benchmarkFeasibilityMedian})   |   Opportunity Delta: $oppDeltaStr vs Median (${idea.benchmarkOpportunityMedian})", 38f, y + 58f, bodyPaint)

            val summaryText = idea.benchmarkSummary.ifEmpty {
                "Outperforms ${idea.benchmarkPercentile}% of early-stage opportunities in $sectorStr with accelerated product-market timing."
            }
            val summaryTrunc1 = if (summaryText.length > 85) summaryText.take(82) + "..." else summaryText
            canvas.drawText("Benchmark Synthesis: $summaryTrunc1", 38f, y + 78f, bodyPaint)

            val diffText = idea.benchmarkDifferentiator.ifEmpty { "Specialized vertical workflow telemetry creating 10x switching costs." }
            val diffTrunc = if (diffText.length > 82) diffText.take(79) + "..." else diffText
            canvas.drawText("Key Differentiator: $diffTrunc", 38f, y + 98f, bodyPaint)

            val riskText = idea.benchmarkDownsideRisk.ifEmpty { "Elongated procurement sales cycles and legacy enterprise vendor lock-in." }
            val riskTrunc = if (riskText.length > 82) riskText.take(79) + "..." else riskText
            canvas.drawText("Sector Downside Risk: $riskTrunc", 38f, y + 118f, bodyPaint)

            val recReason = idea.recommendationReason.ifEmpty { "High conviction thesis validated by structural sector tailwinds." }
            val recTrunc = if (recReason.length > 85) recReason.take(82) + "..." else recReason
            canvas.drawText("Strategic Rationale: $recTrunc", 38f, y + 138f, subPaint)

            y += benchmarkCardHeight + 16f

            // Problem & Customer Thesis Card
            val problemCardHeight = 110f
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + problemCardHeight, 8f, 8f, cardBg)
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + problemCardHeight, 8f, 8f, cardBorder)

            canvas.drawText("PROBLEM THESIS & ECONOMIC BUYER ARCHITECTURE", 38f, y + 22f, sectionPaint)
            val probStmt = idea.problemStatement.ifEmpty { idea.description }
            val probTrunc = if (probStmt.length > 88) probStmt.take(85) + "..." else probStmt
            canvas.drawText("Friction: $probTrunc", 38f, y + 42f, bodyPaint)

            val costInact = idea.problemCostOfInaction.ifEmpty { "Significant recurring annual cash bleed and unrecovered team labor." }
            val costTrunc = if (costInact.length > 82) costInact.take(79) + "..." else costInact
            canvas.drawText("Cost of Inaction: $costTrunc", 38f, y + 62f, bodyPaint)

            val icpText = "Primary User: ${idea.primaryUser.ifEmpty { "Domain Practitioner" }}  |  Buyer: ${idea.economicBuyer.ifEmpty { "Department Head" }}  |  Severity: ${idea.problemSeverity}"
            val icpTrunc = if (icpText.length > 88) icpText.take(85) + "..." else icpText
            canvas.drawText(icpTrunc, 38f, y + 82f, subPaint)

            val whyNowText = idea.whyNow.ifEmpty { "Regulatory tailwinds and modern cloud APIs create an open 12-month wedge." }
            val whyNowTrunc = if (whyNowText.length > 85) whyNowText.take(82) + "..." else whyNowText
            canvas.drawText("Market Tailwinds (Why Now): $whyNowTrunc", 38f, y + 98f, bodyPaint)

            pdfDocument.finishPage(page)

            // ---------------- PAGE 2: FEASIBILITY & EXECUTION ROADMAP ----------------
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas

            y = 35f

            // Top Header Continuation
            canvas.drawRect(24f, y, (pageWidth - 24).toFloat(), y + 44f, headerBg)
            canvas.drawText("ORVEXA EVALUATION DOSSIER — FEASIBILITY & VALIDATION ROADMAP", 38f, y + 26f, sectionPaint)
            canvas.drawText("OPPORTUNITY: ${idea.name.uppercase()} (Page 2 of 2)", (pageWidth - 190).toFloat(), y + 26f, subPaint)

            y += 60f

            // Feasibility Sub-Scores Card
            val feasCardHeight = 110f
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + feasCardHeight, 8f, 8f, cardBg)
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + feasCardHeight, 8f, 8f, cardBorder)

            canvas.drawText("FEASIBILITY ARCHITECTURE DECOMPOSITION", 38f, y + 22f, sectionPaint)
            canvas.drawText("Technical Feasibility: ${idea.technicalFeasibility}/100", 38f, y + 44f, boldPaint)
            canvas.drawText("Financial Feasibility: ${idea.financialFeasibility}/100", 220f, y + 44f, boldPaint)
            canvas.drawText("Regulatory Feasibility: ${idea.regulatoryFeasibility}/100", 390f, y + 44f, boldPaint)

            val aiBuild = idea.aiBuildability.ifEmpty { "High (Standard Gemini Models + Modern Cloud Infrastructure)" }
            canvas.drawText("AI Buildability: $aiBuild", 38f, y + 66f, bodyPaint)

            val mvpEffort = "Estimated MVP Build Cycle: ${idea.mvpEstimatedEffort}  |  Estimated Initial Capital: $25k - $50k"
            canvas.drawText(mvpEffort, 38f, y + 86f, subPaint)

            y += feasCardHeight + 16f

            // Immediate Action Items & Experiments Card
            val actionCardHeight = 130f
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + actionCardHeight, 8f, 8f, cardBg)
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + actionCardHeight, 8f, 8f, cardBorder)

            canvas.drawText("VALIDATION EXPERIMENTS & IMMEDIATE ACTION ROADMAP", 38f, y + 22f, sectionPaint)
            val nextAct = idea.nextImmediateAction.ifEmpty { "Interview 5 prospective target customers to validate willingness to pay." }
            val nextActTrunc = if (nextAct.length > 85) nextAct.take(82) + "..." else nextAct
            canvas.drawText("1. Immediate Step: $nextActTrunc", 38f, y + 44f, boldPaint)

            val exp1 = if (idea.experiments.isNotEmpty()) idea.experiments[0].assumption else "Validate pricing model against 3 anchor buyers."
            val exp1Trunc = if (exp1.length > 82) exp1.take(79) + "..." else exp1
            canvas.drawText("2. Hypothesis Experiment: $exp1Trunc", 38f, y + 66f, bodyPaint)

            val riskStmt = idea.biggestRisk.ifEmpty { "Distribution access and user reluctance to displace existing systems." }
            val riskStmtTrunc = if (riskStmt.length > 82) riskStmt.take(79) + "..." else riskStmt
            canvas.drawText("3. Core Risk to De-risk: $riskStmtTrunc", 38f, y + 88f, bodyPaint)

            val reviewText = "Recommended Next Review: ${idea.nextReviewDate}  |  Health Trend: ${idea.healthTrend}"
            canvas.drawText(reviewText, 38f, y + 110f, subPaint)

            y += actionCardHeight + 16f

            // Historical Audit Trail Box
            val historyBoxHeight = 110f
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + historyBoxHeight, 8f, 8f, cardBg)
            canvas.drawRoundRect(24f, y, (pageWidth - 24).toFloat(), y + historyBoxHeight, 8f, 8f, cardBorder)

            canvas.drawText("HISTORICAL AUDIT TRAIL & TRAJECTORY", 38f, y + 22f, sectionPaint)
            if (idea.scoreHistory.isNotEmpty()) {
                val recentEntries = idea.scoreHistory.takeLast(3)
                var hy = y + 42f
                for (entry in recentEntries) {
                    val entryLine = "• ${entry.dateLabel}: Opp ${entry.opportunityScore} | Feas ${entry.feasibilityScore} | Timing ${entry.timingScore} — ${entry.reason.take(50)}"
                    canvas.drawText(entryLine, 38f, hy, bodyPaint)
                    hy += 18f
                }
            } else {
                canvas.drawText("Initial evaluation generated on $dateStr via Automated Gemini Benchmark Engine.", 38f, y + 44f, bodyPaint)
            }

            pdfDocument.finishPage(page)

            // Write PDF
            val file = File(context.cacheDir, "orvexa_evaluation_${idea.id}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Orvexa Evaluation Report — ${idea.name}")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Share Evaluation Report (PDF)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Report generation error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exports selected ideas or full portfolio to a PDF document with benchmark metrics.
     */
    fun exportSelectedIdeasPdf(context: Context, ideas: List<Idea>, reportTitle: String = "Selected Opportunities Brief") {
        if (ideas.isEmpty()) {
            Toast.makeText(context, "No ideas selected to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 17f
                color = Color.rgb(15, 23, 42)
            }
            val subPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 9.5f
                color = Color.rgb(100, 116, 139)
            }
            val headerBg = Paint().apply { color = Color.rgb(241, 245, 249) }
            val cardBg = Paint().apply { color = Color.rgb(248, 250, 252) }
            val strokePaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.rgb(226, 232, 240)
            }
            val boldPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 11.5f
                color = Color.rgb(15, 23, 42)
            }
            val bodyPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 9.5f
                color = Color.rgb(51, 65, 85)
            }
            val accentPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 10.5f
                color = Color.rgb(2, 132, 199)
            }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            var y = 35f

            // Top Banner
            canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 58f, headerBg)
            canvas.drawText("ORVEXA IDEA INTELLIGENCE OS", 34f, y + 24f, titlePaint)
            val avgScore = (ideas.map { it.opportunityScore }.average()).toInt()
            val avgFeas = (ideas.map { it.feasibilityScore }.average()).toInt()
            canvas.drawText("$reportTitle • Generated $dateStr • ${ideas.size} Ideas (Avg Opp: $avgScore/100, Avg Feas: $avgFeas/100)", 34f, y + 44f, subPaint)

            y += 74f

            for (i in ideas.indices) {
                val idea = ideas[i]
                val cardHeight = 110f

                if (y + cardHeight > pageHeight - 35f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 35f

                    canvas.drawText("ORVEXA REPORT (Page $pageNumber)", 30f, y, subPaint)
                    y += 20f
                }

                canvas.drawRoundRect(20f, y, (pageWidth - 20).toFloat(), y + cardHeight, 8f, 8f, cardBg)
                canvas.drawRoundRect(20f, y, (pageWidth - 20).toFloat(), y + cardHeight, 8f, 8f, strokePaint)

                val titleTrunc = if (idea.name.length > 36) idea.name.take(33) + "..." else idea.name
                canvas.drawText("${i + 1}. $titleTrunc", 32f, y + 20f, boldPaint)
                canvas.drawText("Opp: ${idea.opportunityScore} | Feas: ${idea.feasibilityScore} | ${idea.recommendation.label}", (pageWidth - 230).toFloat(), y + 20f, accentPaint)

                val benchmarkInfo = "${idea.benchmarkPercentile}th %ile in ${idea.benchmarkSector.take(28)}"
                canvas.drawText("Priority: ${idea.priority}  |  Sector: ${idea.category}  |  $benchmarkInfo", 32f, y + 38f, subPaint)

                val thesisText = idea.oneLineThesis.ifEmpty { idea.description }
                val thesisTrunc = if (thesisText.length > 85) thesisText.take(82) + "..." else thesisText
                canvas.drawText("Thesis: $thesisTrunc", 32f, y + 58f, bodyPaint)

                val nextAction = idea.nextImmediateAction.ifEmpty { "Validate assumptions with 5 users" }
                val nextActionTrunc = if (nextAction.length > 80) nextAction.take(77) + "..." else nextAction
                canvas.drawText("Next Action: $nextActionTrunc", 32f, y + 76f, bodyPaint)

                val whyNow = idea.whyNow.ifEmpty { idea.recommendationReason }
                if (whyNow.isNotEmpty()) {
                    val whyNowTrunc = if (whyNow.length > 85) whyNow.take(82) + "..." else whyNow
                    canvas.drawText("Strategic Signal: $whyNowTrunc", 32f, y + 94f, subPaint)
                }

                y += cardHeight + 10f
            }

            pdfDocument.finishPage(page)

            val file = File(context.cacheDir, "orvexa_ideas_export.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Orvexa Idea Intelligence Portfolio")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, "Share Ideas PDF Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportAsPdf(context: Context, ideas: List<Idea>) {
        exportSelectedIdeasPdf(context, ideas, "Portfolio Executive Brief")
    }

    fun copyToClipboard(context: Context, ideas: List<Idea>) {
        if (ideas.isEmpty()) {
            Toast.makeText(context, "No saved ideas to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.appendLine("ORVEXA IDEA INTELLIGENCE PORTFOLIO (${ideas.size} Ideas):")
        ideas.forEachIndexed { i, idea ->
            sb.appendLine("${i + 1}. ${idea.name} (${idea.opportunityScore}/100 - ${idea.recommendation.label}) - ${idea.priority} Priority")
            sb.appendLine("   Thesis: ${idea.oneLineThesis.ifEmpty { idea.description }}")
            sb.appendLine("   Benchmark: ${idea.benchmarkPercentile}th percentile (${idea.benchmarkSector})")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Orvexa Ideas Portfolio", sb.toString())
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "Portfolio copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
