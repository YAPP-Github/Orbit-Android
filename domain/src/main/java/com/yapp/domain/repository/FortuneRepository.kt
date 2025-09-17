package com.yapp.domain.repository

import com.yapp.domain.model.Fortune
import com.yapp.domain.model.FortuneCreateStatus
import kotlinx.coroutines.flow.Flow

interface FortuneRepository {
    val fortuneIdFlow: Flow<Long?>
    val fortuneDateEpochFlow: Flow<Long?>
    val fortuneImageIdFlow: Flow<Int?>
    val fortuneScoreFlow: Flow<Int?>
    val hasUnseenFortuneFlow: Flow<Boolean>
    val shouldShowFortuneToolTipFlow: Flow<Boolean>
    val isFirstAlarmDismissedTodayFlow: Flow<Boolean>

    val fortuneCreateStatusFlow: Flow<FortuneCreateStatus>

    suspend fun markFortuneAsCreating(attemptId: String, leaseMillis: Long = 2 * 60_000L)
    suspend fun markFortuneAsCreated(attemptId: String, fortuneId: Long)
    suspend fun markFortuneAsFailed(attemptId: String)
    suspend fun markFortuneSeen()
    suspend fun markFortuneTooltipShown()
    suspend fun saveFortuneImageId(imageResId: Int)
    suspend fun saveFortuneScore(score: Int)
    suspend fun markFirstAlarmDismissedToday()

    suspend fun clearFortuneData()

    suspend fun postFortune(userId: Long): Result<Fortune>
    suspend fun getFortune(fortuneId: Long): Result<Fortune>
}
