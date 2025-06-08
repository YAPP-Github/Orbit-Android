package com.yapp.setting

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.extensions.sharedHiltViewModel
import com.yapp.common.navigation.route.SettingBaseRoute
import com.yapp.common.navigation.route.SettingDestination
import kotlinx.coroutines.flow.collectLatest

fun NavGraphBuilder.settingNavGraph(
    navigator: OrbitNavigator,
) {
    navigation<SettingBaseRoute>(
        startDestination = SettingDestination.Setting,
    ) {
        composable<SettingDestination.Setting> {
            val viewModel = it.sharedHiltViewModel<SettingViewModel>(navigator.navController)

            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator)
                }
            }

            SettingRoute(viewModel)
        }

        composable<SettingDestination.EditProfile> {
            val viewModel = it.sharedHiltViewModel<EditProfileViewModel>(navigator.navController)

            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator)
                }
            }

            EditProfileRoute(viewModel)
        }

        composable<SettingDestination.EditBirthday>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = FastOutSlowInEasing,
                    ),
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing,
                    ),
                )
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing,
                    ),
                )
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing,
                    ),
                )
            },
        ) {
            val viewModel = it.sharedHiltViewModel<EditProfileViewModel>(navigator.navController)

            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collectLatest { sideEffect ->
                    handleSideEffect(sideEffect, navigator)
                }
            }

            EditBirthdayRoute(viewModel)
        }
    }
}

private fun handleSideEffect(
    sideEffect: SettingContract.SideEffect,
    navigator: OrbitNavigator,
) {
    when (sideEffect) {
        SettingContract.SideEffect.NavigateBack -> navigator.navigateBack()

        SettingContract.SideEffect.NavigateToSettingRoute -> {
            navigator.navigateToSetting(
                navOptions = navOptions {
                    popUpTo(SettingBaseRoute) {
                        inclusive = true
                    }
                },
            )
        }

        SettingContract.SideEffect.NavigateToEditProfile -> {
            navigator.navigateToEditProfile()
        }

        SettingContract.SideEffect.NavigateToEditBirthday -> {
            navigator.navigateToEditBirthDay()
        }

        is SettingContract.SideEffect.OpenWebView -> {
            navigator.navigateToWebView(Uri.encode(sideEffect.url))
        }
    }
}
