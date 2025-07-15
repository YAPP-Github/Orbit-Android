package com.yapp.domain.usecase

import android.net.Uri
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
) {
    suspend fun getAlarmSounds(): Result<List<AlarmSound>> = alarmRepository.getAlarmSounds()
    fun initializeSoundPlayer(uri: Uri) = alarmRepository.initializeSoundPlayer(uri)
    fun playAlarmSound(volume: Int) = alarmRepository.playAlarmSound(volume)
    fun stopAlarmSound() = alarmRepository.stopAlarmSound()
    fun updateAlarmVolume(volume: Int) = alarmRepository.updateAlarmVolume(volume)
    fun releaseSoundPlayer() = alarmRepository.releaseSoundPlayer()
    fun getAllAlarms(): Flow<List<Alarm>> = alarmRepository.getAllAlarms()
    fun getPagedAlarms(limit: Int, offset: Int): Flow<List<Alarm>> = alarmRepository.getPagedAlarms(limit, offset)
    fun getAlarmsByTime(hour: Int, minute: Int): Flow<List<Alarm>> = alarmRepository.getAlarmsByTime(hour, minute)
    fun getAlarmCount(): Flow<Int> = alarmRepository.getAlarmCount()
    suspend fun insertAlarm(alarm: Alarm): Result<Alarm> = alarmRepository.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: Alarm): Result<Alarm> = alarmRepository.updateAlarm(alarm)
    suspend fun updateAlarmActive(id: Long, active: Boolean): Result<Alarm> = alarmRepository.updateAlarmActive(id, active)
    suspend fun getAlarm(id: Long): Result<Alarm> = alarmRepository.getAlarm(id)
    suspend fun deleteAlarm(id: Long): Result<Unit> = alarmRepository.deleteAlarm(id)
}
