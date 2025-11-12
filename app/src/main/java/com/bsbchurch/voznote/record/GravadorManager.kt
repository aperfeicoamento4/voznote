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

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var listening = false
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    // Live callback simples via lambda
    var onTranscricao: ((String) -> Unit)? = null
    var onErro: ((String) -> Unit)? = null

    // Gera caminho de arquivo para salvar a gravação
    private fun gerarArquivoAudio(): String {
        val pasta = contexto.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: contexto.filesDir
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val nome = "voznote_${sdf.format(Date())}.m4a"
        return File(pasta, nome).absolutePath
    }

    fun iniciarGravacao() {
        try {
            outputFile = gerarArquivoAudio()
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(192000)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            Timber.i("Gravação iniciada: %s", outputFile)
            iniciarSpeechRecognizer()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao iniciar gravação")
            onErro?.invoke("Erro ao iniciar gravação: ${e.message}")
        }
    }

    fun pausarGravacao() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                Timber.i("Gravação pausada")
            } else {
                Timber.w("Paussa não suportada nesta API")
                onErro?.invoke("Pausa não suportada nesta versão do Android")
            }
            pararSpeechRecognizer()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao pausar gravação")
            onErro?.invoke("Erro ao pausar: ${e.message}")
        }
    }

    fun retomarGravacao() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                Timber.i("Gravação retomada")
            } else {
                Timber.w("Resume não suportado nesta API")
                onErro?.invoke("Retomar não suportado nesta versão do Android")
            }
            iniciarSpeechRecognizer()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao retomar gravação")
            onErro?.invoke("Erro ao retomar: ${e.message}")
        }
    }

    fun pararESalvar(): String? {
        try {
            pararSpeechRecognizer()
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Timber.w(e, "stop() lançou exceção, ignorando")
                }
                reset()
                release()
            }
            mediaRecorder = null
            Timber.i("Gravação parada e salva: %s", outputFile)
            return outputFile
        } catch (e: Exception) {
            Timber.e(e, "Erro ao parar e salvar")
            onErro?.invoke("Erro ao parar gravação: ${e.message}")
            return null
        }
    }

    fun cancelarGravacao() {
        try {
            pararSpeechRecognizer()
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception){}
                reset()
                release()
            }
            mediaRecorder = null
            outputFile?.let {
                try { File(it).delete() } catch (_: Exception){}
            }
            outputFile = null
            Timber.i("Gravação cancelada e arquivo excluído se existia")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao cancelar gravação")
            onErro?.invoke("Erro ao cancelar gravação: ${e.message}")
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
                        if (t.isNotBlank()) onTranscricao?.invoke(t)
                        // Continuar escutando
                        if (listening) iniciarListening()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val texts = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val t = texts?.joinToString(" ") ?: ""
                        if (t.isNotBlank()) onTranscricao?.invoke(t)
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
