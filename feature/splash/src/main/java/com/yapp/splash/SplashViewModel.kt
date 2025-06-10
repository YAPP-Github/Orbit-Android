package com.yapp.splash

import androidx.lifecycle.viewModelScope
import com.yapp.domain.repository.UserDataRepository
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : BaseViewModel<SplashContract.State, SplashContract.SideEffect>(
    initialState = SplashContract.State(),
) {
    init {
        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        viewModelScope.launch {
            updateState { copy(isVisible = true) }
            delay(1500)
            updateState { copy(isVisible = false) }
            delay(1000)

            checkUserState()
        }
    }

    private fun checkUserState() {
        viewModelScope.launch {
            combine(
                userDataRepository.userIdFlow,
                userDataRepository.onboardingCompletedFlow,
            ) { userId, onboardingCompleted ->
                Pair(userId, onboardingCompleted)
            }.collect { (userId, onboardingCompleted) ->
                if (userId != null && onboardingCompleted) {
                    emitSideEffect(SplashContract.SideEffect.NavigateToHome)
                } else {
                    emitSideEffect(SplashContract.SideEffect.NavigateToOnboarding)
                }
            }
        }
    }
}
