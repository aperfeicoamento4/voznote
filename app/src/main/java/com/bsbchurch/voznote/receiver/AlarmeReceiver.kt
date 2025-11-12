package com.bsbchurch.voznote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Receiver para receber alarmes do AlarmManager.
 * Implementação básica: logar o recebimento. A notificação será implementada na etapa de Alarmes.
 */
class AlarmeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("AlarmeReceiver recebeu um alarme")
        // TODO: construir e exibir notificação com som padrão
    }
}
