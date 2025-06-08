package com.yapp.splash

import com.yapp.ui.base.UiState

sealed class SplashContract {
    data class State(
        val isLoading: Boolean = true,
        val isVisible: Boolean = false,
    ) : UiState

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data object NavigateToHome : SideEffect()

        data object NavigateToOnboarding : SideEffect()
    }
}
