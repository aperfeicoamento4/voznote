package com.bsbchurch.voznote.record

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsbchurch.voznote.data.Nota
import com.bsbchurch.voznote.data.NotaRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

/**
 * ViewModel que expõe estados da gravação e transcrição.
 * Agora salva automaticamente uma Nota no Room ao parar a gravação.
 */
class GravacaoViewModel : ViewModel() {

    private val _transcricao = MutableLiveData<String>()
    val transcricao: LiveData<String> = _transcricao

    private val _gravando = MutableLiveData(false)
    val gravando: LiveData<Boolean> = _gravando

    private val _pausado = MutableLiveData(false)
    val pausado: LiveData<Boolean> = _pausado

    private var manager: GravadorManager? = null
    private var appContext: Context? = null

    fun iniciarManager(context: Context) {
        if (manager == null) {
            manager = GravadorManager(context.applicationContext)
            appContext = context.applicationContext
            manager?.onTranscricao = { texto ->
                Timber.d("ViewModel recebeu transcrição: %s", texto)
                _transcricao.postValue(texto)
            }
            manager?.onErro = { erro ->
                Timber.w("Erro no GravadorManager: %s", erro)
            }
        }
    }

    fun iniciarGravacao() {
        manager?.iniciarGravacao()
        _gravando.value = true
        _pausado.value = false
    }

    fun pausar() {
        manager?.pausarGravacao()
        _pausado.value = true
    }

    fun retomar() {
        manager?.retomarGravacao()
        _pausado.value = false
    }

    fun pararESalvar(): String? {
        val caminho = manager?.pararESalvar()
        _gravando.value = false
        _pausado.value = false

        // Salvar nota em background se houver texto ou arquivo
        val textoAtual = _transcricao.value?.trim() ?: ""
        if (textoAtual.isNotBlank() || caminho != null) {
            val nota = Nota(
                texto = textoAtual.ifBlank { "Nota de voz" },
                caminhoAudio = caminho,
                dataHora = System.currentTimeMillis(),
                corFundo = gerarCorPastel(),
                ordem = 0
            )
            try {
                val ctx = appContext
                if (ctx != null) {
                    viewModelScope.launch {
                        try {
                            val repo = NotaRepository.obter(ctx)
                            val id = repo.inserir(nota)
                            Timber.i("Nota inserida com id %s", id)
                        } catch (e: Exception) {
                            Timber.e(e, "Erro ao inserir nota")
                        }
                    }
                } else {
                    Timber.w("Contexto da app não disponível para salvar nota")
                }
            } catch (e: Exception) {
                Timber.e(e, "Falha ao agendar persistência da nota")
            }
        }

        return caminho
    }

    fun cancelar() {
        manager?.cancelarGravacao()
        _gravando.value = false
        _pausado.value = false
    }

    override fun onCleared() {
        super.onCleared()
        manager?.cancelarGravacao()
    }

    private fun gerarCorPastel(): Int {
        val h = Random.nextInt(0, 360).toFloat()
        val s = 0.3f + Random.nextFloat() * 0.2f
        val v = 0.95f
        val hsv = floatArrayOf(h, s, v)
        return Color.HSVToColor(hsv)
    }
}
