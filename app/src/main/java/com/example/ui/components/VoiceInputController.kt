package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Robust Speech-to-Text Controller that prevents word duplication across partial
 * and final transcriptions, disables runaway auto-retries, and ensures clean state transitions.
 */
class RobustVoiceInputController(
    private val context: Context,
    private val onTextTranscribed: (field: VoiceTargetField, text: String, isFinal: Boolean) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening by mutableStateOf(false)
        private set
    var activeField by mutableStateOf<VoiceTargetField?>(null)
        private set

    private var basePrefix: String = ""
    private var lastEmittedHypothesis: String = ""

    fun startListening(target: VoiceTargetField, currentInitialText: String) {
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("VoiceInput", "Speech recognition unavailable on this device")
            return
        }

        basePrefix = currentInitialText.trim()
        lastEmittedHypothesis = ""
        activeField = target

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }

                override fun onBeginningOfSpeech() { }

                override fun onRmsChanged(rmsdB: Float) { }

                override fun onBufferReceived(buffer: ByteArray?) { }

                override fun onEndOfSpeech() {
                    isListening = false
                }

                override fun onError(error: Int) {
                    Log.d("VoiceInput", "Speech recognition stopped with code: $error. No auto-retry.")
                    // Clean state reset without auto-retry to prevent word loops
                    cleanup()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()?.trim().orEmpty()
                    if (spoken.isNotEmpty()) {
                        val fullText = if (basePrefix.isEmpty()) spoken else "$basePrefix $spoken"
                        onTextTranscribed(target, fullText, true)
                    }
                    cleanup()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partialSpoken = matches?.firstOrNull()?.trim().orEmpty()
                    if (partialSpoken.isNotEmpty() && partialSpoken != lastEmittedHypothesis) {
                        lastEmittedHypothesis = partialSpoken
                        val fullText = if (basePrefix.isEmpty()) partialSpoken else "$basePrefix $partialSpoken"
                        onTextTranscribed(target, fullText, false)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) { }
            })

            recognizer.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("VoiceInput", "Error starting speech recognition", e)
            cleanup()
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) { }
        cleanup()
    }

    private fun cleanup() {
        isListening = false
        activeField = null
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) { }
        speechRecognizer = null
    }

    fun release() {
        cleanup()
    }
}

@Composable
fun rememberRobustVoiceInput(
    onTranscription: (field: VoiceTargetField, text: String, isFinal: Boolean) -> Unit
): RobustVoiceInputController {
    val context = LocalContext.current
    val controller = remember(context) {
        RobustVoiceInputController(context, onTranscription)
    }

    DisposableEffect(controller) {
        onDispose {
            controller.release()
        }
    }

    return controller
}
