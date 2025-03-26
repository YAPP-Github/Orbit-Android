package com.yapp.data.remote.repositoryimpl

import com.yapp.domain.model.MissionType
import com.yapp.domain.repository.RemoteConfigRepository
import com.yapp.remoteconfig.FirebaseRemoteConfigManager
import javax.inject.Inject

class RemoteConfigRepositoryImpl @Inject constructor(
    private val manager: FirebaseRemoteConfigManager,
) : RemoteConfigRepository {

    override suspend fun fetchAndActivate(): Boolean = manager.fetchAndActivate()

    override fun getMissionType(): MissionType {
        return MissionType.fromRemoteValue(manager.getRawMissionType())
    }
}
