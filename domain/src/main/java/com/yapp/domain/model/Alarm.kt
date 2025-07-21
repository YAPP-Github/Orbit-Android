package com.yapp.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Parcelize
@Serializable
data class Alarm(
    val id: Long = 0,

    val hour: Int = 6,
    val minute: Int = 0,
    val second: Int = 0,

    // 반복 요일 (bitmask 를 통해 설정)
    val repeatDays: Int = 0,

    val isHolidayAlarmOff: Boolean = false,
    val isSnoozeEnabled: Boolean = false,

    val snoozeInterval: Int = 5,
    // -1 이면 무제한
    val snoozeCount: Int = 1,

    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,

    val soundUri: String = "",
    val soundVolume: Int = 70,

    val isAlarmActive: Boolean = true,

    val missionType: MissionType = MissionType.TAP,
    val missionCount: Int = 10,
) : Parcelable {

    companion object {
        fun fromJson(json: String): Alarm {
            return Json.decodeFromString(json)
        }
    }

    override fun toString(): String = Uri.encode(Json.encodeToString(this))
}

fun Alarm.copyFrom(source: Alarm): Alarm {
    return this.copy(
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
}

fun Alarm.toTimeString(): String {
    val formattedHour = hour.toString().padStart(2, '0')
    val formattedMinute = minute.toString().padStart(2, '0')

    return "$formattedHour:$formattedMinute"
}
