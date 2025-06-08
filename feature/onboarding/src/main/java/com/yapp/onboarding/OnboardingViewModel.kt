package com.yapp.onboarding

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.common.navigation.route.OnboardingDestination
import com.yapp.datastore.UserPreferences
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toRepeatDays
import com.yapp.domain.repository.SignUpRepository
import com.yapp.domain.usecase.AlarmUseCase
import com.yapp.media.haptic.HapticFeedbackManager
import com.yapp.media.haptic.HapticType
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    private val signUpRepository: SignUpRepository,
    private val userPreferences: UserPreferences,
    private val alarmUseCase: AlarmUseCase,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<OnboardingContract.State, OnboardingContract.SideEffect>(
    initialState = OnboardingContract.State(
        currentStep = savedStateHandle["currentStep"] ?: 1,
        birthDate = savedStateHandle["birthDate"] ?: "",
        birthType = savedStateHandle["birthType"] ?: "양력",
    ),
) {
    private val currentRoute: KClass<out OnboardingDestination>?
        get() = OnboardingDestination.routes.getOrNull(currentState.currentStep)

    fun processAction(action: OnboardingContract.Action) {
        when (action) {
            is OnboardingContract.Action.NextStep -> moveToNextStep()
            is OnboardingContract.Action.PreviousStep -> moveToPreviousStep()
            is OnboardingContract.Action.SetAlarmTime -> setAlarmTime(action.isAm, action.hour, action.minute)
            is OnboardingContract.Action.CreateAlarm -> createAlarm()
            is OnboardingContract.Action.UpdateField -> updateField(action.value, action.fieldType)
            is OnboardingContract.Action.UpdateBirthDate -> updateBirthDate(action.lunar, action.year, action.month, action.day)
            is OnboardingContract.Action.Reset -> resetFields()
            is OnboardingContract.Action.Submit -> submitUserInfo()
            is OnboardingContract.Action.UpdateGender -> updateGender(action.gender)
            is OnboardingContract.Action.ToggleBottomSheet -> toggleBottomSheet()
            is OnboardingContract.Action.CompleteOnboarding -> completeOnboarding()
            is OnboardingContract.Action.OpenWebView -> openWebView(action.url)
            is OnboardingContract.Action.ShowWarningDialog -> showWarningDialog()
            is OnboardingContract.Action.HideWarningDialog -> hideWarningDialog()
        }
    }

    private fun submitUserInfo() {
        viewModelScope.launch {
            val state = container.stateFlow.value

            val result = signUpRepository.postSignUp(
                name = state.userName,
                calendarType = state.birthType,
                birthDate = state.birthDate,
                birthTime = state.birthTime,
                gender = state.selectedGender ?: "",
            )

            if (result.isSuccess) {
                val userId = result.getOrNull() ?: return@launch
                val userName = state.userName
                userPreferences.saveUserId(userId)
                userPreferences.saveUserName(userName)

                analyticsHelper.setUserId(userId)
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "onboarding_complete",
                        properties = mapOf(
                            AnalyticsEvent.OnboardingPropertiesKeys.STEP to "환영2",
                        ),
                    ),
                )

                updateState { copy(isBottomSheetOpen = false) }
                moveToNextStep()
            } else {
                processAction(OnboardingContract.Action.ShowWarningDialog)
            }
        }
    }

    private fun moveToNextStep() {
        val currentStep = container.stateFlow.value.currentStep
        val nextStep = currentStep + 1
        val nextRoute = OnboardingDestination.getNextRouteForStep(currentStep)

        savedStateHandle["birthDate"] = currentState.birthDate
        savedStateHandle["birthType"] = currentState.birthType

        if (nextRoute != null) {
            savedStateHandle["currentStep"] = nextStep
            updateState { copy(currentStep = nextStep) }
            emitSideEffect(OnboardingContract.SideEffect.NavigateToNextStep(currentStep))
        } else {
            emitSideEffect(OnboardingContract.SideEffect.OnboardingCompleted)
        }
    }

    private fun moveToPreviousStep() {
        val currentStep = container.stateFlow.value.currentStep
        if (currentStep > 1) {
            val previousStep = currentStep - 1
            savedStateHandle["currentStep"] = previousStep
            updateState { copy(currentStep = previousStep) }
            emitSideEffect(OnboardingContract.SideEffect.NavigateBack)
        }
    }

    private fun setAlarmTime(amPm: String, hour: Int, minute: Int) {
        hapticFeedbackManager.performHapticFeedback(HapticType.LIGHT_TICK)

        val newTimeState = currentState.timeState.copy(
            selectedAmPm = amPm,
            selectedHour = hour,
            selectedMinute = minute,
        )
        updateState {
            copy(
                timeState = newTimeState,
            )
        }
    }

    private fun createAlarm() {
        viewModelScope.launch {
            alarmUseCase.getAlarmSounds().onSuccess { sounds ->
                val defaultSoundIndex = sounds.indexOfFirst { it.title == "Homecoming" }.takeIf { it >= 0 } ?: 0
                val defaultSoundUri = sounds[defaultSoundIndex]

                val newAlarm = Alarm(
                    isAm = currentState.timeState.selectedAmPm == "오전",
                    hour = currentState.timeState.selectedHour,
                    minute = currentState.timeState.selectedMinute,
                    repeatDays = setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI).toRepeatDays(),
                    isSnoozeEnabled = true,
                    snoozeInterval = 5,
                    snoozeCount = 5,
                    soundUri = "${defaultSoundUri.uri}",
                )

                alarmUseCase.insertAlarm(
                    alarm = newAlarm,
                ).onSuccess {
                    emitSideEffect(OnboardingContract.SideEffect.OnboardingCompleted)
                }.onFailure {
                    Log.e("OnboardingViewModel", "Failed to create alarm", it)
                }
            }.onFailure {
                Log.e("OnboardingViewModel", "Failed to get alarm sounds", it)
            }
        }
    }

    private fun updateField(value: String, fieldType: OnboardingContract.FieldType) {
        when (fieldType) {
            OnboardingContract.FieldType.TIME -> {
                val isComplete = value.length == 5
                val isValid = isComplete && value.matches(fieldType.validationRegex)

                updateState {
                    copy(
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

                updateState {
                    copy(
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

    private fun updateBirthDate(lunar: String, year: Int, month: Int, day: Int) {
        if (currentRoute != OnboardingDestination.Birthday::class) return

        val formattedDate = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

        hapticFeedbackManager.performHapticFeedback(HapticType.LIGHT_TICK)
        savedStateHandle["birthDate"] = formattedDate
        savedStateHandle["birthType"] = lunar

        updateState {
            copy(
                birthDate = formattedDate,
                birthType = lunar,
                isBirthDateValid = true,
            )
        }
    }

    private fun resetFields() {
        updateState {
            copy(
                textFieldValue = "",
                showWarning = false,
                isButtonEnabled = false,
                isValid = false,
            )
        }
    }

    private fun updateGender(gender: String) {
        updateState { copy(selectedGender = gender, isButtonEnabled = true) }
    }

    private fun toggleBottomSheet() {
        val isCurrentlyOpen = container.stateFlow.value.isBottomSheetOpen
        updateState { copy(isBottomSheetOpen = !isCurrentlyOpen) }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted()
            emitSideEffect(OnboardingContract.SideEffect.OnboardingCompleted)
        }
    }

    private fun openWebView(url: String) {
        emitSideEffect(OnboardingContract.SideEffect.OpenWebView(url))
    }

    private fun showWarningDialog() {
        updateState { copy(isShowWarningDialog = true) }
    }

    private fun hideWarningDialog() {
        updateState { copy(isShowWarningDialog = false) }
    }
}
