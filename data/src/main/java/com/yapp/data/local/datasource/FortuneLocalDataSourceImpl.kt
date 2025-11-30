package com.yapp.data.local.datasource

import com.yapp.datastore.FortunePreferences
import javax.inject.Inject

class FortuneLocalDataSourceImpl @Inject constructor(
    private val fortunePreferences: FortunePreferences,
) : FortuneLocalDataSource {

    override val fortuneIdFlow = fortunePreferences.fortuneIdFlow
    override val fortuneDateEpochFlow = fortunePreferences.fortuneDateEpochFlow
    override val fortuneImageIdFlow = fortunePreferences.fortuneImageIdFlow
    override val fortuneScoreFlow = fortunePreferences.fortuneScoreFlow
    override val hasUnseenFortuneFlow = fortunePreferences.hasUnseenFortuneFlow
    override val shouldShowFortuneToolTipFlow = fortunePreferences.shouldShowFortuneToolTipFlow
    override val isFirstAlarmDismissedTodayFlow = fortunePreferences.isFirstAlarmDismissedTodayFlow

    override suspend fun markFortuneCreated(fortuneId: Long) {
        fortunePreferences.markFortuneCreated(fortuneId)
    }

    override suspend fun markFortuneSeen() {
        fortunePreferences.markFortuneSeen()
    }

    override suspend fun markFortuneTooltipShown() {
        fortunePreferences.markFortuneTooltipShown()
    }

    override suspend fun saveFortuneImageId(imageResId: Int) {
        fortunePreferences.saveFortuneImageId(imageResId)
    }

    override suspend fun saveFortuneScore(score: Int) {
        fortunePreferences.saveFortuneScore(score)
    }

    override suspend fun markFirstAlarmDismissedToday() {
        fortunePreferences.markFirstAlarmDismissedToday()
    }

    override suspend fun clearFortuneData() {
        fortunePreferences.clearFortuneData()
    }

    override suspend fun hasTodayFortune(): Boolean {
        return fortunePreferences.hasTodayFortune()
    }
}
