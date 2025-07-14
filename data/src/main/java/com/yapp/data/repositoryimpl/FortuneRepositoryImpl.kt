package com.yapp.data.repositoryimpl

import com.yapp.data.local.datasource.FortuneLocalDataSource
import com.yapp.data.remote.datasource.FortuneDataSource
import com.yapp.data.remote.dto.response.toDomain
import com.yapp.domain.model.Fortune
import com.yapp.domain.repository.FortuneRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FortuneRepositoryImpl @Inject constructor(
    private val fortuneLocalDataSource: FortuneLocalDataSource,
    private val fortuneRemoteDataSource: FortuneDataSource,
) : FortuneRepository {
    override val fortuneIdFlow: Flow<Long?> = fortuneLocalDataSource.fortuneIdFlow
    override val fortuneDateFlow: Flow<String?> = fortuneLocalDataSource.fortuneDateFlow
    override val fortuneImageIdFlow: Flow<Int?> = fortuneLocalDataSource.fortuneImageIdFlow
    override val fortuneScoreFlow: Flow<Int?> = fortuneLocalDataSource.fortuneScoreFlow
    override val hasNewFortuneFlow: Flow<Boolean> = fortuneLocalDataSource.hasNewFortuneFlow
    override val firstDismissedAlarmIdFlow: Flow<Long?> = fortuneLocalDataSource.firstDismissedAlarmIdFlow

    override suspend fun saveFortuneId(fortuneId: Long) = fortuneLocalDataSource.saveFortuneId(fortuneId)
    override suspend fun markFortuneAsChecked() = fortuneLocalDataSource.markFortuneAsChecked()
    override suspend fun saveFortuneImageId(imageResId: Int) = fortuneLocalDataSource.saveFortuneImageId(imageResId)
    override suspend fun saveFortuneScore(score: Int) = fortuneLocalDataSource.saveFortuneScore(score)
    override suspend fun saveFirstDismissedAlarmId(alarmId: Long) = fortuneLocalDataSource.saveFirstDismissedAlarmId(alarmId)
    override suspend fun clearDismissedAlarmId() = fortuneLocalDataSource.clearDismissedAlarmId()
    override suspend fun clearFortuneId() = fortuneLocalDataSource.clearFortuneId()

    override suspend fun postFortune(userId: Long): Result<Fortune> {
        return fortuneRemoteDataSource.postFortune(userId)
            .mapCatching { fortuneResponse ->
                fortuneResponse.toDomain()
            }
    }

    override suspend fun getFortune(fortuneId: Long): Result<Fortune> {
        return fortuneRemoteDataSource.getFortune(fortuneId)
            .mapCatching { fortuneResponse ->
                fortuneResponse.toDomain()
            }
    }
}
