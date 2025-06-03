package com.yapp.webview

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.route.WebViewRoute

fun NavGraphBuilder.webViewScreen(
    navigator: OrbitNavigator,
) {
    composable<WebViewRoute> { entry ->
        val route = entry.toRoute<WebViewRoute>()
        WebViewRoute(
            url = route.url,
            navController = navigator.navController,
        )
    }
}
