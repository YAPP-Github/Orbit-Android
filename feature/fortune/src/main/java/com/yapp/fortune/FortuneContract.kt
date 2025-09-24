package com.yapp.fortune

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yapp.fortune.page.FortunePageData

sealed class FortuneContract {
    data class State(
        val isLoading: Boolean = true,
        val currentStep: Int = 0,
        val hasReward: Boolean = true,
        val dailyFortuneTitle: String = "",
        val dailyFortuneDescription: String = "",
        val avgFortuneScore: Int = 0,
        val fortunePages: List<FortunePageData> = emptyList(),
        val fortuneImageId: Int? = null,
        val isCreateFailureDialogVisible: Boolean = false,
    ) : com.yapp.ui.base.UiState

    sealed class Action {
        data object NextStep : Action()
        data class UpdateStep(val step: Int) : Action()
        data object NavigateToHome : Action()
        data class SaveImage(@DrawableRes val resId: Int) : Action()
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data object NavigateToFortuneReward : SideEffect()

        data object NavigateToHome : SideEffect()

        data object NavigateBack : SideEffect()

        data class ShowSnackBar(
            val message: String,
            val label: String = "",
            val iconRes: Int,
            val bottomPadding: Dp = 12.dp,
            val durationMillis: Long = 2000,
            val onDismiss: () -> Unit,
            val onAction: () -> Unit,
        ) : SideEffect()
    }
}
