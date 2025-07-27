package com.yapp.mission

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.extensions.sharedHiltViewModel
import com.yapp.common.navigation.route.MissionRoute
import org.orbitmvi.orbit.compose.collectSideEffect

fun NavGraphBuilder.missionScreen(
    navigator: OrbitNavigator,
) {
    composable<MissionRoute>(
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "orbitapp://mission?notificationId={notificationId}&missionType={missionType}&missionCount={missionCount}"
            },
        ),
    ) {
        val viewModel = it.sharedHiltViewModel<MissionViewModel>(navigator.navController)

        viewModel.collectSideEffect {
            handleSideEffect(it, navigator)
        }

        MissionRoute(
            navigator = navigator,
            viewModel = viewModel,
        )
    }
}

private fun handleSideEffect(
    sideEffect: MissionContract.SideEffect,
    navigator: OrbitNavigator,
) {
    when (sideEffect) {
        MissionContract.SideEffect.NavigateToFortune -> {
            navigator.navigateToFortune(
                navOptions = navOptions {
                    popUpTo(MissionRoute.route) {
                        inclusive = true
                    }
                },
            )
        }

        MissionContract.SideEffect.NavigateToHome -> {
            navigator.navigateToHome(
                navOptions = navOptions {
                    popUpTo(MissionRoute.route) {
                        inclusive = true
                    }
                },
            )
        }

        MissionContract.SideEffect.NavigateBack -> navigator.navigateBack()
    }
}
