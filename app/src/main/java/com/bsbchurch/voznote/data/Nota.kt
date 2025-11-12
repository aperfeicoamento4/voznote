package com.bsbchurch.voznote.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Nota para Room.
 * Campos:
 * - id: chave primária
 * - texto: transcrição / texto da nota
 * - caminhoAudio: local do arquivo gravado (opcional)
 * - dataHora: timestamp de criação
 * - corFundo: cor de fundo (int)
 * - alarmeMillis: timestamp do alarme (nullable)
 * - alarmeRecorrencia: "NONE" | "DAILY" | "WEEKLY"
 * - ordem: inteiro para ordenar a lista
 */
@Entity(tableName = "nota")
data class Nota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var texto: String,
    var caminhoAudio: String? = null,
    var dataHora: Long = System.currentTimeMillis(),
    var corFundo: Int = 0xFFFFFFFF.toInt(),
    var alarmeMillis: Long? = null,
    var alarmeRecorrencia: String? = "NONE",
    var ordem: Int = 0
)
