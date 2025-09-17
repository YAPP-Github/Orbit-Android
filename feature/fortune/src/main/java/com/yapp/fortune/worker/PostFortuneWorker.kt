package com.yapp.fortune.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yapp.domain.model.FortuneCreateStatus
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.UserInfoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

@HiltWorker
class PostFortuneWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val fortuneRepository: FortuneRepository,
    private val userInfoRepository: UserInfoRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // 이미 진행 중이거나(다른 워커) 오늘 운세가 성공 상태면 중복 실행 방지
        when (fortuneRepository.fortuneCreateStatusFlow.first()) {
            is FortuneCreateStatus.Creating,
            is FortuneCreateStatus.Success,
            -> return Result.success()
            FortuneCreateStatus.Failure,
            FortuneCreateStatus.Idle,
            -> { /* 계속 진행 */ }
        }

        val userId = userInfoRepository.userIdFlow.firstOrNull()
            ?: run {
                return Result.failure()
            }

        val attemptId = UUID.randomUUID().toString()

        return try {
            fortuneRepository.markFortuneAsCreating(attemptId)

            val result = fortuneRepository.postFortune(userId)

            result.fold(
                onSuccess = { fortune ->
                    fortuneRepository.markFortuneAsCreated(attemptId, fortune.id)
                    fortuneRepository.saveFortuneScore(fortune.avgFortuneScore)
                    Result.success()
                },
                onFailure = {
                    fortuneRepository.markFortuneAsFailed(attemptId)
                    Result.retry()
                },
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            fortuneRepository.markFortuneAsFailed(attemptId)
            Result.retry()
        }
    }
}
