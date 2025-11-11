package com.yapp.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Alarm(
    val id: Long = 0,
    val hour: Int = 6,
    val minute: Int = 0,
    val second: Int = 0,
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
) {
    companion object {
        fun fromJson(json: String): Alarm = Json.decodeFromString(json)
    }
}

fun Alarm.copyFrom(source: Alarm): Alarm = copy(
    repeatDays = source.repeatDays,
    isHolidayAlarmOff = source.isHolidayAlarmOff,
    isSnoozeEnabled = source.isSnoozeEnabled,
    snoozeInterval = source.snoozeInterval,
    snoozeCount = source.snoozeCount,
    isVibrationEnabled = source.isVibrationEnabled,
    isSoundEnabled = source.isSoundEnabled,
    soundUri = source.soundUri,
    soundVolume = source.soundVolume,
    isAlarmActive = source.isAlarmActive,
)

fun Alarm.toTimeString(): String {
    val formattedHour = hour.toString().padStart(2, '0')
    val formattedMinute = minute.toString().padStart(2, '0')
    return "$formattedHour:$formattedMinute"
}

fun Alarm.toJson(): String = Json.encodeToString(this)
