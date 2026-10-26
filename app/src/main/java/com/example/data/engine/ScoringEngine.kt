package com.example.data.engine

import com.example.data.model.Idea
import com.example.data.model.Recommendation
import com.example.data.model.TimingStatus

object ScoringEngine {
    fun calculateOpportunityScore(
        marketScore: Int,
        feasibilityScore: Int,
        timingScore: Int,
        impactScore: Int
    ): Int {
        val composite = (marketScore * 0.30f) + (timingScore * 0.30f) + (feasibilityScore * 0.25f) + (impactScore * 0.15f)
        return composite.toInt().coerceIn(0, 100)
    }

    fun deriveRecommendation(opportunityScore: Int, timingScore: Int, feasibilityScore: Int): Recommendation {
        return when {
            opportunityScore >= 80 && timingScore >= 75 && feasibilityScore >= 65 -> Recommendation.BUILD_NOW
            opportunityScore >= 70 && feasibilityScore >= 60 -> Recommendation.VALIDATE_FIRST
            opportunityScore in 60..69 && feasibilityScore >= 70 -> Recommendation.BUILD_SMALL
            opportunityScore < 60 && timingScore < 50 -> Recommendation.ABANDON
            timingScore < 50 -> Recommendation.WAIT
            feasibilityScore < 50 -> Recommendation.PARTNER
            else -> Recommendation.WATCH
        }
    }
}
