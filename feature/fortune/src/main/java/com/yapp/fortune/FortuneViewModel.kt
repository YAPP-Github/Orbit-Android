package com.yapp.fortune

import android.app.Application
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.ImageRepository
import com.yapp.domain.repository.UserDataRepository
import com.yapp.fortune.page.toFortunePages
import com.yapp.media.decoder.ImageUtils
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FortuneViewModel @Inject constructor(
    private val application: Application,
    private val fortuneRepository: FortuneRepository,
    private val imageRepository: ImageRepository,
    private val userDataRepository: UserDataRepository,
) : BaseViewModel<FortuneContract.State, FortuneContract.SideEffect>(
    FortuneContract.State(),
) {

    init {
        viewModelScope.launch {
            val fortuneId = userDataRepository.fortuneIdFlow.firstOrNull()
            val firstDismissedAlarmId = userDataRepository.firstDismissedAlarmIdFlow.firstOrNull()
            val fortuneDate = userDataRepository.fortuneDateFlow.firstOrNull()
            fortuneId?.let { getFortune(it, firstDismissedAlarmId, fortuneDate) }
        }
    }
    private fun getFortune(fortuneId: Long, firstDismissedAlarmId: Long?, fortuneDate: String?) = intent {
        updateState { copy(isLoading = true) }

        fortuneRepository.getFortune(fortuneId).onSuccess { fortune ->
            val savedImageId = userDataRepository.fortuneImageIdFlow.firstOrNull()
            val imageId = savedImageId ?: getRandomImage()

            val formattedTitle = fortune.dailyFortuneTitle.replace(",", ",\n").trim()
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val hasReward = (fortuneDate == todayDate) && (firstDismissedAlarmId != null)
            updateState {
                copy(
                    isLoading = false,
                    dailyFortuneTitle = formattedTitle,
                    dailyFortuneDescription = fortune.dailyFortuneDescription,
                    avgFortuneScore = fortune.avgFortuneScore,
                    fortunePages = fortune.toFortunePages(),
                    fortuneImageId = imageId,
                    hasReward = hasReward,
                )
            }
        }.onFailure { error ->
            Log.e("FortuneViewModel", "운세 데이터 요청 실패: ${error.message}")
            updateState { copy(isLoading = false) }
        }
    }

    fun saveFortuneImageIdIfNeeded(imageId: Int) = viewModelScope.launch {
        val savedImageId = userDataRepository.fortuneImageIdFlow.firstOrNull()
        if (savedImageId == null || savedImageId != imageId) {
            userDataRepository.saveFortuneImageId(imageId)
        }
    }

    fun onAction(action: FortuneContract.Action) = intent {
        when (action) {
            is FortuneContract.Action.NextStep -> {
                if (state.hasReward) {
                    postSideEffect(FortuneContract.SideEffect.NavigateToFortuneReward)
                } else {
                    reduce { state.copy(currentStep = (state.currentStep + 1).coerceAtMost(5)) }
                }
            }
            is FortuneContract.Action.UpdateStep -> {
                reduce { state.copy(currentStep = action.step) }
            }
            is FortuneContract.Action.NavigateToHome -> {
                navigateToHome()
            }
            is FortuneContract.Action.SaveImage -> {
                saveImage(action.resId)
            }
        }
    }

    private fun navigateToHome() {
        emitSideEffect(FortuneContract.SideEffect.NavigateToHome)
    }

    private fun saveImage(@DrawableRes resId: Int) = viewModelScope.launch {
        val bitmap = ImageUtils.getBitmapFromResource(application, resId)
        val byteArray = ImageUtils.bitmapToByteArray(bitmap)

        val isSuccess = imageRepository.saveImage(byteArray)

        if (isSuccess) {
            emitSideEffect(
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
