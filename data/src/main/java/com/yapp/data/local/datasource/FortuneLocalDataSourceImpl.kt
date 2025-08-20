package com.yapp.data.local.datasource

import com.yapp.datastore.UserPreferences
import javax.inject.Inject

class FortuneLocalDataSourceImpl @Inject constructor(
    private val userPreferences: UserPreferences,
) : FortuneLocalDataSource {

    override val fortuneIdFlow = userPreferences.fortuneIdFlow
    override val fortuneDateFlow = userPreferences.fortuneDateFlow
    override val fortuneImageIdFlow = userPreferences.fortuneImageIdFlow
    override val fortuneScoreFlow = userPreferences.fortuneScoreFlow
    override val hasUnseenFortuneFlow = userPreferences.hasUnseenFortuneFlow
    override val shouldShowFortuneToolTipFlow = userPreferences.shouldShowFortuneToolTipFlow
    override val isFirstAlarmDismissedTodayFlow = userPreferences.isFirstAlarmDismissedTodayFlow
    override val isFortuneCreatingFlow = userPreferences.isFortuneCreatingFlow
    override val isFortuneFailedFlow = userPreferences.isFortuneFailedFlow

    override suspend fun markFortuneCreating() {
        userPreferences.markFortuneCreating()
    }

    override suspend fun markFortuneCreated(fortuneId: Long) {
        userPreferences.markFortuneCreated(fortuneId)
    }

    override suspend fun markFortuneFailed() {
        userPreferences.markFortuneFailed()
    }

    override suspend fun markFortuneSeen() {
        userPreferences.markFortuneSeen()
    }

    override suspend fun markFortuneTooltipShown() {
        userPreferences.markFortuneTooltipShown()
    }

    override suspend fun saveFortuneImageId(imageResId: Int) {
        userPreferences.saveFortuneImageId(imageResId)
    }

    override suspend fun saveFortuneScore(score: Int) {
        userPreferences.saveFortuneScore(score)
    }

    override suspend fun saveFirstAlarmDismissedToday(firstAlarmId: Long) {
        userPreferences.saveFirstAlarmDismissedToday(firstAlarmId)
    }

    override suspend fun clearFirstAlarmDismissedToday() {
        userPreferences.clearFirstAlarmDismissedToday()
    }

    override suspend fun clearFortuneData() {
        userPreferences.clearFortuneData()
    }
}
