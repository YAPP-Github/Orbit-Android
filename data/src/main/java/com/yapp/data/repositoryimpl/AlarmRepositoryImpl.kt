package com.yapp.data.repositoryimpl

import com.yapp.data.local.datasource.AlarmLocalDataSource
import com.yapp.data.local.mapper.toEntity
import com.yapp.domain.media.AlarmSoundManager
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmLocalDataSource: AlarmLocalDataSource,
    private val alarmSoundManager: AlarmSoundManager,
) : AlarmRepository {
    override suspend fun getAlarmSounds(): Result<List<AlarmSound>> = runCatching {
        alarmSoundManager.getAlarmSounds()
    }

    override fun initializeSoundPlayer(uri: String) {
        alarmSoundManager.initializeSoundPlayer(uri)
    }

    override fun playAlarmSound(volume: Int) {
        alarmSoundManager.playAlarmSound(volume)
    }

    override fun stopAlarmSound() {
        alarmSoundManager.stopAlarmSound()
    }

    override fun updateAlarmVolume(volume: Int) {
        alarmSoundManager.updateAlarmVolume(volume)
    }

    override fun releaseSoundPlayer() {
        alarmSoundManager.releaseSoundPlayer()
    }

    override fun getAllAlarms(): Flow<List<Alarm>> =
        alarmLocalDataSource.getAllAlarms()

    override fun getAlarmsByTime(hour: Int, minute: Int): Flow<List<Alarm>> =
        alarmLocalDataSource.getAlarmsByTime(hour, minute)

    override suspend fun insertAlarm(alarm: Alarm): Result<Alarm> = runCatching {
        val alarmId = alarmLocalDataSource.insertAlarm(alarm.toEntity())
        alarmLocalDataSource.getAlarm(alarmId)
            ?: throw Exception("Failed to insert alarm")
    }.onFailure {
        throw Exception("Failed to insert alarm")
    }

    override suspend fun updateAlarm(alarm: Alarm): Result<Alarm> = runCatching {
        val updatedRows = alarmLocalDataSource.updateAlarm(alarm.toEntity())
        if (updatedRows > 0) {
            alarmLocalDataSource.getAlarm(alarm.id)
                ?: throw Exception("Failed to fetch updated alarm")
        } else {
            throw Exception("No rows updated")
        }
    }

    override suspend fun updateAlarmActive(id: Long, active: Boolean) = runCatching {
        val updatedRows = alarmLocalDataSource.updateAlarmActive(id, active)
        if (updatedRows > 0) {
            alarmLocalDataSource.getAlarm(id)
                ?: throw Exception("Failed to update alarm active")
        } else {
            throw Exception("No rows updated")
        }
    }

    override suspend fun getAlarm(id: Long): Result<Alarm> = runCatching {
        alarmLocalDataSource.getAlarm(id)
            ?: throw Exception("Failed to get alarm")
    }.onFailure {
        throw Exception("Failed to get alarm")
    }

    override suspend fun deleteAlarm(id: Long): Result<Unit> = runCatching {
        val deletedRows = alarmLocalDataSource.deleteAlarm(id)
        if (deletedRows > 0) {
            Unit
        } else {
            throw Exception("No rows deleted")
        }
    }
}
