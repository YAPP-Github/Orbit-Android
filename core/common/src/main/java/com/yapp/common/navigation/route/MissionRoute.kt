package com.yapp.common.navigation.route

import com.yapp.domain.MissionMode
import kotlinx.serialization.Serializable

@Serializable
data class MissionRoute(
    val missionType: String,
    val missionCount: String,
    val missionMode: String = MissionMode.REAL.name,
) {
    companion object {
        const val route = "mission"
    }
}
