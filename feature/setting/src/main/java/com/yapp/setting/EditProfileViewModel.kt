package com.yapp.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import com.yapp.domain.model.EditUser
import com.yapp.domain.repository.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
) : ViewModel(), ContainerHost<SettingContract.State, SettingContract.SideEffect> {

    override val container: Container<SettingContract.State, SettingContract.SideEffect> = container(
        initialState = SettingContract.State(),
    )

    fun processAction(action: SettingContract.Action) {
        when (action) {
            is SettingContract.Action.UpdateName -> updateName(action.name)
            is SettingContract.Action.UpdateBirthDate -> updateBirthDate(action)
            is SettingContract.Action.UpdateCalendarType -> updateCalendarType(action.calendarType)
            is SettingContract.Action.UpdateGender -> updateGender(action.gender)
            is SettingContract.Action.ToggleGender -> toggleGender(action.isMale)
            is SettingContract.Action.ToggleTimeUnknown -> toggleTimeUnknown(action.isChecked)
            is SettingContract.Action.UpdateTimeOfBirth -> updateTimeOfBirth(action.time)
            is SettingContract.Action.ConfirmAndNavigateBack -> navigateBack()
            is SettingContract.Action.Reset -> resetState()
            SettingContract.Action.ShowDialog -> showDialog()
            SettingContract.Action.HideDialog -> hideDialog()
            SettingContract.Action.PreviousStep -> previousStep()
            SettingContract.Action.SubmitUserInfo -> submitUserInfo()
            is SettingContract.Action.NavigateToEditBirthday -> navigateToEditBirthday()
            is SettingContract.Action.RefreshUserInfo -> refreshUserInfo()
            else -> {}
        }
    }

    private fun updateName(name: String) = intent {
        reduce {
            state.copy(name = name, isNameValid = validateName(name))
        }
    }

    private fun validateName(name: String): Boolean {
        return SettingContract.FieldType.NAME.validationRegex.matches(name)
    }

    private fun updateBirthDate(action: SettingContract.Action.UpdateBirthDate) = intent {
        reduce {
            val formattedDate = "${action.year}-${action.month.toString().padStart(2, '0')}-${
            action.day.toString().padStart(2, '0')
            }"
            state.copy(birthDate = formattedDate)
        }
    }

    private fun updateCalendarType(calendarType: String) = intent {
        reduce {
            state.copy(birthType = calendarType)
        }
    }

    private fun updateGender(gender: String) = intent {
        reduce {
            state.copy(selectedGender = gender)
        }
    }

    private fun toggleGender(isMale: Boolean) = intent {
        reduce {
            state.copy(
                isMaleSelected = isMale,
                isFemaleSelected = !isMale,
                selectedGender = if (isMale) "남성" else "여성",
            )
        }
    }

    private fun toggleTimeUnknown(isChecked: Boolean) = intent {
        reduce {
            val newState = state.copy(
                isTimeUnknown = isChecked,
                timeOfBirth = if (isChecked) "시간모름" else "",
            )
            newState.copy(isTimeValid = validateTimeOfBirth(newState.timeOfBirth, isChecked))
        }
    }

    private fun updateTimeOfBirth(time: String) = intent {
        reduce {
            state.copy(timeOfBirth = time, isTimeValid = validateTimeOfBirth(time, state.isTimeUnknown))
        }
    }

    private fun validateTimeOfBirth(time: String, isTimeUnknown: Boolean): Boolean {
        return if (isTimeUnknown) {
            true
        } else {
            time.length == 5 && SettingContract.FieldType.TIME.validationRegex.matches(time)
        }
    }

    private fun fetchUserInfo(userId: Long) = intent {
        userInfoRepository.getUserInfo(userId)
            .onSuccess { user ->
                val (initialYear, initialMonth, initialDay) = user.birthDate.split("-")

                reduce {
                    state.copy(
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

    private fun previousStep() = intent {
        reduce { state.copy(shouldFetchUserInfo = true) }
        postSideEffect(SettingContract.SideEffect.NavigateBack)
    }

    private fun submitUserInfo() = intent {
        val userId = userInfoRepository.userIdFlow.firstOrNull() ?: return@intent

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
            postSideEffect(SettingContract.SideEffect.NavigateToSettingRoute)
        } else {
            Log.e("EditProfileViewModel", "사용자 정보 수정 실패")
        }
    }

    private fun extractBirthDate(formattedDate: String): String {
        return formattedDate.replace(Regex("[^0-9-]"), "")
    }

    private fun navigateBack() = intent {
        postSideEffect(SettingContract.SideEffect.NavigateBack)
    }

    private fun resetState() = intent {
        reduce { SettingContract.State() }
    }

    private fun showDialog() = intent {
        reduce { state.copy(isDialogVisible = true) }
    }

    private fun hideDialog() = intent {
        reduce { state.copy(isDialogVisible = false) }
    }

    private fun refreshUserInfo() = intent {
        val userId = userInfoRepository.userIdFlow.firstOrNull()
        if (userId != null) {
            fetchUserInfo(userId)
        }
    }

    private fun navigateToEditBirthday() = intent {
        reduce { state.copy(shouldFetchUserInfo = false) }
        postSideEffect(SettingContract.SideEffect.NavigateToEditBirthday)
    }
}
