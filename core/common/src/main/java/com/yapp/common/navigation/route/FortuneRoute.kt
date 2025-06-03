package com.yapp.common.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data object FortuneBaseRoute

sealed interface FortuneDestination {
    @Serializable
    data object Fortune : FortuneDestination

    @Serializable
    data object Reward : FortuneDestination
}
