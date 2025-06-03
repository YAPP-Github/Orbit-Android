package com.yapp.setting

import com.yapp.ui.base.UiState

sealed class SettingContract {
    data class State(
        val initialLoading: Boolean = true,
        val name: String = "",
        val initialYear: String = "2000",
        val initialMonth: String = "01",
        val initialDay: String = "01",
        val birthType: String = "양력",
        val birthDate: String = "2000-01-01",
        val selectedGender: String? = null,
        val isMaleSelected: Boolean = true,
        val isFemaleSelected: Boolean = false,
        val isTimeUnknown: Boolean = false,
        val timeOfBirth: String = "20:00",
        val isDialogVisible: Boolean = false,
        val isNameValid: Boolean = true,
        val isTimeValid: Boolean = true,
        val shouldFetchUserInfo: Boolean = true,
    ) : UiState {
        val birthDateFormatted: String
            get() {
                val parts = birthDate.split("-")
                val year = parts[0] + "년"
                val month = parts[1].toInt().toString() + "월"
                val day = parts[2].toInt().toString() + "일"

                return "$birthType $year $month $day"
            }
        val timeOfBirthFormatted: String
            get() = timeOfBirth.takeIf { it.length >= 5 }?.let {
                "${it.substring(0, 2)}시 ${it.substring(3, 5)}분"
            } ?: " "

        val isActionEnabled: Boolean
            get() = isNameValid && (isTimeUnknown || (timeOfBirth.length == 5 && isTimeValid)) && selectedGender != null
    }

    sealed class Action {
        data object PreviousStep : Action()
        data class UpdateName(val name: String) : Action()
        data class UpdateBirthDate(val birthType: String, val year: Int, val month: Int, val day: Int) : Action()
        data class UpdateGender(val gender: String) : Action()
        data class ToggleGender(val isMale: Boolean) : Action()
        data class ToggleTimeUnknown(val isChecked: Boolean) : Action()
        data class UpdateTimeOfBirth(val time: String) : Action()
        data class UpdateCalendarType(val calendarType: String) : Action()
        data object ConfirmAndNavigateBack : Action()
        data object Reset : Action()
        data object NavigateToEditProfile : Action()
        data object NavigateToEditBirthday : Action()
        data object ShowDialog : Action()
        data object HideDialog : Action()
        data object SubmitUserInfo : Action()
        data class OpenWebView(val url: String) : Action()
        data object RefreshUserInfo : Action()
    }

    enum class FieldType(val validationRegex: Regex) {
        TIME(Regex("^(24:00|([0-1]\\d|2[0-3]):[0-5]\\d)\$")),
        NAME(Regex("^(?=.{1,13}\$)(?=.{1,6}(?:[가-힣]|[a-zA-Z]{2})\$)[가-힣a-zA-Z]*\$")),
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data object NavigateBack : SideEffect()

        data object NavigateToSettingRoute : SideEffect()

        data object NavigateToEditProfile : SideEffect()

        data object NavigateToEditBirthday : SideEffect()

        data class OpenWebView(val url: String) : SideEffect()
    }
}
