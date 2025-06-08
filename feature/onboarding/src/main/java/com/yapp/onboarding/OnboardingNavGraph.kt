package com.yapp.onboarding

import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.extensions.sharedHiltViewModel
import com.yapp.common.navigation.route.OnboardingBaseRoute
import com.yapp.common.navigation.route.OnboardingDestination
import kotlinx.coroutines.flow.collectLatest

fun NavGraphBuilder.onboardingNavGraph(
    navigator: OrbitNavigator,
) {
    navigation<OnboardingBaseRoute>(startDestination = OnboardingDestination.Explain) {
        composable<OnboardingDestination.Explain> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingExplainRoute(viewModel)
        }

        composable<OnboardingDestination.AlarmTimeSelection> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingAlarmTimeSelectionRoute(viewModel)
        }

        composable<OnboardingDestination.Birthday> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingBirthdayRoute(viewModel)
        }

        composable<OnboardingDestination.TimeOfBirth> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingTimeOfBirthRoute(viewModel)
        }

        composable<OnboardingDestination.Name> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingNameRoute(viewModel)
        }

        composable<OnboardingDestination.Gender> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingGenderRoute(viewModel)
        }

        composable<OnboardingDestination.Access> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingAccessRoute(viewModel)
        }

        composable<OnboardingDestination.Complete1> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingCompleteRoute(viewModel)
        }

        composable<OnboardingDestination.Complete2> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)
            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator, viewModel)
                }
            }
            OnboardingCompleteRoute2(viewModel)
        }
    }
}

private fun handleSideEffect(
    sideEffect: OnboardingContract.SideEffect,
    navigator: OrbitNavigator,
    viewModel: OnboardingViewModel,
) {
    when (sideEffect) {
        is OnboardingContract.SideEffect.NavigateToNextStep -> {
            navigator.navigateToOnboardingNextStep(sideEffect.currentStep)
        }

        OnboardingContract.SideEffect.NavigateBack -> {
            viewModel.processAction(OnboardingContract.Action.Reset)
            navigator.navigateBack()
        }

        OnboardingContract.SideEffect.OnboardingCompleted -> {
            navigator.navigateToHome(
                navOptions = navOptions {
                    popUpTo(OnboardingBaseRoute) {
                        inclusive = true
                    }
                },
            )
        }

        is OnboardingContract.SideEffect.OpenWebView -> {
            navigator.navigateToWebView(Uri.encode(sideEffect.url))
        }
    }
}
