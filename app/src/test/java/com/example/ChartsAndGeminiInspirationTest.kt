package com.example

import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.data.remote.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChartsAndGeminiInspirationTest {

    @Test
    fun testPortfolioMetricsCalculation() {
        val ideas = listOf(
            Idea(
                id = "i1",
                name = "MediClaim",
                oneLineThesis = "Medical billing",
                description = "Desc",
                category = "Healthcare",
                tag = "AI",
                opportunityScore = 80,
                timingScore = 85,
                feasibilityScore = 75,
                recommendation = Recommendation.BUILD_NOW
            ),
            Idea(
                id = "i2",
                name = "TradeEscrow",
                oneLineThesis = "Escrow platform",
                description = "Desc",
                category = "Marketplace",
                tag = "Fintech",
                opportunityScore = 70,
                timingScore = 65,
                feasibilityScore = 60,
                recommendation = Recommendation.VALIDATE_FIRST
            ),
            Idea(
                id = "i3",
                name = "RemitFX",
                oneLineThesis = "Remittance tool",
                description = "Desc",
                category = "Fintech",
                tag = "Remittance",
                opportunityScore = 60,
                timingScore = 60,
                feasibilityScore = 70,
                recommendation = Recommendation.BUILD_SMALL
            )
        )

        val avgOpportunity = ideas.map { it.opportunityScore }.average().toInt()
        val avgTiming = ideas.map { it.timingScore }.average().toInt()
        val avgFeasibility = ideas.map { it.feasibilityScore }.average().toInt()

        assertEquals(70, avgOpportunity)
        assertEquals(70, avgTiming)
        assertEquals(68, avgFeasibility)

        val categoryCounts = ideas.groupingBy { it.category }.eachCount()
        assertEquals(1, categoryCounts["Healthcare"])
        assertEquals(1, categoryCounts["Marketplace"])
        assertEquals(1, categoryCounts["Fintech"])

        val recCounts = ideas.groupingBy { it.recommendation }.eachCount()
        assertEquals(1, recCounts[Recommendation.BUILD_NOW])
        assertEquals(1, recCounts[Recommendation.VALIDATE_FIRST])
        assertEquals(1, recCounts[Recommendation.BUILD_SMALL])
    }

    @Test
    fun testGeminiPromptSuggestions() = runBlocking {
        val geminiService = GeminiService()
        val suggestions = geminiService.suggestIdeaPrompts()

        assertNotNull(suggestions)
        assertTrue(suggestions.isNotEmpty())
        assertEquals(4, suggestions.size)

        // Verify that suggestion titles and prompts are rich and populated
        val first = suggestions.first()
        assertTrue(first.title.isNotBlank())
        assertTrue(first.category.isNotBlank())
        assertTrue(first.tag.isNotBlank())
        assertTrue(first.prompt.isNotBlank())
    }
}
