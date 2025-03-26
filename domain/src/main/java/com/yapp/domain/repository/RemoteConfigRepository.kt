package com.yapp.domain.repository

import com.yapp.domain.model.MissionType

interface RemoteConfigRepository {
    suspend fun fetchAndActivate(): Boolean
    fun getMissionType(): MissionType
}
