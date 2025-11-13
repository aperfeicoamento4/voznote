package com.bsbchurch.voznote.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bsbchurch.voznote.R
import timber.log.Timber

class AlarmeActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarme)

        // Mostrar sobre a tela de bloqueio e ligar a tela
        try {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } catch (e: Exception) {
            // métodos podem não existir em versões muito antigas
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val texto = intent.getStringExtra("texto") ?: "Lembrete VozNote"
        val notaId = intent.getIntExtra("notaId", 0)

        val tv = findViewById<TextView>(R.id.alarmeText)
        tv.text = primeiroasTresLinhas(texto)

        val btn = findViewById<Button>(R.id.btnCancelarAlarme)
        btn.setOnClickListener {
            cancelarAlarme(notaId, texto)
            finish()
        }

        // Tocar som de alarme padrão com atributos USAGE_ALARM
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, alarmUri)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone?.apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(attrs)
                }
                play()
            }
        } catch (e: Exception) {
            Timber.w(e, "Não foi possível tocar som de alarme")
        }
    }

    override fun onDestroy() {
        try { ringtone?.stop() } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun cancelarAlarme(notaId: Int, texto: String) {
        try {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmeReceiver::class.java).apply {
                putExtra("notaId", notaId)
                putExtra("texto", texto)
            }
            val pending = PendingIntent.getBroadcast(this, notaId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.cancel(pending)
            try {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                nm.cancel(notaId)
            } catch (_: Exception) {}
        } catch (e: Exception) {
            Timber.w(e, "Erro ao cancelar alarme na Activity para nota %s", notaId)
        }
    }

    private fun primeiroasTresLinhas(texto: String): String {
        val lines = texto.split('\n')
        val sb = StringBuilder()
        var count = 0
        for (ln in lines) {
            if (ln.isBlank()) continue
            sb.append(ln.trim())
            count++
            if (count >= 3) break
            if (count < 3) sb.append('\n')
        }
        var res = sb.toString()
        if (res.isBlank()) {
            res = if (texto.length > 200) texto.substring(0, 200) else texto
        }
        return res
    }
}
