package com.yapp.data.local.datasource

import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    val userIdFlow: Flow<Long?>
    val userNameFlow: Flow<String?>
    val onboardingCompletedFlow: Flow<Boolean>
    val updateBottomSheetDontShowVersionFlow: Flow<String?>
    val updateBottomSheetLastClosedDateFlow: Flow<String?>

    suspend fun saveUserId(userId: Long)
    suspend fun saveUserName(userName: String)
    suspend fun setOnboardingCompleted()
    suspend fun markUpdateBottomSheetDontShow(version: String)
    suspend fun markUpdateBottomSheetClosedToday()
    suspend fun clearUserData()
}
