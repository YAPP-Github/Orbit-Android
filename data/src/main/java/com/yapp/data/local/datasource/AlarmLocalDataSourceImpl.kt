package com.yapp.data.local.datasource

import com.yapp.data.local.AlarmDao
import com.yapp.data.local.AlarmEntity
import com.yapp.data.local.toDomain
import com.yapp.datastore.UserPreferences
import com.yapp.domain.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmLocalDataSourceImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val userPreferences: UserPreferences,
) : AlarmLocalDataSource {
    override val firstDismissedAlarmIdFlow: Flow<Long?> = userPreferences.firstDismissedAlarmIdFlow

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms()
            .map { alarmEntities -> alarmEntities.map { it.toDomain() } }
    }

    override fun getPagedAlarms(
        limit: Int,
        offset: Int,
    ): Flow<List<Alarm>> {
        return alarmDao.getPagedAlarms(limit, offset)
            .map { alarmEntities -> alarmEntities.map { it.toDomain() } }
    }

    override fun getAlarmsByTime(hour: Int, minute: Int, isAm: Boolean): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByTime(hour, minute, isAm).map { alarmEntities ->
            alarmEntities.map { it.toDomain() }
        }
    }

    override fun getAlarmCount(): Flow<Int> {
        return alarmDao.getAlarmCount()
    }

    override suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmDao.insertAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: AlarmEntity): Int {
        return alarmDao.updateAlarm(alarm)
    }

    override suspend fun updateAlarmActive(id: Long, active: Boolean): Int {
        return alarmDao.updateAlarmActive(id, active)
    }

    override suspend fun getAlarm(id: Long): Alarm? {
        return alarmDao.getAlarm(id)?.toDomain()
    }

    override suspend fun deleteAlarm(id: Long): Int {
        return alarmDao.deleteAlarm(id)
    }

    override suspend fun saveFirstDismissedAlarmId(alarmId: Long) {
        userPreferences.saveFirstDismissedAlarmId(alarmId)
    }

    override suspend fun clearDismissedAlarmId() {
        userPreferences.clearDismissedAlarmId()
    }
}
