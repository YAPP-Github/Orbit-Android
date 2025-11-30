package com.yapp.data.local.datasource

import kotlinx.coroutines.flow.Flow

interface FortuneLocalDataSource {
    val fortuneIdFlow: Flow<Long?>
    val fortuneDateEpochFlow: Flow<Long?>
    val fortuneImageIdFlow: Flow<Int?>
    val fortuneScoreFlow: Flow<Int?>
    val hasUnseenFortuneFlow: Flow<Boolean>
    val shouldShowFortuneToolTipFlow: Flow<Boolean>
    val isFirstAlarmDismissedTodayFlow: Flow<Boolean>

    suspend fun markFortuneCreated(fortuneId: Long)
    suspend fun markFortuneSeen()
    suspend fun markFortuneTooltipShown()
    suspend fun saveFortuneImageId(imageResId: Int)
    suspend fun saveFortuneScore(score: Int)
    suspend fun markFirstAlarmDismissedToday()

    suspend fun clearFortuneData()

    suspend fun hasTodayFortune(): Boolean
}
