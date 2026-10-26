package com.example.ui

/**
 * Navigation destination hierarchy for ORVEXA.
 */
sealed interface Screen {
    data object Dashboard : Screen
    data object MyIdeas : Screen
    data object AIAdvisor : Screen
    data object MarketIntelligence : Screen
    data object Experiments : Screen
    data object Actions : Screen
    data object Reminders : Screen
    data object Insights : Screen
    data object Charts : Screen
    data object Matrix : Screen
    data object Profile : Screen
    data object Settings : Screen
    data object RecycleBin : Screen
    data class AIAnalyzer(val initialIdeaId: String? = null) : Screen
    data class Detail(val ideaId: String) : Screen
}
