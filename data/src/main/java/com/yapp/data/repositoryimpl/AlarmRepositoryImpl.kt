package com.yapp.data.repositoryimpl

import android.net.Uri
import com.yapp.data.local.datasource.AlarmLocalDataSource
import com.yapp.database.toEntity
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.repository.AlarmRepository
import com.yapp.media.ringtone.RingtoneManagerHelper
import com.yapp.media.sound.SoundPlayer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmLocalDataSource: AlarmLocalDataSource,
    private val ringtoneManagerHelper: RingtoneManagerHelper,
    private val soundPlayer: SoundPlayer,
) : AlarmRepository {
    override val firstDismissedAlarmIdFlow: Flow<Long?> = alarmLocalDataSource.firstDismissedAlarmIdFlow

    override suspend fun getAlarmSounds(): Result<List<AlarmSound>> = runCatching {
        ringtoneManagerHelper.getAlarmSounds().map { (title, uri) ->
            AlarmSound(title, uri)
        }
    }

    override fun initializeSoundPlayer(uri: Uri) {
        soundPlayer.initialize(uri)
    }

    override fun playAlarmSound(volume: Int) {
        soundPlayer.playSound(volume)
    }

    override fun stopAlarmSound() {
        soundPlayer.stopSound()
    }

    override fun updateAlarmVolume(volume: Int) {
        soundPlayer.updateVolume(volume)
    }

    override fun releaseSoundPlayer() {
        soundPlayer.release()
    }

    override fun getAllAlarms(): Flow<List<Alarm>> =
        alarmLocalDataSource.getAllAlarms()

    override fun getPagedAlarms(limit: Int, offset: Int): Flow<List<Alarm>> =
        alarmLocalDataSource.getPagedAlarms(limit, offset)

    override fun getAlarmsByTime(hour: Int, minute: Int): Flow<List<Alarm>> =
        alarmLocalDataSource.getAlarmsByTime(hour, minute)

    override fun getAlarmCount(): Flow<Int> =
        alarmLocalDataSource.getAlarmCount()

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

    override suspend fun saveFirstDismissedAlarmId(alarmId: Long) {
        alarmLocalDataSource.saveFirstDismissedAlarmId(alarmId)
    }

    override suspend fun clearDismissedAlarmId() {
        alarmLocalDataSource.clearDismissedAlarmId()
    }
}
