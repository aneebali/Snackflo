package com.salesflo.snackflo

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.Calendar

fun scheduleAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
            return
        }
    }

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("message", "Kindly Place your orders!")
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 11)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)


        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }

        // Skip Saturday (7) and Sunday (1) - find next weekday
        while (get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
            get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            add(Calendar.DATE, 1)
        }
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )

   // Toast.makeText(context, "⏰ Alarm set for next weekday", Toast.LENGTH_SHORT).show()
}

class AlarmReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("message") ?: "Reminder!"

        // Start alarm service
        val alarmIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("message", message)
        }
        context?.startForegroundService(alarmIntent)

        scheduleNextAlarm(context)
    }

    private fun scheduleNextAlarm(context: Context?) {
        context?.let {
            val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(it, AlarmReceiver::class.java).apply {
                putExtra("message", "Kindly Place your orders!")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                it,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DATE, 1)
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // Skip weekends
                while (get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    add(Calendar.DATE, 1)
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}


class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        private const val NOTIFICATION_ID = 1003
        private var isAlarmRunning = false // Prevent duplicate alarms
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "STOP_ALARM" -> {
                stopAlarm()
                return START_NOT_STICKY
            }
            else -> {
                // Prevent duplicate alarm if already running
                if (!isAlarmRunning) {
                    val message = intent?.getStringExtra("message") ?: "Alarm!"
                    startAlarm(message)
                }
                return START_STICKY
            }
        }
    }

    private fun startAlarm(message: String) {
        isAlarmRunning = true

        createForegroundNotification(message)

        startAlarmSound()


        startVibration()

        Handler(Looper.getMainLooper()).postDelayed({
            stopAlarm()
        }, 60000) // 1 minute
    }

    private fun createForegroundNotification(message: String) {
        val channelId = "alarm_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.salesflo.snackflo.R.drawable.ic_launcher)
            .setContentTitle("Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startAlarmSound() {
        try {
            mediaPlayer = MediaPlayer().apply {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                setDataSource(this@AlarmService, alarmUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                }
                isLooping = true
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playSystemAlarm()
        }
    }

    private fun playSystemAlarm() {
        val ringtone = RingtoneManager.getRingtone(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        ringtone?.play()
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlarm() {
        isAlarmRunning = false

        // Stop sound
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null

        vibrator?.cancel()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}


