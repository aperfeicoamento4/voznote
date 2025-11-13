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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Configurações"
        Timber.i("ConfiguracoesActivity criada")

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val habilitado = prefs.getBoolean(KEY_BORDAS, false)

        // se existir switch no layout, ligar ao pref
        val sw = try { findViewById<SwitchCompat>(R.id.switchBordas) } catch (e: Exception) { null }
        if (sw != null) {
            sw.isChecked = habilitado
            sw.setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(KEY_BORDAS, checked).apply()
            }
        } else {
            // fallback: mostrar mensagem
            binding.textConfig.text = "Opções: Bordas nas caixas de notas (toggle)." 
        }
    }
}
