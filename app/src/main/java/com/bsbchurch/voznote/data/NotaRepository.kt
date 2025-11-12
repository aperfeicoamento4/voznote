package com.bsbchurch.voznote.data

import android.content.Context

/**
 * Repositório simples para gerenciar operações com notas.
 */
class NotaRepository private constructor(context: Context) {

    private val dao = AppDatabase.obterDatabase(context).notaDao()

    suspend fun listarTodas() = dao.listarTodas()

    suspend fun inserir(nota: Nota): Long = dao.inserir(nota)

    suspend fun atualizar(nota: Nota) = dao.atualizar(nota)

    suspend fun deletar(nota: Nota) = dao.deletar(nota)

    suspend fun atualizarOrdem(id: Int, ordem: Int) = dao.atualizarOrdem(id, ordem)

    companion object {
        @Volatile
        private var INSTANCIA: NotaRepository? = null

        fun obter(context: Context): NotaRepository {
            return INSTANCIA ?: synchronized(this) {
                val repo = NotaRepository(context.applicationContext)
                INSTANCIA = repo
                repo
            }
        }
    }
}
