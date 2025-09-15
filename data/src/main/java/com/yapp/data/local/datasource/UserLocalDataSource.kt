package com.yapp.data.local.datasource

import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
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
}
