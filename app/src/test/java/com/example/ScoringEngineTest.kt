package com.example

import com.example.data.engine.ScoringEngine
import com.example.data.engine.SeedData
import com.example.data.model.Recommendation
import org.junit.Assert.*
import org.junit.Test

class ScoringEngineTest {

    @Test
    fun testScoreBoundsAndDeterminism() {
        val score1 = ScoringEngine.calculateOpportunityScore(
            marketScore = 80,
            feasibilityScore = 70,
            timingScore = 85,
            impactScore = 75
        )
        val score2 = ScoringEngine.calculateOpportunityScore(
            marketScore = 80,
            feasibilityScore = 70,
            timingScore = 85,
            impactScore = 75
        )

        assertEquals("Scores must be deterministic given identical inputs", score1, score2)
        assertTrue("Opportunity score must be >= 0", score1 >= 0)
        assertTrue("Opportunity score must be <= 100", score1 <= 100)
        assertEquals(78, score1)
    }

    @Test
    fun testStrategicRecommendationLogic() {
        val recWait = ScoringEngine.deriveRecommendation(
            opportunityScore = 65,
            timingScore = 35,
            feasibilityScore = 55
        )
        assertEquals(Recommendation.WAIT, recWait)

        val recAbandon = ScoringEngine.deriveRecommendation(
            opportunityScore = 50,
            timingScore = 40,
            feasibilityScore = 80
        )
        assertEquals(Recommendation.ABANDON, recAbandon)

        val recBuildNow = ScoringEngine.deriveRecommendation(
            opportunityScore = 85,
            timingScore = 85,
            feasibilityScore = 75
        )
        assertEquals(Recommendation.BUILD_NOW, recBuildNow)
    }

    @Test
    fun testSeedDataIntegrity() {
        assertTrue("Sample ideas must not be empty", SeedData.sampleIdeas.isNotEmpty())
        SeedData.sampleIdeas.forEach { idea ->
            assertTrue("Idea name must not be blank", idea.name.isNotBlank())
            assertTrue("One line thesis must not be blank", idea.oneLineThesis.isNotBlank())
            assertTrue("Problem statement must not be blank", idea.problemStatement.isNotBlank())
            assertTrue("Must contain competitors", idea.competitors.isNotEmpty())
            assertTrue("Must contain epistemic claims", idea.epistemicClaims.isNotEmpty())
            assertTrue("Opportunity score in range", idea.opportunityScore in 0..100)
            assertTrue("Timing score in range", idea.timingScore in 0..100)
            assertTrue("Feasibility score in range", idea.feasibilityScore in 0..100)
        }
    }
}
