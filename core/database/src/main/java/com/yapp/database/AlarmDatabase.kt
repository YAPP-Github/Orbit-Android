package com.yapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AlarmEntity::class], version = 2, exportSchema = true)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "alarm_database"
    }
}
