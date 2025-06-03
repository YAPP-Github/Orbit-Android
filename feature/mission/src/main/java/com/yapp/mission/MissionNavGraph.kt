package com.yapp.mission

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.extensions.sharedHiltViewModel
import com.yapp.common.navigation.route.MissionRoute

fun NavGraphBuilder.missionScreen(
    navigator: OrbitNavigator,
) {
    composable<MissionRoute>(
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "orbitapp://mission?notificationId={notificationId}"
            },
        ),
    ) {
        val viewModel = it.sharedHiltViewModel<MissionViewModel>(navigator.navController)

        LaunchedEffect(viewModel) {
            viewModel.container.sideEffectFlow.collect { sideEffect ->
                when (sideEffect) {
                    MissionContract.SideEffect.NavigateToFortune -> {
                        navigator.navigateToFortune(
                            navOptions = navOptions {
                                popUpTo(MissionRoute) {
                                    inclusive = true
                                }
                            },
                        )
                    }

                    MissionContract.SideEffect.NavigateBack -> navigator.navigateBack()
                }
            }
        }

        MissionRoute(viewModel)
    }
}
