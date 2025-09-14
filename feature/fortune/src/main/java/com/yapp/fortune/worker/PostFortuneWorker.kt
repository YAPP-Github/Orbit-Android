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
        when (fortuneRepository.fortuneCreateStatusFlow.first()) {
            is FortuneCreateStatus.Creating,
            is FortuneCreateStatus.Success,
            -> {
                return Result.success()
            }
            FortuneCreateStatus.Failure,
            FortuneCreateStatus.Idle,
            -> {
                val userId = userInfoRepository.userIdFlow.firstOrNull()
                    ?: run {
                        // 사용자 없으면 실패 상태 표시 후 실패 반환
                        fortuneRepository.markFortuneAsFailed()
                        return Result.failure()
                    }

                return try {
                    fortuneRepository.markFortuneAsCreating()

                    val result = fortuneRepository.postFortune(userId)
                    result.fold(
                        onSuccess = { fortune ->
                            fortuneRepository.markFortuneAsCreated(fortune.id)
                            fortuneRepository.saveFortuneScore(fortune.avgFortuneScore)
                            Result.success()
                        },
                        onFailure = {
                            fortuneRepository.markFortuneAsFailed()
                            // WM 백오프 규칙에 따라 재시도
                            Result.retry()
                        },
                    )
                } catch (_: Throwable) {
                    fortuneRepository.markFortuneAsFailed()
                    Result.retry()
                }
            }
        }
    }
}
