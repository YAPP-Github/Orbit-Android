package com.yapp.alarm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.yapp.alarm.AlarmConstants
import com.yapp.alarm.AndroidAlarmScheduler
import com.yapp.alarm.pendingIntent.interaction.createAlarmAlertPendingIntent
import com.yapp.alarm.pendingIntent.interaction.createAlarmDismissPendingIntent
import com.yapp.alarm.pendingIntent.interaction.createAlarmSnoozePendingIntent
import com.yapp.alarm.pendingIntent.interaction.createNavigateToMissionPendingIntent
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.usecase.AlarmUseCase
import com.yapp.media.sound.SoundPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var alarmUseCase: AlarmUseCase

    @Inject
    lateinit var soundPlayer: SoundPlayer

    private lateinit var vibrator: Vibrator

    @Inject
    lateinit var androidAlarmScheduler: AndroidAlarmScheduler

    @Inject
    lateinit var fortuneRepository: FortuneRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupVibrator()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            handleIntent(intent ?: return@launch)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopVibration()
        stopSound()
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun handleIntent(intent: Intent) {
        val alarm: Alarm? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(AlarmConstants.EXTRA_ALARM, Alarm::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(AlarmConstants.EXTRA_ALARM)
        }

        if (alarm == null) {
            Log.e("AlarmService", "Failed to retrieve Alarm object from intent")
            return
        }

        val notificationId = alarm.id
        val isDismiss = intent.getBooleanExtra(AlarmConstants.EXTRA_IS_DISMISS, false)
        val isOneTimeAlarm = alarm.repeatDays == 0

        Log.d("AlarmService", "AlarmService started for alarm: $alarm")

        // 반복 요일 알람 시, 다음 주 동일 요일 알람 예약
        if (!isOneTimeAlarm) {
            intent.getStringExtra(AlarmConstants.EXTRA_ALARM_DAY)?.let {
                androidAlarmScheduler.scheduleWeeklyAlarm(alarm, AlarmDay.valueOf(it))
            }
        }

        // 알람 해제 여부에 따른 처리
        when (isDismiss) {
            true -> stopSelf()
            false -> {
                startForeground(
                    notificationId.toInt(),
                    createNotification(alarm, shouldNavigateToMission()),
                )
                if (alarm.isVibrationEnabled) startVibration()
                if (alarm.isSoundEnabled) startSound(alarm.soundUri, alarm.soundVolume)
            }
        }

        if (isOneTimeAlarm) {
            turnOffAlarm(alarmId = notificationId)
        }
    }

    private suspend fun shouldNavigateToMission(): Boolean {
        val fortuneDate = fortuneRepository.fortuneDateFlow.firstOrNull()
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return fortuneDate != todayDate
    }

    private fun createNotification(alarm: Alarm, shouldNavigateToMission: Boolean): Notification {
        val alarmAlertPendingIntent =
            createAlarmAlertPendingIntent(applicationContext, alarm)

        val alarmDismissPendingIntent = if (shouldNavigateToMission) {
            createNavigateToMissionPendingIntent(
                applicationContext = applicationContext,
                notificationId = alarm.id,
            )
        } else {
            createAlarmDismissPendingIntent(
                applicationContext = applicationContext,
                pendingIntentId = alarm.id,
            )
        }

        val snoozePendingIntent = if (alarm.isSnoozeEnabled && alarm.snoozeCount != 0) {
            createAlarmSnoozePendingIntent(applicationContext, alarm)
        } else {
            null
        }

        val builder = NotificationCompat.Builder(applicationContext, ALARM_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(core.designsystem.R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, core.designsystem.R.mipmap.ic_launcher))
            .setContentTitle("오르비 알람")
            .setContentText("알람을 해제할 시간이예요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(alarmAlertPendingIntent, true)
            .setDeleteIntent(
                snoozePendingIntent ?: alarmDismissPendingIntent,
            )
            .addAction(core.designsystem.R.drawable.ic_cancel, "알람 해제", alarmDismissPendingIntent)

        snoozePendingIntent?.let {
            builder.addAction(core.designsystem.R.drawable.ic_cancel, "미루기", it)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ALARM_NOTIFICATION_CHANNEL_ID,
            ALARM_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = ALARM_NOTIFICATION_CHANNEL_DESCRIPTION
            enableVibration(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                null,
            )
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun turnOffAlarm(alarmId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmUseCase.updateAlarmActive(
                id = alarmId,
                active = false,
            )
        }
    }

    private fun setupVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = applicationContext.getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator ?: applicationContext.getSystemService(Vibrator::class.java)
        } else {
            applicationContext.getSystemService(Vibrator::class.java)
        } ?: throw IllegalStateException("Vibrator service is unavailable")
    }

    private fun startVibration() {
        val pattern: LongArray = longArrayOf(0, 1000, 500)
        val effect = VibrationEffect.createWaveform(pattern, 0)
        vibrator.vibrate(effect)
    }

    private fun stopVibration() {
        vibrator.cancel()
    }

    private fun startSound(soundUri: String, volume: Int) {
        val uri: Uri = if (soundUri.isNotEmpty()) {
            soundUri.toUri()
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        soundPlayer.initialize(uri)
        soundPlayer.playSound(volume)
    }

    private fun stopSound() {
        soundPlayer.stopSound()
    }

    companion object {
        const val ALARM_NOTIFICATION_CHANNEL_ID = "Orbit_Channel_Id"
        const val ALARM_NOTIFICATION_CHANNEL_NAME = "Orbit"
        const val ALARM_NOTIFICATION_CHANNEL_DESCRIPTION = "To show notification for alarms"
    }
}
