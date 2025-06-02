package com.yapp.splash

import androidx.lifecycle.viewModelScope
import com.yapp.common.navigation.destination.HomeDestination
import com.yapp.common.navigation.destination.OnboardingDestination
import com.yapp.datastore.UserPreferences
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
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
                userPreferences.userIdFlow,
                userPreferences.onboardingCompletedFlow,
            ) { userId, onboardingCompleted ->
                Pair(userId, onboardingCompleted)
            }.collect { (userId, onboardingCompleted) ->
                val destination = if (userId != null && onboardingCompleted) {
                    HomeDestination.Home.route
                } else {
                    OnboardingDestination.Route.route
                }
                emitSideEffect(SplashContract.SideEffect.Navigate(destination))
            }
        }
    }
}
