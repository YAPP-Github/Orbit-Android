package com.yapp.fortune

import android.app.Application
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.yapp.domain.model.FortuneCreateStatus
import com.yapp.domain.repository.FortuneRepository
import com.yapp.fortune.page.toFortunePages
import com.yapp.media.decoder.ImageUtils
import com.yapp.media.storage.ImageSaver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FortuneViewModel @Inject constructor(
    private val application: Application,
    private val fortuneRepository: FortuneRepository,
    private val imageSaver: ImageSaver,
) : ViewModel(), ContainerHost<FortuneContract.State, FortuneContract.SideEffect> {

    override val container: Container<FortuneContract.State, FortuneContract.SideEffect> = container(
        initialState = FortuneContract.State(),
    ) {
        observeFortune()
    }

    fun processAction(action: FortuneContract.Action) {
        when (action) {
            is FortuneContract.Action.NextStep -> {
                moveToNextStep()
            }
            is FortuneContract.Action.UpdateStep -> {
                updateStep(action.step)
            }
            is FortuneContract.Action.NavigateToHome -> {
                navigateToHome()
            }
            is FortuneContract.Action.SaveImage -> {
                saveImage(action.resId)
            }
        }
    }

    private fun observeFortune() = intent {
        fortuneRepository.fortuneCreateStatusFlow.collect { status ->
            when (status) {
                is FortuneCreateStatus.Creating -> {
                    reduce { state.copy(isLoading = true) }
                }

                is FortuneCreateStatus.Success -> {
                    fetchAndUpdateFortune(
                        fortuneId = status.fortuneId,
                        isFirstAlarmDismissedToday = fortuneRepository.isFirstAlarmDismissedTodayFlow.first(),
                    )
                }

                is FortuneCreateStatus.Failure -> {
                    reduce {
                        state.copy(
                            isLoading = false,
                            isCreateFailureDialogVisible = true,
                        )
                    }
                }

                is FortuneCreateStatus.Idle -> {
                    if (!state.isCreateFailureDialogVisible) {
                        postSideEffect(FortuneContract.SideEffect.NavigateToHome)
                    }
                }
            }
        }
    }

    private fun fetchAndUpdateFortune(
        fortuneId: Long,
        isFirstAlarmDismissedToday: Boolean,
    ) = intent {
        reduce { state.copy(isLoading = true) }

        fortuneRepository.getFortune(fortuneId).onSuccess { fortune ->
            val savedImageId = fortuneRepository.fortuneImageIdFlow.firstOrNull()
            val imageId = savedImageId ?: getRandomImage()

            val formattedTitle = fortune.dailyFortuneTitle.replace(",", ",\n").trim()

            fortuneRepository.markFortuneSeen()

            reduce {
                state.copy(
                    isLoading = false,
                    dailyFortuneTitle = formattedTitle,
                    dailyFortuneDescription = fortune.dailyFortuneDescription,
                    avgFortuneScore = fortune.avgFortuneScore,
                    fortunePages = fortune.toFortunePages(),
                    fortuneImageId = imageId,
                    hasReward = isFirstAlarmDismissedToday,
                    isCreateFailureDialogVisible = false,
                )
            }
        }.onFailure { error ->
            Log.e("FortuneViewModel", "운세 데이터 요청 실패: ${error.message}")
            reduce { state.copy(isLoading = false) }
        }
    }

    fun saveFortuneImageIdIfNeeded(imageId: Int) = intent {
        val savedImageId = fortuneRepository.fortuneImageIdFlow.firstOrNull()
        if (savedImageId == null || savedImageId != imageId) {
            fortuneRepository.saveFortuneImageId(imageId)
        }
    }

    private fun moveToNextStep() = intent {
        if (state.hasReward) {
            postSideEffect(FortuneContract.SideEffect.NavigateToFortuneReward)
        } else {
            reduce { state.copy(currentStep = (state.currentStep + 1).coerceAtMost(5)) }
        }
    }

    private fun updateStep(step: Int) = intent {
        reduce { state.copy(currentStep = step) }
    }

    private fun navigateToHome() = intent {
        if (state.isCreateFailureDialogVisible) {
            reduce { state.copy(isCreateFailureDialogVisible = false) }
        }

        postSideEffect(FortuneContract.SideEffect.NavigateToHome)
    }

    private fun saveImage(@DrawableRes resId: Int) = intent {
        val bitmap = ImageUtils.getBitmapFromResource(application, resId)
        val byteArray = ImageUtils.bitmapToByteArray(bitmap)

        val isSuccess = imageSaver.saveImage(byteArray, "fortune_${System.currentTimeMillis()}.png")

        if (isSuccess) {
            postSideEffect(
                FortuneContract.SideEffect.ShowSnackBar(
                    message = "앨범에 저장되었습니다.",
                    iconRes = core.designsystem.R.drawable.ic_check_green,
                    onDismiss = {},
                    onAction = {},
                ),
            )
        } else {
            Log.e("FortuneViewModel", "이미지 저장 실패")
        }
    }

    fun getRandomImage(): Int {
        return listOf(
            core.designsystem.R.drawable.ic_fortune_reward1,
            core.designsystem.R.drawable.ic_fortune_reward2,
            core.designsystem.R.drawable.ic_fortune_reward3,
            core.designsystem.R.drawable.ic_fortune_reward4,
            core.designsystem.R.drawable.ic_fortune_reward5,
        ).random()
    }
}
