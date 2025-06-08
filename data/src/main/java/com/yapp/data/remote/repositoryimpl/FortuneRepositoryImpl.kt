package com.yapp.data.remote.repositoryimpl

import com.yapp.data.remote.datasource.FortuneDataSource
import com.yapp.data.remote.dto.response.toDomain
import com.yapp.domain.model.Fortune
import com.yapp.domain.repository.FortuneRepository
import javax.inject.Inject

class FortuneRepositoryImpl @Inject constructor(
    private val fortuneDataSource: FortuneDataSource,
) : FortuneRepository {
    override suspend fun postFortune(userId: Long): Result<Fortune> {
        return fortuneDataSource.postFortune(userId)
            .mapCatching { fortuneResponse ->
                fortuneResponse.toDomain()
            }
    }
    override suspend fun getFortune(fortuneId: Long): Result<Fortune> {
        return fortuneDataSource.getFortune(fortuneId)
            .mapCatching { fortuneResponse ->
                fortuneResponse.toDomain()
            }
    }
}
