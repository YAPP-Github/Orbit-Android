package com.yapp.onboarding

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.common.navigation.route.OnboardingDestination
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toRepeatDays
import com.yapp.domain.repository.SignUpRepository
import com.yapp.domain.repository.UserInfoRepository
import com.yapp.domain.usecase.AlarmUseCase
import com.yapp.media.haptic.HapticFeedbackManager
import com.yapp.media.haptic.HapticType
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalTime
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    private val signUpRepository: SignUpRepository,
    private val userInfoRepository: UserInfoRepository,
    private val alarmUseCase: AlarmUseCase,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), ContainerHost<OnboardingContract.State, OnboardingContract.SideEffect> {

    override val container: Container<OnboardingContract.State, OnboardingContract.SideEffect> = container(
        initialState = OnboardingContract.State(
            currentStep = savedStateHandle["currentStep"] ?: 1,
            birthDate = savedStateHandle["birthDate"] ?: "",
            birthType = savedStateHandle["birthType"] ?: "양력",
        ),
    )

    private val currentRoute: KClass<out OnboardingDestination>?
        get() = OnboardingDestination.routes.getOrNull(container.stateFlow.value.currentStep)

    fun processAction(action: OnboardingContract.Action) {
        when (action) {
            is OnboardingContract.Action.NextStep -> moveToNextStep()
            is OnboardingContract.Action.PreviousStep -> moveToPreviousStep()
            is OnboardingContract.Action.SetAlarmTime -> setAlarmTime(action.newTime)
            is OnboardingContract.Action.CreateAlarm -> createAlarm()
            is OnboardingContract.Action.UpdateField -> updateField(action.value, action.fieldType)
            is OnboardingContract.Action.UpdateBirthDate -> updateBirthDate(action.lunar, action.year, action.month, action.day)
            is OnboardingContract.Action.Reset -> resetFields()
            is OnboardingContract.Action.Submit -> submitUserInfo()
            is OnboardingContract.Action.UpdateGender -> updateGender(action.gender)
            is OnboardingContract.Action.ShowBottomSheet -> showBottomSheet()
            is OnboardingContract.Action.HideBottomSheet -> hideBottomSheet()
            is OnboardingContract.Action.CompleteOnboarding -> completeOnboarding()
            is OnboardingContract.Action.OpenWebView -> openWebView(action.url)
            is OnboardingContract.Action.ShowWarningDialog -> showWarningDialog()
            is OnboardingContract.Action.HideWarningDialog -> hideWarningDialog()
        }
    }

    private fun submitUserInfo() = intent {
        val result = signUpRepository.postSignUp(
            name = state.userName,
            calendarType = state.birthType,
            birthDate = state.birthDate,
            birthTime = state.birthTime,
            gender = state.selectedGender ?: "",
        )

        if (result.isSuccess) {
            val userId = result.getOrNull() ?: return@intent
            val userName = state.userName
            userInfoRepository.saveUserId(userId)
            userInfoRepository.saveUserName(userName)

            analyticsHelper.setUserId(userId)
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "onboarding_complete",
                    properties = mapOf(
                        AnalyticsEvent.OnboardingPropertiesKeys.STEP to "환영2",
                    ),
                ),
            )

            moveToNextStep()
        } else {
            showWarningDialog()
        }
    }

    private fun moveToNextStep() = intent {
        val currentStep = state.currentStep
        val nextStep = currentStep + 1
        val nextRoute = OnboardingDestination.getNextRouteForStep(currentStep)

        savedStateHandle["birthDate"] = state.birthDate
        savedStateHandle["birthType"] = state.birthType

        if (nextRoute != null) {
            savedStateHandle["currentStep"] = nextStep
            reduce { state.copy(currentStep = nextStep) }
            postSideEffect(OnboardingContract.SideEffect.NavigateToNextStep(currentStep))
        } else {
            postSideEffect(OnboardingContract.SideEffect.OnboardingCompleted)
        }
    }

    private fun moveToPreviousStep() = intent {
        val currentStep = state.currentStep
        if (currentStep > 1) {
            val previousStep = currentStep - 1
            savedStateHandle["currentStep"] = previousStep
            reduce { state.copy(currentStep = previousStep) }
            postSideEffect(OnboardingContract.SideEffect.NavigateBack)
        }
    }

    private fun setAlarmTime(newTime: LocalTime) = intent {
        hapticFeedbackManager.performHapticFeedback(HapticType.LIGHT_TICK)

        reduce { state.copy(selectedTime = newTime) }
    }

    private fun createAlarm() = intent {
        alarmUseCase.getAlarmSounds().onSuccess { sounds ->
            val defaultSoundIndex = sounds.indexOfFirst { it.title == "Homecoming" }.takeIf { it >= 0 } ?: 0
            val defaultSound = sounds[defaultSoundIndex]

            val newAlarm = Alarm(
                hour = state.selectedTime.hour,
                minute = state.selectedTime.minute,
                repeatDays = setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI).toRepeatDays(),
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                snoozeCount = 5,
                soundUri = defaultSound.uri,
            )

            alarmUseCase.insertAlarm(
                alarm = newAlarm,
            ).onFailure {
                Log.e("OnboardingViewModel", "Failed to create alarm", it)
            }
        }.onFailure {
            Log.e("OnboardingViewModel", "Failed to get alarm sounds", it)
        }
    }

    private fun updateField(value: String, fieldType: OnboardingContract.FieldType) = intent {
        when (fieldType) {
            OnboardingContract.FieldType.TIME -> {
                val isComplete = value.length == 5
                val isValid = isComplete && value.matches(fieldType.validationRegex)

                reduce {
                    state.copy(
                        textFieldValue = value,
                        birthTime = if (isValid) value else "",
                        showWarning = isComplete && !isValid,
                        isButtonEnabled = isValid,
                        isBirthTimeValid = isValid,
                        isValid = isValid,
                    )
                }
            }

            OnboardingContract.FieldType.NAME -> {
                val truncatedValue = OnboardingContract.truncateTextToLimit(value)
                val isValid = truncatedValue.matches(fieldType.validationRegex)

                reduce {
                    state.copy(
                        textFieldValue = truncatedValue,
                        userName = truncatedValue,
                        showWarning = !isValid,
                        isButtonEnabled = truncatedValue.isNotEmpty() && isValid,
                        isValid = isValid,
                    )
                }
            }
        }
    }

    private fun updateBirthDate(lunar: String, year: Int, month: Int, day: Int) = intent {
        if (currentRoute != OnboardingDestination.Birthday::class) return@intent

        val formattedDate = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

        hapticFeedbackManager.performHapticFeedback(HapticType.LIGHT_TICK)
        savedStateHandle["birthDate"] = formattedDate
        savedStateHandle["birthType"] = lunar

        reduce {
            state.copy(
                birthDate = formattedDate,
                birthType = lunar,
                isBirthDateValid = true,
            )
        }
    }

    private fun resetFields() = intent {
        reduce {
            state.copy(
                textFieldValue = "",
                showWarning = false,
                isButtonEnabled = false,
                isValid = false,
            )
        }
    }

    private fun updateGender(gender: String) = intent {
        reduce { state.copy(selectedGender = gender, isButtonEnabled = true) }
    }

    private fun showBottomSheet() = intent {
        postSideEffect(OnboardingContract.SideEffect.ShowBottomSheet)
    }

    private fun hideBottomSheet() = intent {
        postSideEffect(OnboardingContract.SideEffect.HideBottomSheet)
    }

    private fun completeOnboarding() = intent {
        userInfoRepository.setOnboardingCompleted()
        postSideEffect(OnboardingContract.SideEffect.OnboardingCompleted)
    }

    private fun openWebView(url: String) = intent {
        postSideEffect(OnboardingContract.SideEffect.OpenWebView(url))
    }

    private fun showWarningDialog() = intent {
        reduce { state.copy(isShowWarningDialog = true) }
    }

    private fun hideWarningDialog() = intent {
        reduce { state.copy(isShowWarningDialog = false) }
    }
}
