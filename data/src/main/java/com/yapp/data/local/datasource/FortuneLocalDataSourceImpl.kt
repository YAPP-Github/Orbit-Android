package com.yapp.data.local.datasource

import com.yapp.datastore.UserPreferences
import com.yapp.domain.model.FortuneCreateStatus
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    override val fortuneCreateStatusFlow = combine(
        userPreferences.fortuneIdFlow,
        userPreferences.fortuneDateFlow,
        userPreferences.isFortuneCreatingFlow,
        userPreferences.isFortuneFailedFlow,
    ) { fortuneId, fortuneDate, isCreating, isFailed ->
        when {
            isFailed -> FortuneCreateStatus.Failure
            isCreating -> FortuneCreateStatus.Creating
            fortuneId != null && fortuneDate == today() -> FortuneCreateStatus.Success(fortuneId)
            else -> FortuneCreateStatus.Idle
        }
    }.distinctUntilChanged()

    private fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

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

    override suspend fun markFirstAlarmDismissedToday() {
        userPreferences.markFirstAlarmDismissedToday()
    }

    override suspend fun clearFortuneData() {
        userPreferences.clearFortuneData()
    }
}
