package com.bsbchurch.voznote

import android.app.Application
import timber.log.Timber

/**
 * Classe Application para inicializações globais.
 * Aqui inicializamos o Timber para logs legíveis durante desenvolvimento.
 */
class VozNoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Planta uma DebugTree do Timber para logs detalhados
        Timber.plant(Timber.DebugTree())
        Timber.i("VozNoteApp iniciada: Timber plantado")
    }
}
