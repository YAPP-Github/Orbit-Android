package com.yapp.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.route.SplashRoute

fun NavGraphBuilder.splashScreen(
    navigator: OrbitNavigator,
) {
    composable<SplashRoute> {
        SplashRoute(navigator)
    }
}
