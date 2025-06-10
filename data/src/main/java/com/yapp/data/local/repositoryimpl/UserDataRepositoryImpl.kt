package com.yapp.data.local.repositoryimpl

import com.yapp.data.local.datasource.UserLocalDataSource
import com.yapp.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
) : UserDataRepository {
    override val userIdFlow: Flow<Long?> = userLocalDataSource.userIdFlow
    override val userNameFlow: Flow<String?> = userLocalDataSource.userNameFlow
    override val onboardingCompletedFlow: Flow<Boolean> = userLocalDataSource.onboardingCompletedFlow
    override val fortuneIdFlow: Flow<Long?> = userLocalDataSource.fortuneIdFlow
    override val fortuneDateFlow: Flow<String?> = userLocalDataSource.fortuneDateFlow
    override val fortuneImageIdFlow: Flow<Int?> = userLocalDataSource.fortuneImageIdFlow
    override val fortuneScoreFlow: Flow<Int?> = userLocalDataSource.fortuneScoreFlow
    override val hasNewFortuneFlow: Flow<Boolean> = userLocalDataSource.hasNewFortuneFlow
    override val firstDismissedAlarmIdFlow: Flow<Long?> = userLocalDataSource.firstDismissedAlarmIdFlow

    override suspend fun saveUserId(userId: Long) = userLocalDataSource.saveUserId(userId)
    override suspend fun saveUserName(userName: String) = userLocalDataSource.saveUserName(userName)
    override suspend fun setOnboardingCompleted() = userLocalDataSource.setOnboardingCompleted()
    override suspend fun saveFortuneId(fortuneId: Long) = userLocalDataSource.saveFortuneId(fortuneId)
    override suspend fun markFortuneAsChecked() = userLocalDataSource.markFortuneAsChecked()
    override suspend fun saveFortuneImageId(imageResId: Int) = userLocalDataSource.saveFortuneImageId(imageResId)
    override suspend fun saveFortuneScore(score: Int) = userLocalDataSource.saveFortuneScore(score)
    override suspend fun saveFirstDismissedAlarmId(alarmId: Long) = userLocalDataSource.saveFirstDismissedAlarmId(alarmId)
    override suspend fun clearDismissedAlarmId() = userLocalDataSource.clearDismissedAlarmId()
    override suspend fun clearUserData() = userLocalDataSource.clearUserData()
    override suspend fun clearFortuneId() = userLocalDataSource.clearFortuneId()
}
