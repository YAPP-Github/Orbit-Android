package com.yapp.fortune.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.UserInfoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class PostFortuneWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val fortuneRepository: FortuneRepository,
    private val userInfoRepository: UserInfoRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val hasUnseenFortune = fortuneRepository.hasUnseenFortuneFlow.first()
        val creating = fortuneRepository.isFortuneCreatingFlow.first()

        if (hasUnseenFortune || creating) {
            return Result.success()
        }

        val userId = userInfoRepository.userIdFlow.firstOrNull() ?: return Result.failure()

        return try {
            fortuneRepository.markFortuneAsCreating()
            val result = fortuneRepository.postFortune(userId)
            result.fold(
                onSuccess = { fortune ->
                    fortuneRepository.markFortuneAsCreated(fortune.id)
                    fortuneRepository.saveFortuneScore(fortune.avgFortuneScore)
                    Result.success()
                },
                onFailure = { e ->
                    fortuneRepository.markFortuneAsFailed()
                    Result.retry()
                },
            )
        } catch (_: Throwable) {
            fortuneRepository.markFortuneAsFailed()
            Result.retry()
        }
    }
}
