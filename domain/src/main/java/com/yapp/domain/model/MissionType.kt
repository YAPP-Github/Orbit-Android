package com.yapp.domain.model

sealed class MissionType {
    data object Shake : MissionType()
    data object Click : MissionType()

    companion object {
        fun fromRemoteValue(value: String): MissionType {
            return when (value) {
                "tap_mission" -> Click
                "shake_mission" -> Shake
                else -> {
                    Click
                }
            }
        }
    }
}
