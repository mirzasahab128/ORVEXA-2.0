package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Exact design tokens for the ORVEXA Orb Core design system: Night (dark) and Morning (light).
 * Hardcodes no colors in screen composables; all components read from LocalThemeTokens.current.
 */
data class ThemeTokens(
    val isNight: Boolean,
    val pageBackground: Color,
    val isPageGradient: Boolean,
    val pageGradientStops: List<Color>,
    // Backdrop glows (drawn as radial gradient brushes on Canvas)
    val glowTopLeft: Color,
    val glowRight: Color,
    val glowBottom: Color,
    // Glass surface
    val glassFillTop: Color,
    val glassFillBottom: Color,
    val glassBorder: Color,
    val glassInnerTopHighlight: Color,
    val glassInnerBottomLine: Color,
    val glassShadow: Color,
    val glassShadowBlur: Dp,
    val glassShadowOffsetY: Dp,
    // Morning extra shadow
    val hasSecondShadow: Boolean = false,
    val secondShadowColor: Color = Color.Transparent,
    val secondShadowBlur: Dp = 0.dp,
    val secondShadowOffsetY: Dp = 0.dp,
    // Text tokens
    val textPrimary: Color,
    val textSecondary: Color,
    val textLabel: Color,
    // Selected pill
    val selectedPillFill: Color,
    val selectedPillHighlight: Color,
    // Input and chip
    val inputChipFill: Color,
    val inputChipBorder: Color,
    // Primary button
    val primaryButtonFill: Color,
    val primaryButtonText: Color,
    // Orb glow & shadows
    val orbGlowAlpha: Float,
    val orbGlowBlur: Dp,
    val orbGlowSpread: Dp,
    val hasGroundShadow: Boolean,
    val groundShadowColor: Color,
    // Rings and shell
    val ringStrokeR1: Color,
    val ringStrokeR2: Color,
    val ringStrokeR3: Color,
    val shellBorder: Color,
    val shellFillTop: Color,
    val shellFillBottom: Color,
    // Mode colors
    val modeAnalyze: Color,
    val modeResearch: Color,
    val modeCompare: Color,
    val modeValidate: Color,
    val modeListening: Color = Color(0xFFE4531F),
    val modeError: Color
) {
    companion object {
        val Night = ThemeTokens(
            isNight = true,
            pageBackground = Color(0xFF070816),
            isPageGradient = false,
            pageGradientStops = listOf(Color(0xFF070816), Color(0xFF070816)),
            // #3B4CFF at 45% top left, #B13CFF at 35% right, #12B5C9 at 30% bottom
            glowTopLeft = Color(0xFF3B4CFF).copy(alpha = 0.45f),
            glowRight = Color(0xFFB13CFF).copy(alpha = 0.35f),
            glowBottom = Color(0xFF12B5C9).copy(alpha = 0.30f),
            // Glass fill: gradient from white 16% to white 5% at 160 deg
            glassFillTop = Color.White.copy(alpha = 0.16f),
            glassFillBottom = Color.White.copy(alpha = 0.05f),
            // Glass border: 1dp white 20%. Inner top highlight 1dp white 38%. Inner bottom line white 6%
            glassBorder = Color.White.copy(alpha = 0.20f),
            glassInnerTopHighlight = Color.White.copy(alpha = 0.38f),
            glassInnerBottomLine = Color.White.copy(alpha = 0.06f),
            // Glass shadow: black 35%, blur 24dp, offset y 16dp
            glassShadow = Color.Black.copy(alpha = 0.35f),
            glassShadowBlur = 24.dp,
            glassShadowOffsetY = 16.dp,
            hasSecondShadow = false,
            // Text: primary #FFFFFF, secondary white 78%, label white 72%
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color.White.copy(alpha = 0.78f),
            textLabel = Color.White.copy(alpha = 0.72f),
            // Selected pill: white 24% fill, top highlight white 55%
            selectedPillFill = Color.White.copy(alpha = 0.24f),
            selectedPillHighlight = Color.White.copy(alpha = 0.55f),
            // Input and chip fill: white 10%, border white 16%
            inputChipFill = Color.White.copy(alpha = 0.10f),
            inputChipBorder = Color.White.copy(alpha = 0.16f),
            // Primary button: #FFFFFF fill, #12123A text
            primaryButtonFill = Color(0xFFFFFFFF),
            primaryButtonText = Color(0xFF12123A),
            // Orb glow: tint color at 55%, blur 90dp equivalent, spread 8dp
            orbGlowAlpha = 0.55f,
            orbGlowBlur = 90.dp,
            orbGlowSpread = 8.dp,
            hasGroundShadow = false,
            groundShadowColor = Color.Transparent,
            // Ring strokes: white 45%, 28%, and 30%. Shell border white 45%
            ringStrokeR1 = Color.White.copy(alpha = 0.45f),
            ringStrokeR2 = Color.White.copy(alpha = 0.28f),
            ringStrokeR3 = Color.White.copy(alpha = 0.30f),
            shellBorder = Color.White.copy(alpha = 0.45f),
            shellFillTop = Color.White.copy(alpha = 0.35f),
            shellFillBottom = Color.White.copy(alpha = 0.14f),
            // Mode colors: Analyze Night #6A7DFF, Research Night #1FB6E8, Compare Night #FF7A6E, Validate Night #35C987
            modeAnalyze = Color(0xFF6A7DFF),
            modeResearch = Color(0xFF1FB6E8),
            modeCompare = Color(0xFFFF7A6E),
            modeValidate = Color(0xFF35C987),
            modeListening = Color(0xFFE4531F),
            modeError = Color(0xFFFFB020)
        )

        val Morning = ThemeTokens(
            isNight = false,
            pageBackground = Color(0xFFBCD3FF),
            isPageGradient = true,
            // Page: gradient at 135 degrees from #BCD3FF to #DCC9FF to #FFD2C2
            pageGradientStops = listOf(Color(0xFFBCD3FF), Color(0xFFDCC9FF), Color(0xFFFFD2C2)),
            // Backdrop glows: #6F8BFF at 50% top left, #FF8FC8 at 45% right, #7BE0D6 at 45% bottom
            glowTopLeft = Color(0xFF6F8BFF).copy(alpha = 0.50f),
            glowRight = Color(0xFFFF8FC8).copy(alpha = 0.45f),
            glowBottom = Color(0xFF7BE0D6).copy(alpha = 0.45f),
            // Glass fill: gradient from white 70% to white 34% at 160 degrees
            glassFillTop = Color.White.copy(alpha = 0.70f),
            glassFillBottom = Color.White.copy(alpha = 0.34f),
            // Glass border: 1dp white 75%. Inner top highlight white 95%. Inner bottom line white 40%
            glassBorder = Color.White.copy(alpha = 0.75f),
            glassInnerTopHighlight = Color.White.copy(alpha = 0.95f),
            glassInnerBottomLine = Color.White.copy(alpha = 0.40f),
            // Glass shadow: rgba(70,60,140) at 22%, blur 40dp, offset y 24dp. Second shadow at 12%, blur 12dp, offset y 6dp
            glassShadow = Color(70, 60, 140, (255 * 0.22).toInt()),
            glassShadowBlur = 40.dp,
            glassShadowOffsetY = 24.dp,
            hasSecondShadow = true,
            secondShadowColor = Color(70, 60, 140, (255 * 0.12).toInt()),
            secondShadowBlur = 12.dp,
            secondShadowOffsetY = 6.dp,
            // Text: primary #1B1D33, secondary #3D4060, label #4A4D6A
            textPrimary = Color(0xFF1B1D33),
            textSecondary = Color(0xFF3D4060),
            textLabel = Color(0xFF4A4D6A),
            // Selected pill: solid white with a soft shadow
            selectedPillFill = Color.White,
            selectedPillHighlight = Color.White,
            // Input and chip fill: white 55%, border white 70%
            inputChipFill = Color.White.copy(alpha = 0.55f),
            inputChipBorder = Color.White.copy(alpha = 0.70f),
            // Primary button: #1B1D33 fill, #FFFFFF text
            primaryButtonFill = Color(0xFF1B1D33),
            primaryButtonText = Color(0xFFFFFFFF),
            // Orb shadow: tint color at 50%, blur 70dp, offset y 30dp. Ground shadow ellipse 200x26dp rgba(60,50,140) at 35%
            orbGlowAlpha = 0.50f,
            orbGlowBlur = 70.dp,
            orbGlowSpread = 0.dp,
            hasGroundShadow = true,
            groundShadowColor = Color(60, 50, 140, (255 * 0.35).toInt()),
            // Ring strokes: white 85%, 60%, and 65%. Shell border white 70%
            ringStrokeR1 = Color.White.copy(alpha = 0.85f),
            ringStrokeR2 = Color.White.copy(alpha = 0.60f),
            ringStrokeR3 = Color.White.copy(alpha = 0.65f),
            shellBorder = Color.White.copy(alpha = 0.70f),
            shellFillTop = Color.White.copy(alpha = 0.55f),
            shellFillBottom = Color.White.copy(alpha = 0.25f),
            // Mode colors: Analyze Morning dot #5B6DFF, Research Morning dot #12A8D0, Compare Morning dot #EE5F78, Validate Morning dot #22A870
            modeAnalyze = Color(0xFF5B6DFF),
            modeResearch = Color(0xFF12A8D0),
            modeCompare = Color(0xFFEE5F78),
            modeValidate = Color(0xFF22A870),
            modeListening = Color(0xFFE4531F),
            modeError = Color(0xFFB26B00)
        )
    }
}

val LocalThemeTokens = staticCompositionLocalOf { ThemeTokens.Night }

object AppTheme {
    val tokens: ThemeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeTokens.current
}
