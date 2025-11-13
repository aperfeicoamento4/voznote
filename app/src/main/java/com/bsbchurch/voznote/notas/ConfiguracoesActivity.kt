package com.bsbchurch.voznote.notas

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bsbchurch.voznote.databinding.ActivityConfiguracoesBinding
import timber.log.Timber

/**
 * Tela de configurações com toggle para bordas nas caixas de nota
 */
class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    private val PREFS = "voznote_prefs"
    private val KEY_BORDAS = "pref_bordas"
    private val KEY_ARREDONDAMENTO = "pref_arredondamento"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Configurações"
        Timber.i("ConfiguracoesActivity criada")

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val habilitado = prefs.getBoolean(KEY_BORDAS, false)

        // ligar switch via view binding
        try {
            binding.switchBordas.isChecked = habilitado
            binding.switchBordas.setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(KEY_BORDAS, checked).apply()
            }
        } catch (e: Exception) {
            Timber.w(e, "switchBordas não encontrado no layout; usando fallback text")
            binding.textConfig.text = "Opções: Bordas nas caixas de notas (toggle)."
        }

        // Arredondamento: por padrão desabilitado
        try {
            val arred = prefs.getBoolean(KEY_ARREDONDAMENTO, false)
            binding.switchArredondamento.isChecked = arred
            binding.switchArredondamento.setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(KEY_ARREDONDAMENTO, checked).apply()
            }
        } catch (e: Exception) {
            Timber.w(e, "switchArredondamento não encontrado no layout; ignorando")
        }
    }
}
