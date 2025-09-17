package com.yapp.data.local.datasource

import com.yapp.domain.model.FortuneCreateStatus
import kotlinx.coroutines.flow.Flow

interface FortuneLocalDataSource {
    val fortuneIdFlow: Flow<Long?>
    val fortuneDateEpochFlow: Flow<Long?>
    val fortuneImageIdFlow: Flow<Int?>
    val fortuneScoreFlow: Flow<Int?>
    val hasUnseenFortuneFlow: Flow<Boolean>
    val shouldShowFortuneToolTipFlow: Flow<Boolean>
    val isFirstAlarmDismissedTodayFlow: Flow<Boolean>

    val fortuneCreateStatusFlow: Flow<FortuneCreateStatus>

    suspend fun markFortuneCreating(attemptId: String, leaseMillis: Long)
    suspend fun markFortuneCreated(attemptId: String, fortuneId: Long)
    suspend fun markFortuneFailed(attemptId: String)
    suspend fun markFortuneSeen()
    suspend fun markFortuneTooltipShown()
    suspend fun saveFortuneImageId(imageResId: Int)
    suspend fun saveFortuneScore(score: Int)
    suspend fun markFirstAlarmDismissedToday()

    suspend fun clearFortuneData()
}
