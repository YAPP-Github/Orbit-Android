package com.yapp.domain.repository

import com.yapp.domain.model.Fortune
import kotlinx.coroutines.flow.Flow

interface FortuneRepository {
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
    suspend fun postFortune(userId: Long): Result<Fortune>
    suspend fun getFortune(fortuneId: Long): Result<Fortune>
}
