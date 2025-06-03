package com.yapp.common.navigation.route

import com.yapp.domain.model.Alarm
import kotlinx.serialization.Serializable

@Serializable
data object AlarmInteractionBaseRoute

sealed interface AlarmInteractionDestination {
    @Serializable
    data object Route : AlarmInteractionDestination

    @Serializable
    data class AlarmAction(val alarm: Alarm) : AlarmInteractionDestination

    @Serializable
    data class AlarmSnoozeTimer(val alarm: Alarm) : AlarmInteractionDestination
}
