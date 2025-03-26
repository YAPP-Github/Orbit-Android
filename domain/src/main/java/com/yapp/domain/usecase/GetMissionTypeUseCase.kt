package com.yapp.domain.usecase

import com.yapp.domain.model.MissionType
import com.yapp.domain.repository.RemoteConfigRepository
import javax.inject.Inject

class GetMissionTypeUseCase @Inject constructor(
    private val repository: RemoteConfigRepository,
) {
    suspend fun execute(): MissionType {
        repository.fetchAndActivate()
        return repository.getMissionType()
    }
}
