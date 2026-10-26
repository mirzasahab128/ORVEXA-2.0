package com.example.data.remote

/**
 * Single source of truth for "is a real Gemini API key configured?".
 *
 * The codebase previously checked different placeholder values in different
 * places (services checked "AQ.placeholder", screens checked "DEFAULT_KEY"
 * and "TODO_FILL_IN_SECRET"), so the UI and the AI layer could disagree
 * about whether AI was available. Every check must go through here.
 *
 * NOTE: a key baked into the app build (BuildConfig) can be extracted from
 * the APK. Before any public release, Gemini calls must move behind a
 * server-side proxy and this check must reflect the proxy configuration.
 */
object GeminiKeyConfig {

    private val placeholders = setOf(
        "AQ.placeholder",
        "DEFAULT_KEY",
        "TODO_FILL_IN_SECRET"
    )

    fun isConfigured(apiKey: String?): Boolean {
        if (apiKey.isNullOrBlank()) return false
        if (placeholders.contains(apiKey.trim())) return false
        return true
    }
}
