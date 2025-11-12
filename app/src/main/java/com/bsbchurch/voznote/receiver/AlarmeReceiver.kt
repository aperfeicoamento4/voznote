package com.bsbchurch.voznote.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bsbchurch.voznote.R
import com.bsbchurch.voznote.notas.NotasActivity
import timber.log.Timber

class AlarmeReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "voznote_alarme_channel"
        const val CHANNEL_NAME = "Alarmes VozNote"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("AlarmeReceiver recebeu um alarme")
        if (context == null || intent == null) return

        val texto = intent.getStringExtra("texto") ?: "Lembrete VozNote"
        val notaId = intent.getIntExtra("notaId", 0)

        // Criar canal de notificação se necessário
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de lembretes do VozNote"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                // Use default notification sound
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            nm.createNotificationChannel(channel)
        }

        // Ao tocar, abrir a lista de notas
        val openIntent = Intent(context, NotasActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notaId", notaId)
        }
        val pending = PendingIntent.getActivity(context, notaId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle("Lembrete: VozNote")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pending)
        // Para Android < O, garantir som padrão
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            builder.setDefaults(android.app.Notification.DEFAULT_ALL)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notaId.takeIf { it != 0 } ?: System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
