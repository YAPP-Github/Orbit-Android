package com.yapp.domain.repository

import com.yapp.domain.model.Fortune
import kotlinx.coroutines.flow.Flow

interface FortuneRepository {
    val fortuneIdFlow: Flow<Long?>
    val fortuneDateEpochFlow: Flow<Long?>
    val fortuneImageIdFlow: Flow<Int?>
    val fortuneScoreFlow: Flow<Int?>
    val hasUnseenFortuneFlow: Flow<Boolean>
    val shouldShowFortuneToolTipFlow: Flow<Boolean>
    val isFirstAlarmDismissedTodayFlow: Flow<Boolean>

    suspend fun markFortuneAsCreated(fortuneId: Long)
    suspend fun markFortuneSeen()
    suspend fun markFortuneTooltipShown()
    suspend fun saveFortuneImageId(imageResId: Int)
    suspend fun saveFortuneScore(score: Int)
    suspend fun markFirstAlarmDismissedToday()

    suspend fun clearFortuneData()

    suspend fun hasTodayFortune(): Boolean

    suspend fun postFortune(userId: Long): Result<Fortune>
    suspend fun getFortune(fortuneId: Long): Result<Fortune>
}
