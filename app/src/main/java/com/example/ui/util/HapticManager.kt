package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Centralized HapticManager providing tactile confirmation across buttons, cards,
 * navigation items, and deletion/undo triggers.
 */
class HapticManager(private val context: Context, private val composeHaptics: HapticFeedback? = null) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Standard subtle tactile click for buttons, tabs, chips, and list toggles.
     */
    fun performClick() {
        try {
            composeHaptics?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) { }
    }

    /**
     * Firm tactile feedback for major actions (saving, FAB creation, delete, undo).
     */
    fun performHeavyClick() {
        try {
            composeHaptics?.performHapticFeedback(HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) { }
    }

    /**
     * Positive tactile confirmation for sync complete, idea restored, or experiment saved.
     */
    fun performSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 20, 60, 25)
                val amplitudes = intArrayOf(0, 160, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 20, 60, 25), -1)
            }
        } catch (_: Exception) { }
    }

    /**
     * Subtle tick for toggle switches, sliders, or filter chip selections.
     */
    fun performTick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) { }
    }
}

@Composable
fun rememberHapticManager(): HapticManager {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    return remember(context, composeHaptics) {
        HapticManager(context, composeHaptics)
    }
}
