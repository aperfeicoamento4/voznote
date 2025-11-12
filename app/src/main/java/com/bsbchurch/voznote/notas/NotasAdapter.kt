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
        fun onConfigurarAlarme(nota: Nota)
    }

    inner class NotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val texto: TextView = view.findViewById(R.id.textoNota)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenu)
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
