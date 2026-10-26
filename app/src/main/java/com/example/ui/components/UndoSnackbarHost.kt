package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Idea
import com.example.ui.util.HapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pinned Undo Snackbar supporting a persistent 5-second window with tactile haptics
 * and one-tap restore action.
 */
@Composable
fun UndoSnackbarListener(
    lastDeletedIdea: Idea?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    hapticManager: HapticManager? = null
) {
    LaunchedEffect(lastDeletedIdea) {
        val deleted = lastDeletedIdea ?: return@LaunchedEffect
        hapticManager?.performHeavyClick()

        val job = launch {
            val result = snackbarHostState.showSnackbar(
                message = "\"${deleted.name}\" deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                hapticManager?.performSuccess()
                onUndo()
            } else {
                onDismiss()
            }
        }

        // Enforce exact 5-second window
        delay(5000)
        snackbarHostState.currentSnackbarData?.dismiss()
        job.cancel()
        onDismiss()
    }
}
