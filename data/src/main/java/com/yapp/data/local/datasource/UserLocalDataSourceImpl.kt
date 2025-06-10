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
    override val fortuneIdFlow: Flow<Long?> = userPreferences.fortuneIdFlow
    override val fortuneDateFlow: Flow<String?> = userPreferences.fortuneDateFlow
    override val fortuneImageIdFlow: Flow<Int?> = userPreferences.fortuneImageIdFlow
    override val fortuneScoreFlow: Flow<Int?> = userPreferences.fortuneScoreFlow
    override val hasNewFortuneFlow: Flow<Boolean> = userPreferences.hasNewFortuneFlow
    override val firstDismissedAlarmIdFlow: Flow<Long?> = userPreferences.firstDismissedAlarmIdFlow

    override suspend fun saveUserId(userId: Long) {
        userPreferences.saveUserId(userId)
    }

    override suspend fun saveUserName(userName: String) {
        userPreferences.saveUserName(userName)
    }

    override suspend fun setOnboardingCompleted() {
        userPreferences.setOnboardingCompleted()
    }

    override suspend fun saveFortuneId(fortuneId: Long) {
        userPreferences.saveFortuneId(fortuneId)
    }

    override suspend fun markFortuneAsChecked() {
        userPreferences.markFortuneAsChecked()
    }

    override suspend fun saveFortuneImageId(imageResId: Int) {
        userPreferences.saveFortuneImageId(imageResId)
    }

    override suspend fun saveFortuneScore(score: Int) {
        userPreferences.saveFortuneScore(score)
    }

    override suspend fun saveFirstDismissedAlarmId(alarmId: Long) {
        userPreferences.saveFirstDismissedAlarmId(alarmId)
    }

    override suspend fun clearDismissedAlarmId() {
        userPreferences.clearDismissedAlarmId()
    }

    override suspend fun clearUserData() {
        userPreferences.clearUserData()
    }

    override suspend fun clearFortuneId() {
        userPreferences.clearFortuneId()
    }
}
