package com.yapp.onboarding

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.extensions.sharedHiltViewModel
import com.yapp.common.navigation.route.OnboardingBaseRoute
import com.yapp.common.navigation.route.OnboardingDestination
import com.yapp.ui.component.bottomsheet.OrbitBottomSheetState
import org.orbitmvi.orbit.compose.collectSideEffect

fun NavGraphBuilder.onboardingNavGraph(
    navigator: OrbitNavigator,
    bottomSheetState: OrbitBottomSheetState,
) {
    navigation<OnboardingBaseRoute>(startDestination = OnboardingDestination.Explain) {
        composable<OnboardingDestination.Explain> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingExplainRoute(viewModel)
        }

        composable<OnboardingDestination.AlarmTimeSelection> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingAlarmTimeSelectionRoute(viewModel)
        }

        composable<OnboardingDestination.Birthday> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingBirthdayRoute(viewModel)
        }

        composable<OnboardingDestination.TimeOfBirth> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingTimeOfBirthRoute(viewModel)
        }

        composable<OnboardingDestination.Name> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingNameRoute(viewModel)
        }

        composable<OnboardingDestination.Gender> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            OnboardingGenderRoute(navigator, bottomSheetState, viewModel)
        }

        composable<OnboardingDestination.Access> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingAccessRoute(viewModel)
        }

        composable<OnboardingDestination.Complete1> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingCompleteRoute(viewModel)
        }

        composable<OnboardingDestination.Complete2> {
            val viewModel = it.sharedHiltViewModel<OnboardingViewModel>(navigator.navController)

            viewModel.collectSideEffect { sideEffect ->
                handleOnboardingCommonSideEffect(sideEffect, navigator, viewModel::processAction)
            }

            OnboardingCompleteRoute2(viewModel)
        }
    }
}

private fun handleOnboardingCommonSideEffect(
    sideEffect: OnboardingContract.SideEffect,
    navigator: OrbitNavigator,
    processAction: (OnboardingContract.Action) -> Unit,
) {
    when (sideEffect) {
        is OnboardingContract.SideEffect.NavigateToNextStep -> {
            navigator.navigateToOnboardingNextStep(sideEffect.currentStep)
        }

        OnboardingContract.SideEffect.NavigateBack -> {
            processAction(OnboardingContract.Action.Reset)
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

        else -> { }
    }
}
