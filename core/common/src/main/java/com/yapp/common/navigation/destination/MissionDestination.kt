package com.yapp.common.navigation.destination

import com.yapp.common.navigation.Routes

sealed class MissionDestination(val route: String) {
    data object Route : MissionDestination(Routes.Mission.ROUTE)
    data object Mission : MissionDestination(Routes.Mission.MISSION)
}
