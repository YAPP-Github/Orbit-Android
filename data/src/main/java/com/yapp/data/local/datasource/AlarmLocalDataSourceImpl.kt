package com.yapp.data.local.datasource

import com.yapp.database.AlarmDao
import com.yapp.database.AlarmEntity
import com.yapp.database.toDomain
import com.yapp.domain.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmLocalDataSourceImpl @Inject constructor(
    private val alarmDao: AlarmDao,
) : AlarmLocalDataSource {
    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms()
            .map { alarmEntities -> alarmEntities.map { it.toDomain() } }
    }

    override fun getAlarmsByTime(hour: Int, minute: Int): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByTime(hour, minute).map { alarmEntities ->
            alarmEntities.map { it.toDomain() }
        }
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
}
