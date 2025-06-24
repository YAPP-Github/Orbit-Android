package com.yapp.setting

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.yapp.domain.model.EditUser
import com.yapp.domain.repository.UserInfoRepository
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.simple.intent
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
) : BaseViewModel<SettingContract.State, SettingContract.SideEffect>(
    SettingContract.State(),
) {
    fun onAction(action: SettingContract.Action) = intent {
        when (action) {
            is SettingContract.Action.UpdateName -> updateName(action.name)
            is SettingContract.Action.UpdateBirthDate -> updateBirthDate(action)
            is SettingContract.Action.UpdateCalendarType -> updateCalendarType(action.calendarType)
            is SettingContract.Action.UpdateGender -> updateGender(action.gender)
            is SettingContract.Action.ToggleGender -> toggleGender(action.isMale)
            is SettingContract.Action.ToggleTimeUnknown -> toggleTimeUnknown(action.isChecked)
            is SettingContract.Action.UpdateTimeOfBirth -> updateTimeOfBirth(action.time)
            is SettingContract.Action.ConfirmAndNavigateBack -> emitSideEffect(SettingContract.SideEffect.NavigateBack)
            is SettingContract.Action.Reset -> updateState { SettingContract.State() }
            SettingContract.Action.ShowDialog -> updateState { copy(isDialogVisible = true) }
            SettingContract.Action.HideDialog -> updateState { copy(isDialogVisible = false) }
            SettingContract.Action.PreviousStep -> previousStep()
            SettingContract.Action.SubmitUserInfo -> submitUserInfo()
            is SettingContract.Action.NavigateToEditBirthday -> navigateToEditBirthday()
            is SettingContract.Action.RefreshUserInfo -> {
                if (currentState.shouldFetchUserInfo) {
                    refreshUserInfo()
                }
            }
            else -> {}
        }
    }

    private fun updateName(name: String) = updateState {
        copy(name = name, isNameValid = validateName(name))
    }

    private fun validateName(name: String): Boolean {
        return SettingContract.FieldType.NAME.validationRegex.matches(name)
    }

    private fun updateBirthDate(action: SettingContract.Action.UpdateBirthDate) = updateState {
        val formattedDate = "${action.year}-${action.month.toString().padStart(2, '0')}-${
        action.day.toString().padStart(2, '0')
        }"
        copy(birthDate = formattedDate)
    }

    private fun updateCalendarType(calendarType: String) = updateState {
        copy(birthType = calendarType)
    }

    private fun updateGender(gender: String) = updateState {
        copy(selectedGender = gender)
    }

    private fun toggleGender(isMale: Boolean) = updateState {
        copy(
            isMaleSelected = isMale,
            isFemaleSelected = !isMale,
            selectedGender = if (isMale) "남성" else "여성",
        )
    }

    private fun toggleTimeUnknown(isChecked: Boolean) = updateState {
        val newState = copy(
            isTimeUnknown = isChecked,
            timeOfBirth = if (isChecked) "시간모름" else "",
        )
        newState.copy(isTimeValid = validateTimeOfBirth(newState.timeOfBirth, isChecked))
    }

    private fun updateTimeOfBirth(time: String) = updateState {
        copy(timeOfBirth = time, isTimeValid = validateTimeOfBirth(time, isTimeUnknown))
    }

    private fun validateTimeOfBirth(time: String, isTimeUnknown: Boolean): Boolean {
        return if (isTimeUnknown) {
            true
        } else {
            time.length == 5 && SettingContract.FieldType.TIME.validationRegex.matches(time)
        }
    }

    private fun fetchUserInfo(userId: Long) {
        viewModelScope.launch {
            userInfoRepository.getUserInfo(userId)
                .onSuccess { user ->
                    val (initialYear, initialMonth, initialDay) = user.birthDate.split("-")

                    updateState {
                        copy(
                            name = user.name,
                            isNameValid = validateName(user.name),
                            initialYear = initialYear,
                            initialMonth = initialMonth,
                            initialDay = initialDay,
                            birthType = user.calendarType,
                            birthDate = user.birthDate,
                            selectedGender = user.gender,
                            timeOfBirth = user.birthTime ?: "99:99",
                            isTimeUnknown = user.birthTime == "시간모름",
                            isTimeValid = validateTimeOfBirth(
                                user.birthTime ?: "",
                                user.birthTime == "시간모름",
                            ),
                            isMaleSelected = user.gender == "남성",
                            isFemaleSelected = user.gender == "여성",
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("EditProfileViewModel", "사용자 정보 가져오기 실패: ${error.message}")
                }
        }
    }

    private fun previousStep() {
        updateState { copy(shouldFetchUserInfo = true) }
        emitSideEffect(SettingContract.SideEffect.NavigateBack)
    }

    private fun submitUserInfo() = viewModelScope.launch {
        val userId = userInfoRepository.userIdFlow.firstOrNull() ?: return@launch
        val state = container.stateFlow.value

        val updatedUser = EditUser(
            name = state.name,
            calendarType = state.birthType,
            birthDate = extractBirthDate(state.birthDate),
            birthTime = if (state.isTimeUnknown) null else state.timeOfBirth,
            gender = state.selectedGender ?: "남성",
        )

        val result = userInfoRepository.updateUserInfo(userId, updatedUser)

        if (result.isSuccess) {
            userInfoRepository.saveUserName(state.name)
            emitSideEffect(SettingContract.SideEffect.NavigateToSettingRoute)
        } else {
            Log.e("EditProfileViewModel", "사용자 정보 수정 실패")
        }
    }

    private fun extractBirthDate(formattedDate: String): String {
        return formattedDate.replace(Regex("[^0-9-]"), "")
    }

    private fun navigateToEditBirthday() {
        updateState { copy(shouldFetchUserInfo = false) }
        emitSideEffect(SettingContract.SideEffect.NavigateToEditBirthday)
    }

    private fun refreshUserInfo() {
        viewModelScope.launch {
            val userId = userInfoRepository.userIdFlow.firstOrNull()
            if (userId != null) {
                fetchUserInfo(userId)
            }
        }
    }
}
