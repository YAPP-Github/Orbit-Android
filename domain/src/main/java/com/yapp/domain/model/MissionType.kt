package com.yapp.domain.model

enum class MissionType(val value: Int) {
    TAP(0),
    SHAKE(1),
    ;

    companion object {
        fun fromInt(value: Int): MissionType {
            return MissionType.entries.find { it.value == value } ?: TAP
        }
    }
}
