package com.bsbchurch.voznote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Nota::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obterDatabase(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voznote_db"
                ).build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
