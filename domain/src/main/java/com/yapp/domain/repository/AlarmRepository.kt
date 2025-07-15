package com.yapp.domain.repository

import android.net.Uri
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmSound
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    val firstDismissedAlarmIdFlow: Flow<Long?>

    suspend fun getAlarmSounds(): Result<List<AlarmSound>>
    fun initializeSoundPlayer(uri: Uri)
    fun playAlarmSound(volume: Int)
    fun stopAlarmSound()
    fun updateAlarmVolume(volume: Int)
    fun releaseSoundPlayer()
    fun getAllAlarms(): Flow<List<Alarm>>
    fun getPagedAlarms(limit: Int, offset: Int): Flow<List<Alarm>>
    fun getAlarmsByTime(hour: Int, minute: Int): Flow<List<Alarm>>
    fun getAlarmCount(): Flow<Int>
    suspend fun insertAlarm(alarm: Alarm): Result<Alarm>
    suspend fun updateAlarm(alarm: Alarm): Result<Alarm>
    suspend fun updateAlarmActive(id: Long, active: Boolean): Result<Alarm>
    suspend fun getAlarm(id: Long): Result<Alarm>
    suspend fun deleteAlarm(id: Long): Result<Unit>
    suspend fun saveFirstDismissedAlarmId(alarmId: Long)
    suspend fun clearDismissedAlarmId()
}
