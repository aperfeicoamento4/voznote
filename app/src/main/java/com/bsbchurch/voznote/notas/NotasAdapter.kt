package com.bsbchurch.voznote.notas

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bsbchurch.voznote.R
import com.bsbchurch.voznote.data.Nota
import timber.log.Timber

/** Adapter simples para a lista de notas com menu de opções */
class NotasAdapter(
    private val contexto: Context,
    private val lista: MutableList<Nota>,
    private val listener: Callback
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>() {

    interface Callback {
        fun onEditar(nota: Nota)
        fun onExcluir(nota: Nota)
        fun onAbrir(nota: Nota)
        fun onConfigurarAlarme(nota: Nota)
    }

    inner class NotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val texto: TextView = view.findViewById(R.id.textoNota)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenu)
        val alarmInfo: TextView = view.findViewById(R.id.alarmInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_nota_full, parent, false)
        return NotaViewHolder(v)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = lista[position]
        holder.texto.text = nota.texto
        try {
            holder.itemView.setBackgroundColor(nota.corFundo)
        } catch (e: Exception) {
            Timber.w(e, "Erro ao aplicar cor de fundo")
        }

        // Mostrar info de alarme se presente
        if (nota.alarmeMillis != null && nota.alarmeMillis!! > 0) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = nota.alarmeMillis!!
            val txt = String.format(java.util.Locale.getDefault(), "🔔 %1\$tF %1\$tR", cal)
            holder.alarmInfo.text = txt
            holder.alarmInfo.visibility = View.VISIBLE
        } else {
            holder.alarmInfo.visibility = View.GONE
        }

        holder.btnMenu.setOnClickListener { v ->
            val popup = PopupMenu(contexto, v)
            popup.menu.add("Editar")
            popup.menu.add("Excluir")
            popup.menu.add("Configurar Alarme")
            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.title.toString()) {
                    "Editar" -> listener.onEditar(nota)
                    "Excluir" -> listener.onExcluir(nota)
                    "Configurar Alarme" -> listener.onConfigurarAlarme(nota)
                }
                true
            }
            popup.show()
        }
        // Clique no item abre a nota em tela cheia
        holder.itemView.setOnClickListener {
            Timber.d("Nota clicada: %s", nota.id)
            listener.onAbrir(nota)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun moveItem(from: Int, to: Int) {
        val item = lista.removeAt(from)
        lista.add(to, item)
        notifyItemMoved(from, to)
    }

    fun removeAt(pos: Int) {
        lista.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun updateList(nova: List<Nota>) {
        lista.clear()
        lista.addAll(nova)
        notifyDataSetChanged()
    }

    fun getItems(): List<Nota> = lista
}
