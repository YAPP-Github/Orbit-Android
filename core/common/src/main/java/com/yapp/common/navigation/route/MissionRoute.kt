package com.yapp.common.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data class MissionRoute(
    val missionType: String,
    val missionCount: String,
    val missionMode: String = "REAL", // PREVIEW 지원
) {
    companion object {
        const val route = "mission"
    }
}
