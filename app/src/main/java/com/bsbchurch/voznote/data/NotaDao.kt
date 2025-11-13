package com.bsbchurch.voznote.data

import androidx.room.*

@Dao
interface NotaDao {
    @Query("SELECT * FROM nota ORDER BY ordem ASC, dataHora DESC")
    suspend fun listarTodas(): List<Nota>

    @Query("SELECT * FROM nota WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Nota?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(nota: Nota): Long

    @Update
    suspend fun atualizar(nota: Nota)

    @Delete
    suspend fun deletar(nota: Nota)

    @Query("UPDATE nota SET ordem = :ordem WHERE id = :id")
    suspend fun atualizarOrdem(id: Int, ordem: Int)
}
