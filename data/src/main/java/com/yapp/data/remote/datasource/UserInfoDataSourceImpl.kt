package com.yapp.data.remote.datasource

import com.yapp.data.remote.dto.request.UpdateUserInfoRequest
import com.yapp.data.remote.dto.response.UserResponse
import com.yapp.data.remote.service.ApiService
import com.yapp.network.utils.safeApiCall
import javax.inject.Inject

class UserInfoDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
) : UserInfoDataSource {
    override suspend fun getUserInfo(userId: Long): Result<UserResponse> {
        return safeApiCall { apiService.getUserInfo(userId) }
    }

    override suspend fun updateUserInfo(userId: Long, updateUserInfoRequest: UpdateUserInfoRequest): Result<Boolean> {
        return safeApiCall { apiService.updateUserInfo(userId, updateUserInfoRequest).isSuccessful }
    }
}
