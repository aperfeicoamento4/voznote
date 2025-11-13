package com.bsbchurch.voznote.notas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bsbchurch.voznote.databinding.ActivityConfiguracoesBinding
import timber.log.Timber

/**
 * Tela de configurações simples (placeholder)
 */
class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Configurações"
        Timber.i("ConfiguracoesActivity criada")
        binding.textConfig.text = "Opções de configuração serão adicionadas aqui." 
    }
}
