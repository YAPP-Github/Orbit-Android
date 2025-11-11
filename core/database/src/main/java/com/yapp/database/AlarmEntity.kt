package com.yapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = AlarmDatabase.DATABASE_NAME)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val hour: Int = 6,
    val minute: Int = 0,
    val second: Int = 0,

    // 반복 요일 (bitmask 를 통해 설정)
    val repeatDays: Int = 0,

    val isHolidayAlarmOff: Boolean = false,
    val isSnoozeEnabled: Boolean = false,

    val snoozeInterval: Int = 5,
    val snoozeCount: Int = 1,

    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,

    val soundUri: String = "",
    val soundVolume: Int = 70,

    val isAlarmActive: Boolean = true,

    @ColumnInfo(defaultValue = "1")
    val missionType: Int = DEFAULT_MISSION_TYPE_VALUE,
    @ColumnInfo(defaultValue = "10")
    val missionCount: Int = 10,
)

private const val DEFAULT_MISSION_TYPE_VALUE = 1
