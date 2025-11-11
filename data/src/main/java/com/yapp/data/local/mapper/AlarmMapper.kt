package com.yapp.data.local.mapper

import com.yapp.database.AlarmEntity
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.MissionType

fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
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
    missionType = MissionType.fromInt(missionType),
    missionCount = missionCount,
)

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
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
    missionType = missionType.value,
    missionCount = missionCount,
)
