package com.bsbchurch.voznote.notas

import android.content.Context
import android.view.LayoutInflater
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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

        // Mostrar info de alarme se presente (formato dd-MM-yyyy HH:mm)
        if (nota.alarmeMillis != null && nota.alarmeMillis!! > 0) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = nota.alarmeMillis!!
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
            val txt = "🔔 ${sdf.format(java.util.Date(cal.timeInMillis))}"
            holder.alarmInfo.text = txt
            holder.alarmInfo.visibility = View.VISIBLE
        } else {
            holder.alarmInfo.visibility = View.GONE
        }

        holder.btnMenu.setOnClickListener { v ->
            val popup = PopupMenu(contexto, v)
            // Removido item "Editar" conforme pedido do usuário
            popup.menu.add("Excluir")
            popup.menu.add("Configurar Alarme")
            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.title.toString()) {
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

        // Mostrar somente até 3 linhas com reticências coloridas ligeiramente mais escuras que o fundo
        try {
            val textoCompleto = nota.texto ?: ""
            val maxChars = 120 // heurística para aproximar 3 linhas (ajustável)
            if (textoCompleto.length > maxChars) {
                // corta respeitando última palavra
                var corte = textoCompleto.substring(0, maxChars)
                val lastSpace = corte.lastIndexOf(' ')
                if (lastSpace > 0) corte = corte.substring(0, lastSpace)
                val sb = SpannableStringBuilder()
                sb.append(corte)
                // criar reticências com cor derivada da cor de fundo (um pouco mais escura)
                val corFundo = nota.corFundo
                val corEllipsis = try {
                    // escurece a cor de fundo
                    val r = ((corFundo shr 16) and 0xFF)
                    val g = ((corFundo shr 8) and 0xFF)
                    val b = (corFundo and 0xFF)
                    val factor = 0.7f
                    val dr = (r * factor).toInt().coerceIn(0,255)
                    val dg = (g * factor).toInt().coerceIn(0,255)
                    val db = (b * factor).toInt().coerceIn(0,255)
                    (0xFF shl 24) or (dr shl 16) or (dg shl 8) or db
                } catch (e: Exception) {
                    0xFF444444.toInt()
                }
                val ell = "..."
                val start = sb.length
                sb.append(ell)
                sb.setSpan(ForegroundColorSpan(corEllipsis), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.texto.text = sb
            } else {
                holder.texto.text = textoCompleto
            }
        } catch (e: Exception) {
            Timber.w(e, "Erro ao truncar texto da nota")
            holder.texto.text = nota.texto
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
