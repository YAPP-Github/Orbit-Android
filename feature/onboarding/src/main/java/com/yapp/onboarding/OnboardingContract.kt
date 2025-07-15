package com.yapp.onboarding

import com.yapp.ui.base.UiState
import java.time.LocalTime

sealed class OnboardingContract {

    data class State(
        val currentStep: Int = 1,
        val selectedTime: LocalTime = LocalTime.of(1, 0),
        val textFieldValue: String = "",
        val showWarning: Boolean = false,
        val isButtonEnabled: Boolean = false,
        val selectedGender: String? = null,
        val userName: String = "",
        val birthDate: String = "",
        val birthType: String = "양력",
        val birthTime: String = "",
        val isBirthDateValid: Boolean = false,
        val isBirthTimeValid: Boolean = false,
        val isValid: Boolean = false,
        val isBottomSheetOpen: Boolean = false,
        val isShowWarningDialog: Boolean = false,
    ) : UiState {
        val birthDateFormatted: String
            get() {
                val parts = birthDate.split("-")
                val year = parts[0] + "년"
                val month = parts[1].toInt().toString() + "월"
                val day = parts[2].toInt().toString() + "일"

                return "$birthType $year $month $day"
            }

        val birthTimeFormatted: String
            get() {
                if (!isBirthTimeValid || birthTime.isBlank()) return "몰라요"

                val parts = birthTime.split(":")
                val hour = parts[0].toInt().toString() + "시"
                val minute = parts[1].toInt().toString() + "분"

                return "$hour $minute"
            }
    }

    sealed class Action {
        data object NextStep : Action()
        data object PreviousStep : Action()
        data class SetAlarmTime(val newTime: LocalTime) : Action()
        data object CreateAlarm : Action()
        data class UpdateField(val value: String, val fieldType: FieldType) : Action()
        data object Reset : Action()
        data object Submit : Action()
        data class UpdateGender(val gender: String) : Action()
        data class UpdateBirthDate(val lunar: String, val year: Int, val month: Int, val day: Int) : Action()
        data object ToggleBottomSheet : Action()
        data object CompleteOnboarding : Action()
        data class OpenWebView(val url: String) : Action()
        data object ShowWarningDialog : Action()
        data object HideWarningDialog : Action()
    }

    enum class FieldType(val validationRegex: Regex) {
        TIME(Regex("^(24:00|([0-1]\\d|2[0-3]):[0-5]\\d)\$")),
        NAME(Regex("^(?=.{1,13}\$)(?=.{1,6}(?:[가-힣]|[a-zA-Z]{2})\$)[가-힣a-zA-Z]*\$")),
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data class NavigateToNextStep(val currentStep: Int) : SideEffect()

        data object NavigateBack : SideEffect()

        data object OnboardingCompleted : SideEffect()

        data class OpenWebView(val url: String) : SideEffect()
    }

    companion object {
        fun truncateTextToLimit(text: String, maxLength: Int = 12): String {
            var totalLength = 0
            val result = StringBuilder()

            for (char in text) {
                val charWeight = if (char in '가'..'힣') 2 else 1

                if (totalLength + charWeight > maxLength) break

                totalLength += charWeight
                result.append(char)
            }

            return result.toString()
        }
    }
}
