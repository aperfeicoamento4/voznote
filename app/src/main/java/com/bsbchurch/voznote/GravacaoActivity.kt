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
import com.bsbchurch.voznote.notas.NotasActivity
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
        // Ajusta ícones iniciais
        binding.fabPauseRec.setImageResource(R.drawable.ic_pause)
        binding.fabStop.setImageResource(R.drawable.ic_stop)
        binding.fabCancel.setImageResource(R.drawable.ic_close)

        binding.fabPauseRec.setOnClickListener {
            try {
                val gravando = viewModel.gravando.value ?: false
                val pausado = viewModel.pausado.value ?: false
                if (!gravando) {
                    // iniciar gravação
                    viewModel.iniciarManager(this)
                    viewModel.iniciarGravacao()
                    binding.fabPauseRec.setImageResource(R.drawable.ic_pause)
                    Toast.makeText(this, "Captação iniciada", Toast.LENGTH_SHORT).show()
                    Timber.i("Usuário iniciou captação")
                } else {
                    if (!pausado) {
                        viewModel.pausar()
                        binding.fabPauseRec.setImageResource(R.drawable.ic_mic)
                        Toast.makeText(this, "Captação pausada", Toast.LENGTH_SHORT).show()
                        Timber.i("Usuário pausou captação")
                    } else {
                        viewModel.retomar()
                        binding.fabPauseRec.setImageResource(R.drawable.ic_pause)
                        Toast.makeText(this, "Captação retomada", Toast.LENGTH_SHORT).show()
                        Timber.i("Usuário retomou captação")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro no botão Pause/Rec")
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.fabStop.setOnClickListener {
            try {
                val salvo = viewModel.pararESalvar()
                if (salvo) {
                    Toast.makeText(this, "Nota salva", Toast.LENGTH_LONG).show()
                    Timber.i("Nota de texto salva")
                } else {
                    Toast.makeText(this, "Nenhuma transcrição para salvar", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this, NotasActivity::class.java))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao parar captação")
                Toast.makeText(this, "Erro ao parar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.fabCancel.setOnClickListener {
            try {
                viewModel.cancelar()
                // limpar texto exibido
                Toast.makeText(this, "Captação cancelada", Toast.LENGTH_SHORT).show()
                Timber.i("Usuário cancelou captação")
                startActivity(Intent(this, NotasActivity::class.java))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao cancelar captação")
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
        // Iniciar captura automaticamente ao abrir
        viewModel.iniciarGravacao()
        binding.fabPauseRec.setImageResource(R.drawable.ic_pause)
    }
}
