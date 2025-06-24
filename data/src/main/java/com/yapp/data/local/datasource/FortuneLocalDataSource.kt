package com.yapp.data.local.datasource

import kotlinx.coroutines.flow.Flow

interface FortuneLocalDataSource {
    val fortuneIdFlow: Flow<Long?>
    val fortuneDateFlow: Flow<String?>
    val fortuneImageIdFlow: Flow<Int?>
    val fortuneScoreFlow: Flow<Int?>
    val hasNewFortuneFlow: Flow<Boolean>
    val firstDismissedAlarmIdFlow: Flow<Long?>

    suspend fun saveFortuneId(fortuneId: Long)
    suspend fun markFortuneAsChecked()
    suspend fun saveFortuneImageId(imageResId: Int)
    suspend fun saveFortuneScore(score: Int)
    suspend fun saveFirstDismissedAlarmId(alarmId: Long)
    suspend fun clearDismissedAlarmId()
    suspend fun clearFortuneId()
}
