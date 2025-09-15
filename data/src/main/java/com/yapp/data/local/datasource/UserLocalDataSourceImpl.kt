package com.yapp.data.local.datasource

import com.yapp.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserLocalDataSourceImpl @Inject constructor(
    private val userPreferences: UserPreferences,
) : UserLocalDataSource {

    override val userIdFlow: Flow<Long?> = userPreferences.userIdFlow
    override val userNameFlow: Flow<String?> = userPreferences.userNameFlow
    override val onboardingCompletedFlow: Flow<Boolean> = userPreferences.onboardingCompletedFlow
    override val updateBottomSheetDontShowVersionFlow: Flow<String?> = userPreferences.updateBottomSheetDontShowVersionFlow
    override val updateBottomSheetLastClosedDateFlow: Flow<String?> = userPreferences.updateBottomSheetLastClosedDateFlow

    override suspend fun saveUserId(userId: Long) {
        userPreferences.saveUserId(userId)
    }

    override suspend fun saveUserName(userName: String) {
        userPreferences.saveUserName(userName)
    }

    override suspend fun setOnboardingCompleted() {
        userPreferences.setOnboardingCompleted()
    }

    override suspend fun markUpdateBottomSheetDontShow(version: String) {
        userPreferences.markUpdateBottomSheetDontShow(version)
    }

    override suspend fun markUpdateBottomSheetClosedToday() {
        userPreferences.markUpdateBottomSheetClosedToday()
    }

    override suspend fun clearUserData() {
        userPreferences.clearUserData()
    }
}
