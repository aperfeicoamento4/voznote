package com.bsbchurch.voznote.notas

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bsbchurch.voznote.data.Nota
import com.bsbchurch.voznote.data.NotaRepository
import com.bsbchurch.voznote.databinding.ActivityNotaDetalheBinding
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Activity que exibe e permite editar a nota em tela cheia.
 */
class NotaDetalheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotaDetalheBinding
    private var notaId: Int = 0
    private var notaAtual: Nota? = null
    private lateinit var repo: NotaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaDetalheBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Nota"
        repo = NotaRepository.obter(this)

        notaId = intent.getIntExtra("notaId", 0)

        binding.editTextoNota.isFocusable = false
        binding.editTextoNota.isFocusableInTouchMode = false

        binding.editTextoNota.setOnClickListener {
            // Habilitar edição e abrir teclado
            binding.editTextoNota.isFocusableInTouchMode = true
            binding.editTextoNota.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editTextoNota, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.fabSalvarNota.setOnClickListener {
            salvarEdicao()
        }

        carregarNota()
    }

    private fun carregarNota() {
        lifecycleScope.launch {
            try {
                val n = repo.buscarPorId(notaId)
                notaAtual = n
                if (n != null) {
                    binding.editTextoNota.setText(n.texto)
                    binding.textoData.text = java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date(n.dataHora))
                } else {
                    Toast.makeText(this@NotaDetalheActivity, "Nota não encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar nota")
                Toast.makeText(this@NotaDetalheActivity, "Erro ao carregar nota", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun salvarEdicao() {
        val texto = binding.editTextoNota.text.toString().trim()
        if (texto.isBlank()) {
            Toast.makeText(this, "Texto vazio. Não salvo.", Toast.LENGTH_SHORT).show()
            return
        }
        val n = notaAtual
        if (n != null) {
            n.texto = texto
            lifecycleScope.launch {
                try {
                    repo.atualizar(n)
                    Toast.makeText(this@NotaDetalheActivity, "Nota salva", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Timber.e(e, "Erro ao salvar nota")
                    Toast.makeText(this@NotaDetalheActivity, "Erro ao salvar nota", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
