package com.yapp.domain.repository

import com.yapp.domain.model.EditUser
import com.yapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserInfoRepository {
    val userIdFlow: Flow<Long?>
    val userNameFlow: Flow<String?>
    val onboardingCompletedFlow: Flow<Boolean>
    val updateNoticeDontShowVersionFlow: Flow<String?>
    val updateNoticeLastShownDateEpochFlow: Flow<Long?>

    suspend fun saveUserId(userId: Long)
    suspend fun saveUserName(userName: String)
    suspend fun setOnboardingCompleted()
    suspend fun markUpdateNoticeDontShow(version: String)
    suspend fun markUpdateNoticeShownToday()
    suspend fun clearUserData()

    suspend fun getUserInfo(userId: Long): Result<User>
    suspend fun updateUserInfo(userId: Long, editUser: EditUser): Result<Unit>
}
