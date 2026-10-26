package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    NIGHT,
    MORNING,
    DARK,
    LIGHT_OBSIDIAN,
    SYSTEM
}

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.NIGHT }

private val OrbNightColorScheme = darkColorScheme(
    primary = ThemeTokens.Night.modeAnalyze,
    onPrimary = Color.White,
    primaryContainer = ThemeTokens.Night.inputChipFill,
    onPrimaryContainer = Color.White,
    secondary = ThemeTokens.Night.modeResearch,
    onSecondary = Color.White,
    secondaryContainer = ThemeTokens.Night.selectedPillFill,
    onSecondaryContainer = Color.White,
    tertiary = ThemeTokens.Night.modeCompare,
    onTertiary = Color.White,
    background = ThemeTokens.Night.pageBackground,
    onBackground = ThemeTokens.Night.textPrimary,
    surface = Color.Transparent,
    onSurface = ThemeTokens.Night.textPrimary,
    surfaceVariant = ThemeTokens.Night.inputChipFill,
    onSurfaceVariant = ThemeTokens.Night.textSecondary,
    outline = ThemeTokens.Night.glassBorder,
    outlineVariant = ThemeTokens.Night.inputChipBorder,
    error = ThemeTokens.Night.modeError,
    onError = Color.White
)

private val OrbMorningColorScheme = lightColorScheme(
    primary = ThemeTokens.Morning.primaryButtonFill,
    onPrimary = Color.White,
    primaryContainer = ThemeTokens.Morning.inputChipFill,
    onPrimaryContainer = ThemeTokens.Morning.textPrimary,
    secondary = ThemeTokens.Morning.modeResearch,
    onSecondary = Color.White,
    secondaryContainer = ThemeTokens.Morning.selectedPillFill,
    onSecondaryContainer = ThemeTokens.Morning.textPrimary,
    tertiary = ThemeTokens.Morning.modeCompare,
    onTertiary = Color.White,
    background = ThemeTokens.Morning.pageBackground,
    onBackground = ThemeTokens.Morning.textPrimary,
    surface = Color.Transparent,
    onSurface = ThemeTokens.Morning.textPrimary,
    surfaceVariant = ThemeTokens.Morning.inputChipFill,
    onSurfaceVariant = ThemeTokens.Morning.textSecondary,
    outline = ThemeTokens.Morning.glassBorder,
    outlineVariant = ThemeTokens.Morning.inputChipBorder,
    error = ThemeTokens.Morning.modeError,
    onError = Color.White
)

@Composable
fun OrvexaTheme(
    themeMode: ThemeMode = ThemeMode.NIGHT,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isNight = when (themeMode) {
        ThemeMode.NIGHT, ThemeMode.DARK -> true
        ThemeMode.MORNING, ThemeMode.LIGHT_OBSIDIAN -> false
        ThemeMode.SYSTEM -> systemInDark
    }

    val tokens = if (isNight) ThemeTokens.Night else ThemeTokens.Morning
    val colorScheme = if (isNight) OrbNightColorScheme else OrbMorningColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isNight
                controller.isAppearanceLightNavigationBars = !isNight
            }
        }
    }

    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalThemeTokens provides tokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OrvexaTypography,
            content = content
        )
    }
}

