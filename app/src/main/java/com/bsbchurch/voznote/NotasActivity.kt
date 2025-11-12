package com.bsbchurch.voznote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import com.bsbchurch.voznote.databinding.ActivityNotasBinding

/**
 * Tela de Notas (esqueleto nesta etapa).
 * Implementaremos RecyclerView, Room e ItemTouchHelper nas próximas etapas.
 */
class NotasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Minhas Notas"
        Timber.i("NotasActivity criada (esqueleto)")
    }
}
