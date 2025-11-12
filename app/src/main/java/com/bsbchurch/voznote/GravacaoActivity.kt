package com.bsbchurch.voznote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import timber.log.Timber
import com.bsbchurch.voznote.databinding.ActivityGravacaoBinding

/**
 * Activity principal: Tela de Gravação.
 * Ao abrir, tenta iniciar a captura de áudio (microfone ligado).
 * Esta classe tem apenas um esqueleto nesta etapa; gravação e STT serão implementados na próxima etapa.
 */
class GravacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGravacaoBinding

    // Gerenciador de permissão para gravar áudio
    private val requisitarPermissao = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            Timber.i("Permissão de microfone concedida")
            iniciarRecursosDeAudio()
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

        // Verifica permissão e solicita se necessário
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requisitarPermissao.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            iniciarRecursosDeAudio()
        }

        // Botão para abrir tela de notas (apenas esqueleto por enquanto)
        binding.fabNotas.setOnClickListener {
            startActivity(Intent(this, NotasActivity::class.java))
        }
    }

    private fun iniciarRecursosDeAudio() {
        // Aqui iniciaremos a gravação e o SpeechRecognizer na próxima etapa
        Timber.i("Inicializando recursos de áudio (esqueleto)")
        binding.textoTranscricao.text = "Transcrição em tempo real aparecerá aqui..."
    }
}
