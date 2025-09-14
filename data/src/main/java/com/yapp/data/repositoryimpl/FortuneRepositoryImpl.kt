package com.yapp.data.repositoryimpl

import com.yapp.data.local.datasource.FortuneLocalDataSource
import com.yapp.data.remote.datasource.FortuneDataSource
import com.yapp.data.remote.dto.response.toDomain
import com.yapp.domain.model.Fortune
import com.yapp.domain.model.FortuneCreateStatus
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
    override val hasUnseenFortuneFlow: Flow<Boolean> = fortuneLocalDataSource.hasUnseenFortuneFlow
    override val shouldShowFortuneToolTipFlow: Flow<Boolean> = fortuneLocalDataSource.shouldShowFortuneToolTipFlow
    override val isFirstAlarmDismissedTodayFlow: Flow<Boolean> = fortuneLocalDataSource.isFirstAlarmDismissedTodayFlow

    override val fortuneCreateStatusFlow: Flow<FortuneCreateStatus> = fortuneLocalDataSource.fortuneCreateStatusFlow

    override suspend fun markFortuneAsCreating() = fortuneLocalDataSource.markFortuneCreating()
    override suspend fun markFortuneAsCreated(fortuneId: Long) = fortuneLocalDataSource.markFortuneCreated(fortuneId)
    override suspend fun markFortuneAsFailed() = fortuneLocalDataSource.markFortuneFailed()
    override suspend fun markFortuneSeen() = fortuneLocalDataSource.markFortuneSeen()
    override suspend fun markFortuneTooltipShown() = fortuneLocalDataSource.markFortuneTooltipShown()
    override suspend fun saveFortuneImageId(imageResId: Int) = fortuneLocalDataSource.saveFortuneImageId(imageResId)
    override suspend fun saveFortuneScore(score: Int) = fortuneLocalDataSource.saveFortuneScore(score)
    override suspend fun markFirstAlarmDismissedToday() = fortuneLocalDataSource.markFirstAlarmDismissedToday()

    override suspend fun clearFortuneData() = fortuneLocalDataSource.clearFortuneData()

    override suspend fun postFortune(userId: Long): Result<Fortune> {
        return fortuneRemoteDataSource.postFortune(userId)
            .mapCatching { it.toDomain() }
    }

    override suspend fun getFortune(fortuneId: Long): Result<Fortune> {
        return fortuneRemoteDataSource.getFortune(fortuneId)
            .mapCatching { it.toDomain() }
    }
}
