package com.yapp.domain.model

enum class MissionType(val value: Int) {
    NONE(0),
    TAP(1),
    SHAKE(2),
    ;

    companion object {
        fun fromInt(value: Int): MissionType {
            return MissionType.entries.find { it.value == value } ?: NONE
        }

        fun fromRemoteValue(value: String): MissionType {
            return when (value) {
                "tap_mission" -> TAP
                "shake_mission" -> SHAKE
                else -> NONE
            }
        }
    }
}
