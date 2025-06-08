package com.yapp.common.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data object HomeBaseRoute

sealed interface HomeDestination {
    @Serializable
    data object Route : HomeDestination

    @Serializable
    data class AlarmAddEdit(val alarmId: Long? = null) : HomeDestination
}
