package com.bsbchurch.voznote.record

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.media.MediaRecorder

/**
 * Gerencia a gravação de áudio (MediaRecorder) e o SpeechRecognizer para transcrição.
 * Comentários e nomes em PT-BR para facilitar entendimento.
 */
class GravadorManager(private val contexto: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var listening = false
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    // Live callback simples via lambda
    // segundo parâmetro indica se é um resultado final (true) ou parcial (false)
    var onTranscricao: ((String, Boolean) -> Unit)? = null
    var onErro: ((String) -> Unit)? = null

    /**
     * Inicia apenas o SpeechRecognizer para obter transcrição em tempo real.
     */
    fun iniciarGravacao() {
        try {
            iniciarSpeechRecognizer()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao iniciar reconhecimento de voz")
            onErro?.invoke("Erro ao iniciar reconhecimento: ${e.message}")
        }
    }

    fun pausarGravacao() {
        try {
            pararSpeechRecognizer()
            Timber.i("Captura pausada")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao pausar captura")
            onErro?.invoke("Erro ao pausar: ${e.message}")
        }
    }

    fun retomarGravacao() {
        try {
            iniciarSpeechRecognizer()
            Timber.i("Captura retomada")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao retomar captura")
            onErro?.invoke("Erro ao retomar: ${e.message}")
        }
    }

    /**
     * Para o reconhecimento e retorna null (não há arquivo de áudio).
     */
    fun pararESalvar(): String? {
        try {
            pararSpeechRecognizer()
            Timber.i("Captura parada (texto salvo pela camada superior)")
            return null
        } catch (e: Exception) {
            Timber.e(e, "Erro ao parar captura")
            onErro?.invoke("Erro ao parar: ${e.message}")
            return null
        }
    }

    fun cancelarGravacao() {
        try {
            pararSpeechRecognizer()
            Timber.i("Captura cancelada")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao cancelar captura")
            onErro?.invoke("Erro ao cancelar: ${e.message}")
        }
    }

    // ===== SpeechRecognizer (transcrição) =====
    private fun iniciarSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(contexto)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(contexto)
                val listener = object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Timber.d("SpeechRecognizer pronto")
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        Timber.d("SpeechRecognizer fim do áudio")
                    }

                    override fun onError(error: Int) {
                        Timber.w("SpeechRecognizer erro: %d", error)
                        // Reinicia escuta para manter transcrição contínua
                        if (listening) {
                            scope.launch {
                                iniciarListening()
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val t = texts?.joinToString(" ") ?: ""
                        Timber.d("SpeechRecognizer resultados: %s", t)
                        if (t.isNotBlank()) onTranscricao?.invoke(t, true)
                        // Continuar escutando
                        if (listening) iniciarListening()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val texts = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val t = texts?.joinToString(" ") ?: ""
                        if (t.isNotBlank()) onTranscricao?.invoke(t, false)
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                }
                speechRecognizer?.setRecognitionListener(listener)
                listening = true
                iniciarListening()
            } else {
                onErro?.invoke("Reconhecimento de voz não disponível neste dispositivo")
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao iniciar SpeechRecognizer")
            onErro?.invoke("Erro SpeechRecognizer: ${e.message}")
        }
    }

    private fun iniciarListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao iniciar listening")
        }
    }

    private fun pararSpeechRecognizer() {
        try {
            listening = false
            speechRecognizer?.apply {
                stopListening()
                cancel()
                destroy()
            }
            speechRecognizer = null
            Timber.i("SpeechRecognizer parado")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao parar SpeechRecognizer")
        }
    }
}
