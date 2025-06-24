package com.yapp.data.local.datasource

import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    val userIdFlow: Flow<Long?>
    val userNameFlow: Flow<String?>
    val onboardingCompletedFlow: Flow<Boolean>

    suspend fun saveUserId(userId: Long)
    suspend fun saveUserName(userName: String)
    suspend fun setOnboardingCompleted()
    suspend fun clearUserData()
}
