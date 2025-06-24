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
    override val hasNewFortuneFlow = userPreferences.hasNewFortuneFlow
    override val firstDismissedAlarmIdFlow = userPreferences.firstDismissedAlarmIdFlow

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

    override suspend fun clearFortuneId() {
        userPreferences.clearFortuneId()
    }
}
