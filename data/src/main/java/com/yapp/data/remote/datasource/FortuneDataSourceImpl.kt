package com.yapp.data.remote.datasource

import com.yapp.data.remote.dto.response.FortuneResponse
import com.yapp.data.remote.service.ApiService
import com.yapp.network.utils.safeApiCall
import javax.inject.Inject

class FortuneDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
) : FortuneDataSource {
    override suspend fun postFortune(userId: Long): Result<FortuneResponse> {
        return safeApiCall { apiService.postFortune(userId) }
    }
    override suspend fun getFortune(fortuneId: Long): Result<FortuneResponse> {
        return safeApiCall { apiService.getFortune(fortuneId) }
    }
}
