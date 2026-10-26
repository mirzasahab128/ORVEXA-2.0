package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.Locale

enum class VoiceTargetField {
    NONE,
    TITLE,
    DESCRIPTION
}

class VoiceRecognitionState {
    var isListening by mutableStateOf(false)
    var activeField by mutableStateOf(VoiceTargetField.NONE)
    var rmsLevel by mutableFloatStateOf(0f)
    var statusMessage by mutableStateOf<String?>(null)
}

@Composable
fun rememberVoiceRecognition(
    onTranscriptionResult: (field: VoiceTargetField, text: String, isPartial: Boolean) -> Unit
): VoiceRecognitionController {
    val context = LocalContext.current
    val state = remember { VoiceRecognitionState() }

    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun stopListening() {
        try {
            recognizer?.stopListening()
            recognizer?.destroy()
        } catch (_: Exception) {}
        recognizer = null
        state.isListening = false
        state.activeField = VoiceTargetField.NONE
        state.rmsLevel = 0f
        state.statusMessage = null
    }

    var basePrefix by remember { mutableStateOf("") }

    val startListeningForField = { target: VoiceTargetField, initialText: String ->
        stopListening()
        state.activeField = target
        basePrefix = initialText.trimEnd()
        state.statusMessage = "Listening... Speak your thoughts clearly"

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val sr = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer = sr

            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    state.isListening = true
                    state.statusMessage = "Listening..."
                }

                override fun onBeginningOfSpeech() {
                    state.statusMessage = "Transcribing..."
                }

                override fun onRmsChanged(rmsdB: Float) {
                    state.rmsLevel = rmsdB
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    state.statusMessage = "Processing speech..."
                }

                override fun onError(error: Int) {
                    state.statusMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Tap mic to try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out."
                        SpeechRecognizer.ERROR_NETWORK -> "Network error in speech service."
                        else -> "Speech recognition ended."
                    }
                    state.isListening = false
                    state.activeField = VoiceTargetField.NONE
                    state.rmsLevel = 0f
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0].trim()
                        if (spokenText.isNotBlank()) {
                            val formatted = spokenText.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            val fullText = if (basePrefix.isBlank()) formatted else "$basePrefix $formatted"
                            onTranscriptionResult(target, fullText, false)
                        }
                    }
                    stopListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val partial = matches[0].trim()
                        if (partial.isNotBlank()) {
                            val formatted = partial.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            val fullText = if (basePrefix.isBlank()) formatted else "$basePrefix $formatted"
                            onTranscriptionResult(target, fullText, true)
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate your thesis or product idea...")
            }

            try {
                sr.startListening(intent)
                state.isListening = true
            } catch (e: Exception) {
                state.statusMessage = "Unable to start microphone: ${e.message}"
                stopListening()
            }
        } else {
            state.statusMessage = "Speech recognition service not available on this device."
        }
    }

    var pendingTarget by remember { mutableStateOf<VoiceTargetField?>(null) }
    var pendingPrefix by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingTarget?.let { startListeningForField(it, pendingPrefix) }
        } else {
            state.statusMessage = "Microphone permission required for voice-to-text dictation."
            state.isListening = false
            state.activeField = VoiceTargetField.NONE
        }
        pendingTarget = null
        pendingPrefix = ""
    }

    DisposableEffect(Unit) {
        onDispose {
            stopListening()
        }
    }

    return remember(state) {
        VoiceRecognitionController(
            state = state,
            onStartListening = { target, currentText ->
                if (state.isListening && state.activeField == target) {
                    stopListening()
                } else {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        startListeningForField(target, currentText)
                    } else {
                        pendingTarget = target
                        pendingPrefix = currentText
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            onStopListening = { stopListening() }
        )
    }
}

class VoiceRecognitionController(
    val state: VoiceRecognitionState,
    val onStartListening: (VoiceTargetField, String) -> Unit,
    val onStopListening: () -> Unit
) {
    fun onStartListening(target: VoiceTargetField) {
        onStartListening(target, "")
    }
}

@Composable
fun VoiceInputIconButton(
    controller: VoiceRecognitionController,
    targetField: VoiceTargetField,
    currentText: String = "",
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF00E5FF)
) {
    val isListening = controller.state.isListening && controller.state.activeField == targetField
    androidx.compose.material3.IconButton(
        onClick = {
            if (isListening) {
                controller.onStopListening()
            } else {
                controller.onStartListening(targetField, currentText)
            }
        },
        modifier = modifier
    ) {
        androidx.compose.material3.Icon(
            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.MicNone,
            contentDescription = if (isListening) "Stop voice input" else "Start voice input",
            tint = if (isListening) androidx.compose.ui.graphics.Color(0xFFEF4444) else tint
        )
    }
}
