package com.yapp.mission

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.destination.MissionDestination
import com.yapp.common.navigation.extensions.sharedHiltViewModel

fun NavGraphBuilder.missionNavGraph(
    navigator: OrbitNavigator,
) {
    navigation(
        route = MissionDestination.Route.route,
        startDestination = MissionDestination.Mission.route,
    ) {
        composable(
            route = MissionDestination.Mission.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "orbitapp://mission?notificationId={notificationId}"
                },
            ),
        ) { backStackEntry ->
            val viewModel = backStackEntry.sharedHiltViewModel<MissionViewModel>(navigator.navController)

            LaunchedEffect(viewModel) {
                viewModel.container.sideEffectFlow.collect { sideEffect ->
                    handleMissionSideEffect(sideEffect, navigator, viewModel)
                }
            }

            MissionRoute(viewModel)
        }
    }
}

private fun handleMissionSideEffect(
    sideEffect: MissionContract.SideEffect,
    navigator: OrbitNavigator,
    viewModel: MissionViewModel,
) {
    when (sideEffect) {
        is MissionContract.SideEffect.Navigate -> navigator.navigateTo(
            route = sideEffect.route,
            popUpTo = sideEffect.popUpTo,
            inclusive = sideEffect.inclusive,
        )

        MissionContract.SideEffect.NavigateBack -> navigator.navigateBack()
    }
}
