package com.yapp.data.local.datasource

import kotlinx.coroutines.flow.Flow

interface FortuneLocalDataSource {
    val fortuneIdFlow: Flow<Long?>
    val fortuneDateFlow: Flow<String?>
    val fortuneImageIdFlow: Flow<Int?>
    val fortuneScoreFlow: Flow<Int?>
    val hasUnseenFortuneFlow: Flow<Boolean>
    val shouldShowFortuneToolTipFlow: Flow<Boolean>
    val isFortuneCreatingFlow: Flow<Boolean>
    val isFortuneFailedFlow: Flow<Boolean>

    suspend fun tryMarkFortuneCreating(): Boolean
    suspend fun markFortuneCreating()
    suspend fun markFortuneCreated(fortuneId: Long)
    suspend fun markFortuneFailed()
    suspend fun markFortuneSeen()
    suspend fun markFortuneTooltipShown()
    suspend fun saveFortuneImageId(imageResId: Int)
    suspend fun saveFortuneScore(score: Int)

    suspend fun clearFortuneData()
}
