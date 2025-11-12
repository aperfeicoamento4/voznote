package com.bsbchurch.voznote.notas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bsbchurch.voznote.R
import com.bsbchurch.voznote.data.Nota
import com.bsbchurch.voznote.data.NotaRepository
import com.bsbchurch.voznote.databinding.ActivityNotasBinding
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent as AndroidIntent
import com.bsbchurch.voznote.receiver.AlarmeReceiver

/**
 * Tela de Notas com RecyclerView e ItemTouchHelper para reordenar
 */
class NotasActivity : AppCompatActivity(), NotasAdapter.Callback {

    private lateinit var binding: ActivityNotasBinding
    private lateinit var adapter: NotasAdapter
    private lateinit var repo: NotaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Minhas Notas"
        Timber.i("NotasActivity criada")

        repo = NotaRepository.obter(this)

        adapter = NotasAdapter(this, mutableListOf(), this)
        binding.recyclerNotas.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotas.adapter = adapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.moveItem(from, to)
                // Atualizar ordem no banco (simples: salvar ordens atuais)
                salvarOrdemNoBanco()
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        touchHelper.attachToRecyclerView(binding.recyclerNotas)

        binding.fabRec.setOnClickListener {
            startActivity(Intent(this, com.bsbchurch.voznote.GravacaoActivity::class.java))
        }

        carregarNotas()
    }

    private fun carregarNotas() {
        lifecycleScope.launch {
            val notas = repo.listarTodas()
            adapter.updateList(notas)
        }
    }

    private fun salvarOrdemNoBanco() {
        lifecycleScope.launch {
            // Ler lista atual do adapter e persistir a ordem
            val atual = adapter.getItems()
            for (i in atual.indices) {
                val nota = atual[i]
                repo.atualizarOrdem(nota.id, i)
            }
        }
    }

    // ==== Callbacks do Adapter ====
    override fun onEditar(nota: Nota) {
        val editText = EditText(this)
        editText.setText(nota.texto)
        AlertDialog.Builder(this)
            .setTitle("Editar nota")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val texto = editText.text.toString().trim()
                if (texto.isBlank()) {
                    Toast.makeText(this, "Texto vazio. Não salvo.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                nota.texto = texto
                lifecycleScope.launch { repo.atualizar(nota); carregarNotas() }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onExcluir(nota: Nota) {
        AlertDialog.Builder(this)
            .setTitle("Excluir nota")
            .setMessage("Confirma exclusão dessa nota?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch { repo.deletar(nota); carregarNotas() }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onConfigurarAlarme(nota: Nota) {
        // Escolher data
        val hoje = Calendar.getInstance()
        DatePickerDialog(this, { _, ano, mes, dia ->
            TimePickerDialog(this, { _, hora, minuto ->
                val cal = Calendar.getInstance()
                cal.set(ano, mes, dia, hora, minuto, 0)
                val millis = cal.timeInMillis
                // Escolher recorrência simples
                val opcoes = arrayOf("Nenhuma", "Diária", "Semanal")
                AlertDialog.Builder(this)
                    .setTitle("Recorrência")
                    .setItems(opcoes) { _, idx ->
                        val recorrencia = when (idx) {
                            1 -> "DAILY"
                            2 -> "WEEKLY"
                            else -> "NONE"
                        }
                        nota.alarmeMillis = millis
                        nota.alarmeRecorrencia = recorrencia
                        lifecycleScope.launch {
                            repo.atualizar(nota)
                            agendarAlarme(nota)
                            carregarNotas()
                        }
                    }
                    .show()
            }, hoje.get(Calendar.HOUR_OF_DAY), hoje.get(Calendar.MINUTE), true).show()
        }, hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun agendarAlarme(nota: Nota) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = AndroidIntent(this, AlarmeReceiver::class.java).apply {
            putExtra("notaId", nota.id)
            putExtra("texto", nota.texto)
        }
        val pending = PendingIntent.getBroadcast(this, nota.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        nota.alarmeMillis?.let { millis ->
            when (nota.alarmeRecorrencia) {
                "DAILY" -> am.setInexactRepeating(AlarmManager.RTC_WAKEUP, millis, AlarmManager.INTERVAL_DAY, pending)
                "WEEKLY" -> am.setInexactRepeating(AlarmManager.RTC_WAKEUP, millis, AlarmManager.INTERVAL_DAY * 7, pending)
                else -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
                    } else {
                        am.set(AlarmManager.RTC_WAKEUP, millis, pending)
                    }
                }
            }
            Toast.makeText(this, "Alarme agendado", Toast.LENGTH_SHORT).show()
        }
    }

}
