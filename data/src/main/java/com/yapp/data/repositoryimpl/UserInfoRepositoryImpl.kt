package com.yapp.data.repositoryimpl

import com.yapp.data.local.datasource.UserLocalDataSource
import com.yapp.data.remote.datasource.UserInfoDataSource
import com.yapp.data.remote.dto.request.UpdateUserInfoRequest.Companion.toUpdateRequest
import com.yapp.data.remote.dto.response.toDomain
import com.yapp.domain.model.EditUser
import com.yapp.domain.model.User
import com.yapp.domain.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserInfoRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userInfoDataSource: UserInfoDataSource,
) : UserInfoRepository {
    override val userIdFlow: Flow<Long?> = userLocalDataSource.userIdFlow
    override val userNameFlow: Flow<String?> = userLocalDataSource.userNameFlow
    override val onboardingCompletedFlow: Flow<Boolean> = userLocalDataSource.onboardingCompletedFlow
    override val updateBottomSheetDontShowVersionFlow: Flow<String?> = userLocalDataSource.updateBottomSheetDontShowVersionFlow
    override val updateBottomSheetLastShownDateFlow: Flow<String?> = userLocalDataSource.updateBottomSheetLastShownDateFlow

    override suspend fun saveUserId(userId: Long) = userLocalDataSource.saveUserId(userId)
    override suspend fun saveUserName(userName: String) = userLocalDataSource.saveUserName(userName)
    override suspend fun setOnboardingCompleted() = userLocalDataSource.setOnboardingCompleted()
    override suspend fun markUpdateBottomSheetDontShow(version: String) = userLocalDataSource.markUpdateBottomSheetDontShow(version)
    override suspend fun markUpdateBottomSheetShownToday() = userLocalDataSource.markUpdateBottomSheetShownToday()
    override suspend fun clearUserData() = userLocalDataSource.clearUserData()

    override suspend fun getUserInfo(userId: Long): Result<User> {
        return userInfoDataSource.getUserInfo(userId)
            .mapCatching { userResponse ->
                userResponse.toDomain()
            }
    }

    override suspend fun updateUserInfo(userId: Long, editUser: EditUser): Result<Unit> {
        val request = editUser.toUpdateRequest()
        return userInfoDataSource.updateUserInfo(userId, request)
            .mapCatching {
                if (it) {
                    Unit
                } else {
                    throw Exception("Failed to update user info")
                }
            }
    }
}
