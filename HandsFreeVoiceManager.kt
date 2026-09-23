package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class HandsFreeVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Main)

    // TTS
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _spokenResponseText = MutableStateFlow("")
    val spokenResponseText: StateFlow<String> = _spokenResponseText.asStateFlow()

    // STT
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready for voice commands")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var onSpeechResultCallback: ((String) -> Unit)? = null

    init {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("HandsFreeVoiceManager", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.getDefault())
            }
            textToSpeech?.setSpeechRate(1.0f)
            textToSpeech?.setPitch(1.0f)

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    scope.launch { _isSpeaking.value = true }
                }

                override fun onDone(utteranceId: String?) {
                    scope.launch { _isSpeaking.value = false }
                }

                override fun onError(utteranceId: String?) {
                    scope.launch { _isSpeaking.value = false }
                }
            })
            isTtsInitialized = true
        } else {
            Log.e("HandsFreeVoiceManager", "TTS initialization failed status: $status")
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (text.isBlank()) return
        _spokenResponseText.value = text
        if (!isTtsInitialized || textToSpeech == null) {
            _statusMessage.value = "TTS not available on this device"
            onComplete?.invoke()
            return
        }

        stopSpeaking()
        _isSpeaking.value = true
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "OmniFlowUtterance_${System.currentTimeMillis()}")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "OmniFlowUtterance")
    }

    fun stopSpeaking() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
        } catch (e: Exception) {
            Log.e("HandsFreeVoiceManager", "Error stopping TTS", e)
        }
        _isSpeaking.value = false
    }

    fun startListening(onResult: (String) -> Unit) {
        this.onSpeechResultCallback = onResult
        _transcription.value = ""
        _soundLevel.value = 0f

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _statusMessage.value = "Speech recognition service not found. Use command presets or quick text."
            _isListening.value = false
            return
        }

        try {
            stopListening()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _statusMessage.value = "Listening... Speak now"
                    }

                    override fun onBeginningOfSpeech() {
                        _statusMessage.value = "Hearing your voice..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB typically [-2 to 10] into 0.0 .. 1.0 range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                        _soundLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _statusMessage.value = "Processing command..."
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _soundLevel.value = 0f
                        val errDesc = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again or tap a preset."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap mic to speak again."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Could not process speech. You can select a command preset below."
                        }
                        _statusMessage.value = errDesc
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _soundLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull() ?: ""
                        if (spoken.isNotBlank()) {
                            _transcription.value = spoken
                            _statusMessage.value = "Understood: \"$spoken\""
                            onSpeechResultCallback?.invoke(spoken)
                        } else {
                            _statusMessage.value = "No speech recognized."
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let { partial ->
                            _transcription.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("HandsFreeVoiceManager", "Error starting speech recognition", e)
            _isListening.value = false
            _statusMessage.value = "Speech recognition error. Use presets or type command."
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("HandsFreeVoiceManager", "Error stopping recognizer", e)
        }
        speechRecognizer = null
        _isListening.value = false
        _soundLevel.value = 0f
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        try {
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("HandsFreeVoiceManager", "Error shutting down TTS", e)
        }
        textToSpeech = null
        isTtsInitialized = false
    }
}
