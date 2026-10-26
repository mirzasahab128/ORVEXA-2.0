package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.ui.export.IdeaExportHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExportAndPriorityFeatureTest {

    private val sampleIdeas = listOf(
        Idea(
            id = "test-1",
            name = "PriorAuth Accelerator",
            oneLineThesis = "Autonomous insurance prior authorization",
            description = "AI pipeline for prior authorizations",
            category = "Healthcare",
            tag = "B2B",
            priority = "High",
            categories = listOf("Healthcare", "B2B", "HIPAA", "LLM-Ops"),
            opportunityScore = 88,
            feasibilityScore = 78,
            timingScore = 90,
            recommendation = Recommendation.BUILD_NOW,
            whyNow = "CMS-0057-F federal mandates take effect in 2026",
            nextImmediateAction = "Deploy pilot with 3 ambulatory surgical centers"
        ),
        Idea(
            id = "test-2",
            name = "Subcontractor Escrow",
            oneLineThesis = "Milestone disbursements for commercial trade contractors",
            description = "Escrow and lien waiver automation",
            category = "Fintech",
            tag = "B2B",
            priority = "Medium",
            categories = listOf("Fintech", "B2B", "Construction"),
            opportunityScore = 72,
            feasibilityScore = 75,
            timingScore = 70,
            recommendation = Recommendation.VALIDATE_FIRST,
            whyNow = "High interest rates squeeze subcontractor working capital",
            nextImmediateAction = "Interview 10 general contractors"
        ),
        Idea(
            id = "test-3",
            name = "MicroSaaS Auditor",
            oneLineThesis = "Automated churn anomaly scanner",
            description = "Stripe webhook telemetry anomaly detector",
            category = "B2B SaaS",
            tag = "DevTools",
            priority = "Low",
            categories = listOf("B2B SaaS", "DevTools", "Bootstrapped"),
            opportunityScore = 62,
            feasibilityScore = 85,
            timingScore = 60,
            recommendation = Recommendation.BUILD_SMALL
        )
    )

    @Test
    fun testPriorityCategorization() {
        val highPriority = sampleIdeas.filter { it.priority.equals("High", ignoreCase = true) }
        val mediumPriority = sampleIdeas.filter { it.priority.equals("Medium", ignoreCase = true) }
        val lowPriority = sampleIdeas.filter { it.priority.equals("Low", ignoreCase = true) }

        assertEquals(1, highPriority.size)
        assertEquals("PriorAuth Accelerator", highPriority.first().name)

        assertEquals(1, mediumPriority.size)
        assertEquals("Subcontractor Escrow", mediumPriority.first().name)

        assertEquals(1, lowPriority.size)
        assertEquals("MicroSaaS Auditor", lowPriority.first().name)
    }

    @Test
    fun testCustomTaggingAndFiltering() {
        val hipaaTagged = sampleIdeas.filter { idea ->
            idea.tag.equals("HIPAA", ignoreCase = true) ||
            idea.categories.any { it.equals("HIPAA", ignoreCase = true) }
        }
        assertEquals(1, hipaaTagged.size)
        assertEquals("PriorAuth Accelerator", hipaaTagged.first().name)

        val b2bTagged = sampleIdeas.filter { idea ->
            idea.tag.equals("B2B", ignoreCase = true) ||
            idea.categories.any { it.equals("B2B", ignoreCase = true) }
        }
        assertEquals(2, b2bTagged.size)
    }

    @Test
    fun testTextFileExportGeneration() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val report = IdeaExportHelper.generateTextReport(sampleIdeas)
        assertTrue(report.contains("ORVEXA IDEA INTELLIGENCE OS"))
        assertTrue(report.contains("PRIORAUTH ACCELERATOR"))
        assertTrue(report.contains("Priority: High"))
        assertTrue(report.contains("Tags: #Healthcare, #B2B, #HIPAA, #LLM-Ops"))
        assertTrue(report.contains("Build Now"))
        assertTrue(report.contains("Average Opportunity Score: 74/100"))

        // Trigger text export and verify file written
        IdeaExportHelper.exportAsText(context, sampleIdeas)
        val exportFile = File(context.cacheDir, "orvexa_ideas_export.txt")
        assertTrue("Export text file must exist in cacheDir", exportFile.exists())
    }

    @Test
    fun testClipboardExportGeneration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        IdeaExportHelper.copyToClipboard(context, sampleIdeas)
        // Verify no crash when copying to clipboard
        assertTrue(true)
    }
}
