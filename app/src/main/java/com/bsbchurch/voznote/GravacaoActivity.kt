package com.bsbchurch.voznote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bsbchurch.voznote.databinding.ActivityGravacaoBinding
import com.bsbchurch.voznote.record.GravacaoViewModel
import timber.log.Timber

/**
 * Activity principal: Tela de Gravação.
 * Implementa gravação de áudio e transcrição em tempo real.
 */
class GravacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGravacaoBinding
    private val viewModel: GravacaoViewModel by viewModels()

    // Gerenciador de permissão para gravar áudio
    private val requisitarPermissao = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            Timber.i("Permissão de microfone concedida")
            inicializarDepoisPermissao()
        } else {
            Timber.w("Permissão de microfone negada")
            Toast.makeText(this, "Permissão de microfone necessária", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGravacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Gravação"

        Timber.i("GravacaoActivity criada")

        // Observadores
        viewModel.transcricao.observe(this, Observer { texto ->
            binding.textoTranscricao.text = texto
        })

        // Verifica permissão e solicita se necessário
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requisitarPermissao.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            inicializarDepoisPermissao()
        }

        // Ações dos FABs
        binding.fabPauseRec.setOnClickListener {
            try {
                val gravando = viewModel.gravando.value ?: false
                val pausado = viewModel.pausado.value ?: false
                if (!gravando) {
                    // iniciar gravação
                    viewModel.iniciarManager(this)
                    viewModel.iniciarGravacao()
                    binding.fabPauseRec.setImageResource(android.R.drawable.ic_media_pause)
                    Toast.makeText(this, "Gravação iniciada", Toast.LENGTH_SHORT).show()
                    Timber.i("Usuário iniciou gravação")
                } else {
                    if (!pausado) {
                        viewModel.pausar()
                        binding.fabPauseRec.setImageResource(android.R.drawable.ic_media_play)
                        Toast.makeText(this, "Gravação pausada", Toast.LENGTH_SHORT).show()
                        Timber.i("Usuário pausou gravação")
                    } else {
                        viewModel.retomar()
                        binding.fabPauseRec.setImageResource(android.R.drawable.ic_media_pause)
                        Toast.makeText(this, "Gravação retomada", Toast.LENGTH_SHORT).show()
                        Timber.i("Usuário retomou gravação")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro no botão Pause/Rec")
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.fabStop.setOnClickListener {
            try {
                val caminho = viewModel.pararESalvar()
                if (caminho != null) {
                    Toast.makeText(this, "Gravação salva: $caminho", Toast.LENGTH_LONG).show()
                    Timber.i("Gravação salva em: %s", caminho)
                    // Aqui deve-se criar a Nota no banco Room. Por ora, apenas navega para Notas.
                } else {
                    Toast.makeText(this, "Nenhum arquivo para salvar", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this, NotasActivity::class.java))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao parar gravação")
                Toast.makeText(this, "Erro ao parar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.fabCancel.setOnClickListener {
            try {
                viewModel.cancelar()
                Toast.makeText(this, "Gravação cancelada", Toast.LENGTH_SHORT).show()
                Timber.i("Usuário cancelou gravação")
                startActivity(Intent(this, NotasActivity::class.java))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao cancelar gravação")
                Toast.makeText(this, "Erro ao cancelar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.fabNotas.setOnClickListener {
            startActivity(Intent(this, NotasActivity::class.java))
        }
    }

    private fun inicializarDepoisPermissao() {
        // Inicializa o manager no ViewModel para resolver recursos dependentes do contexto
        viewModel.iniciarManager(this)
        binding.textoTranscricao.text = "Transcrição em tempo real aparecerá aqui..."
    }
}
