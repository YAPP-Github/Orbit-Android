package com.yapp.domain.repository

import com.yapp.domain.model.Fortune
import kotlinx.coroutines.flow.Flow

interface FortuneRepository {
    val fortuneIdFlow: Flow<Long?>
    val fortuneDateFlow: Flow<String?>
    val fortuneImageIdFlow: Flow<Int?>
    val fortuneScoreFlow: Flow<Int?>
    val hasUnseenFortuneFlow: Flow<Boolean>
    val shouldShowFortuneToolTipFlow: Flow<Boolean>
    val isFirstAlarmDismissedTodayFlow: Flow<Boolean>
    val isFortuneCreatingFlow: Flow<Boolean>
    val isFortuneFailedFlow: Flow<Boolean>

    suspend fun markFortuneAsCreating()
    suspend fun markFortuneAsCreated(fortuneId: Long)
    suspend fun markFortuneAsFailed()
    suspend fun markFortuneSeen()
    suspend fun markFortuneTooltipShown()
    suspend fun saveFortuneImageId(imageResId: Int)
    suspend fun saveFortuneScore(score: Int)

    suspend fun saveFirstAlarmDismissedToday(firstAlarmId: Long)
    suspend fun clearFirstAlarmDismissedToday()

    suspend fun clearFortuneData()

    suspend fun postFortune(userId: Long): Result<Fortune>
    suspend fun getFortune(fortuneId: Long): Result<Fortune>
}
