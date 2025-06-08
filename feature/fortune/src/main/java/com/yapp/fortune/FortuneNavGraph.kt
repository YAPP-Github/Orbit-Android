package com.yapp.fortune

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.extensions.sharedHiltViewModel
import com.yapp.common.navigation.route.FortuneBaseRoute
import com.yapp.common.navigation.route.FortuneDestination
import com.yapp.ui.component.snackbar.showCustomSnackBar
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.fortuneNavGraph(
    navigator: OrbitNavigator,
    snackBarHostState: SnackbarHostState,
) {
    navigation<FortuneBaseRoute>(startDestination = FortuneDestination.Fortune) {
        composable<FortuneDestination.Fortune> { backStackEntry ->
            val viewModel = backStackEntry.sharedHiltViewModel<FortuneViewModel>(navigator.navController)
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collect { sideEffect ->
                    handleSideEffect(sideEffect, navigator, snackBarHostState, coroutineScope)
                }
            }

            FortuneRoute(
                viewModel = viewModel,
                navigator = navigator,
            )
        }

        composable<FortuneDestination.Reward> { backStackEntry ->
            val viewModel = backStackEntry.sharedHiltViewModel<FortuneViewModel>(navigator.navController)
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collect { sideEffect ->
                    handleSideEffect(sideEffect, navigator, snackBarHostState, coroutineScope)
                }
            }

            FortuneRewardRoute(
                viewModel = viewModel,
            )
        }
    }
}

private suspend fun handleSideEffect(
    sideEffect: FortuneContract.SideEffect,
    navigator: OrbitNavigator,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    when (sideEffect) {
        FortuneContract.SideEffect.NavigateToFortuneReward -> {
            navigator.navigateToFortuneReward(
                navOptions = navOptions {
                    popUpTo(FortuneDestination.Fortune) {
                        inclusive = true
                    }
                },
            )
        }

        FortuneContract.SideEffect.NavigateToHome -> navigator.navigateToHome(
            navOptions = navOptions {
                popUpTo(FortuneBaseRoute) {
                    inclusive = true
                }
            },
        )

        FortuneContract.SideEffect.NavigateBack -> navigator.navigateBack()

        is FortuneContract.SideEffect.ShowSnackBar -> showCustomSnackBar(
            scope = coroutineScope,
            snackBarHostState = snackBarHostState,
            message = sideEffect.message,
            actionLabel = sideEffect.label,
            iconRes = sideEffect.iconRes,
            bottomPadding = sideEffect.bottomPadding,
            durationMillis = sideEffect.durationMillis,
            onDismiss = sideEffect.onDismiss,
            onAction = sideEffect.onAction,
        )
    }
}
