package com.yapp.common.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data object SettingBaseRoute

@Serializable
sealed interface SettingDestination {
    @Serializable
    data object Setting : SettingDestination

    @Serializable
    data object EditProfile : SettingDestination

    @Serializable
    data object EditBirthday : SettingDestination
}
