package com.yapp.splash

import androidx.lifecycle.ViewModel
import com.yapp.domain.repository.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
) : ViewModel(), ContainerHost<SplashContract.State, SplashContract.SideEffect> {

    override val container: Container<SplashContract.State, SplashContract.SideEffect> = container(
        initialState = SplashContract.State(),
    ) {
        startSplashAnimation()
    }

    private fun startSplashAnimation() = intent {
        reduce { state.copy(isVisible = true) }
        delay(1500)
        reduce { state.copy(isVisible = false) }
        delay(1000)

        checkUserState()
    }

    private fun checkUserState() = intent {
        combine(
            userInfoRepository.userIdFlow,
            userInfoRepository.onboardingCompletedFlow,
        ) { userId, onboardingCompleted ->
            Pair(userId, onboardingCompleted)
        }.first { (userId, onboardingCompleted) ->
            if (userId != null && onboardingCompleted) {
                postSideEffect(SplashContract.SideEffect.NavigateToHome)
            } else {
                postSideEffect(SplashContract.SideEffect.NavigateToOnboarding)
            }
            true
        }
    }
}
