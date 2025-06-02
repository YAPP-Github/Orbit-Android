package com.yapp.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.yapp.common.navigation.destination.SplashDestination

class OrbitNavigator(
    val navController: NavHostController,
) {
    val startDestination = SplashDestination.Route.route

    fun navigateTo(route: String, popUpTo: String? = null, inclusive: Boolean = false) {
        navController.navigate(route) {
            popUpTo?.let {
                popUpTo(it) { this.inclusive = inclusive }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}

@Composable
fun rememberOrbitNavigator(
    navController: NavHostController = rememberNavController(),
): OrbitNavigator = remember(navController) {
    OrbitNavigator(navController)
}
