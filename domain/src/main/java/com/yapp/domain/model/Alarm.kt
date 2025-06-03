package com.yapp.domain.model

import android.net.Uri
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Alarm(
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
    // -1 이면 무제한
    val snoozeCount: Int = 1,

    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,

    val soundUri: String = "",
    val soundVolume: Int = 70,

    val isAlarmActive: Boolean = true,
) : Parcelable {

    companion object {
        fun fromJson(json: String): Alarm {
            return Gson().fromJson(json, object : TypeToken<Alarm>() {}.type)
        }
    }

    override fun toString(): String = Uri.encode(Gson().toJson(this))
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
    val displayHour = if (isAm && hour == 12) {
        0 // 오전 12시는 0으로 표시
    } else if (!isAm && hour != 12) {
        hour + 12 // 오후 1시~11시에는 12를 더함
    } else {
        hour // 오전 1시~11시 및 오후 12시는 그대로 사용
    }
    val formattedHour = displayHour.toString().padStart(2, '0')
    val formattedMinute = minute.toString().padStart(2, '0')

    return "$formattedHour:$formattedMinute"
}
