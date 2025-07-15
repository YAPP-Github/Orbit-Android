package com.yapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity): Int

    @Query("UPDATE ${AlarmDatabase.DATABASE_NAME} SET isAlarmActive = :active WHERE id = :id")
    suspend fun updateAlarmActive(id: Long, active: Boolean): Int

    @Query("SELECT * FROM ${AlarmDatabase.DATABASE_NAME} WHERE id = :id")
    suspend fun getAlarm(id: Long): AlarmEntity?

    @Query("SELECT * FROM ${AlarmDatabase.DATABASE_NAME} ORDER BY hour ASC, minute ASC LIMIT :limit OFFSET :offset")
    fun getPagedAlarms(limit: Int, offset: Int): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM ${AlarmDatabase.DATABASE_NAME} ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM ${AlarmDatabase.DATABASE_NAME} WHERE hour = :hour AND minute = :minute")
    fun getAlarmsByTime(hour: Int, minute: Int): Flow<List<AlarmEntity>>

    @Query("SELECT COUNT(*) FROM ${AlarmDatabase.DATABASE_NAME}")
    fun getAlarmCount(): Flow<Int>

    @Query("DELETE FROM ${AlarmDatabase.DATABASE_NAME} WHERE id = :id")
    suspend fun deleteAlarm(id: Long): Int
}
