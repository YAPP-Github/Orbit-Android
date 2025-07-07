package com.yapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.MissionType

@Entity(tableName = AlarmDatabase.DATABASE_NAME)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val isAm: Boolean = true,
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

    val missionType: MissionType = MissionType.TAP,
    val missionCount: Int = 10,
)

fun AlarmEntity.toDomain() = Alarm(
    id = id,
    isAm = isAm,
    hour = hour,
    minute = minute,
    second = second,
    repeatDays = repeatDays,
    isHolidayAlarmOff = isHolidayAlarmOff,
    isSnoozeEnabled = isSnoozeEnabled,
    snoozeInterval = snoozeInterval,
    snoozeCount = snoozeCount,
    isVibrationEnabled = isVibrationEnabled,
    isSoundEnabled = isSoundEnabled,
    soundUri = soundUri,
    soundVolume = soundVolume,
    isAlarmActive = isAlarmActive,
)

fun Alarm.toEntity() = AlarmEntity(
    id = id,
    isAm = isAm,
    hour = hour,
    minute = minute,
    second = second,
    repeatDays = repeatDays,
    isHolidayAlarmOff = isHolidayAlarmOff,
    isSnoozeEnabled = isSnoozeEnabled,
    snoozeInterval = snoozeInterval,
    snoozeCount = snoozeCount,
    isVibrationEnabled = isVibrationEnabled,
    isSoundEnabled = isSoundEnabled,
    soundUri = soundUri,
    soundVolume = soundVolume,
    isAlarmActive = isAlarmActive,
)
