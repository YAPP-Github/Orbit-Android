package com.yapp.data.local.datasource

import com.yapp.datastore.FortunePreferences
import com.yapp.domain.model.FortuneCreateStatus
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
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

    override val fortuneCreateStatusFlow = combine(
        fortunePreferences.fortuneIdFlow,
        fortunePreferences.fortuneDateEpochFlow,
        fortunePreferences.isFortuneCreatingFlow,
        fortunePreferences.isFortuneFailedFlow,
    ) { fortuneId, fortuneDate, isCreating, isFailed ->
        when {
            isFailed -> FortuneCreateStatus.Failure
            isCreating -> FortuneCreateStatus.Creating
            fortuneId != null && fortuneDate == todayEpoch() -> FortuneCreateStatus.Success(fortuneId)
            else -> FortuneCreateStatus.Idle
        }
    }.distinctUntilChanged()

    private fun todayEpoch(): Long = LocalDate.now().toEpochDay()

    override suspend fun markFortuneCreating() {
        fortunePreferences.markFortuneCreating()
    }

    override suspend fun markFortuneCreated(fortuneId: Long) {
        fortunePreferences.markFortuneCreated(fortuneId)
    }

    override suspend fun markFortuneFailed() {
        fortunePreferences.markFortuneFailed()
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
}
