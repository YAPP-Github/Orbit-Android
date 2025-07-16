package com.yapp.domain.usecase

import android.net.Uri
import com.yapp.domain.formatter.AlarmDateTimeFormatter
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.model.toRepeatDays
import com.yapp.domain.repository.AlarmRepository
import com.yapp.domain.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class AlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmDateTimeFormatter: AlarmDateTimeFormatter,
    private val alarmScheduler: AlarmScheduler,
) {
    suspend fun getAlarmSounds(): Result<List<AlarmSound>> = alarmRepository.getAlarmSounds()
    fun initializeSoundPlayer(uri: Uri) = alarmRepository.initializeSoundPlayer(uri)
    fun playAlarmSound(volume: Int) = alarmRepository.playAlarmSound(volume)
    fun stopAlarmSound() = alarmRepository.stopAlarmSound()
    fun updateAlarmVolume(volume: Int) = alarmRepository.updateAlarmVolume(volume)
    fun releaseSoundPlayer() = alarmRepository.releaseSoundPlayer()
    fun getAllAlarms(): Flow<List<Alarm>> = alarmRepository.getAllAlarms()
    fun getAlarmsByTime(hour: Int, minute: Int): Flow<List<Alarm>> = alarmRepository.getAlarmsByTime(hour, minute)
    suspend fun insertAlarm(alarm: Alarm): Result<Alarm> = alarmRepository.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: Alarm): Result<Alarm> = alarmRepository.updateAlarm(alarm)
    suspend fun updateAlarmActive(id: Long, active: Boolean): Result<Alarm> = alarmRepository.updateAlarmActive(id, active)
    suspend fun getAlarm(id: Long): Result<Alarm> = alarmRepository.getAlarm(id)
    suspend fun deleteAlarm(id: Long): Result<Unit> = alarmRepository.deleteAlarm(id)

    fun scheduleAlarm(alarm: Alarm) = alarmScheduler.scheduleAlarm(alarm)
    fun unScheduleAlarm(alarm: Alarm) = alarmScheduler.unScheduleAlarm(alarm)

    fun getFormattedNextAlarmMessage(
        currentTime: LocalTime,
        formats: AlarmDateTimeFormatter.TimeDifferenceFormats,
        selectedDays: Set<AlarmDay>,
        now: LocalDateTime = LocalDateTime.now(),
    ): String {
        val repeatDaysInt = selectedDays.toRepeatDays() // Set<AlarmDay>를 Int로 변환
        val nextOccurrence = alarmDateTimeFormatter.calculateNextOccurrence(
            hour = currentTime.hour,
            minute = currentTime.minute,
            repeatDays = repeatDaysInt,
            now = now,
        )

        return alarmDateTimeFormatter.formatTimeDifference(now, nextOccurrence, formats)
    }

    fun getFormattedEarliestUpcomingAlarm(
        alarms: List<Alarm>,
        formats: AlarmDateTimeFormatter.DeliveryTimeFormats,
        now: LocalDateTime = LocalDateTime.now(),
    ): String {
        return alarmDateTimeFormatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, formats, now)
    }
}
