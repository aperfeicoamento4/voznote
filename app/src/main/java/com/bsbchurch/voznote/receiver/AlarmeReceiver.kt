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
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de lembretes do VozNote"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                // Use default notification sound with audio attributes
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, attrs)
            }
            nm.createNotificationChannel(channel)
        }

        // Preparar full-screen intent para mostrar janela grande do alarme
        val fullIntent = Intent(context, AlarmeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notaId", notaId)
            putExtra("texto", texto)
        }
        val fullPending = PendingIntent.getActivity(context, notaId, fullIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Notificação tradicional (para aparecer na barra) com full-screen intent
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle("Lembrete: VozNote")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(fullPending)
            .setFullScreenIntent(fullPending, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        // Para Android < O, garantir som padrão (usar som de alarme)
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            val alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            builder.setSound(alarmUri)
            builder.setDefaults(android.app.Notification.DEFAULT_LIGHTS)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notaId.takeIf { it != 0 } ?: System.currentTimeMillis().toInt(), builder.build())
        }

        // Também tentar iniciar Activity diretamente (fallback) para garantir que a janela apareça
        try {
            context.startActivity(fullIntent)
        } catch (e: Exception) {
            Timber.w(e, "Não foi possível iniciar AlarmeActivity diretamente, confiando no full-screen intent")
        }
    }
}
