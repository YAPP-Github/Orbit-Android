package com.yapp.navigator

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.rememberOrbitNavigator
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.fortune.fortuneNavGraph
import com.yapp.home.homeNavGraph
import com.yapp.mission.missionScreen
import com.yapp.onboarding.onboardingNavGraph
import com.yapp.setting.settingNavGraph
import com.yapp.splash.splashScreen
import com.yapp.ui.component.snackbar.CustomSnackBarVisuals
import com.yapp.ui.component.snackbar.OrbitSnackBar
import com.yapp.webview.webViewScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun OrbitNavHost(
    modifier: Modifier = Modifier,
    navigator: OrbitNavigator = rememberOrbitNavigator(),
) {
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            OrbitSnackBarHost(snackBarHostState = snackBarHostState)
        },
        containerColor = OrbitTheme.colors.gray_900,
    ) {
        NavHost(
            navController = navigator.navController,
            startDestination = navigator.startDestination,
            modifier = Modifier.navigationBarsPadding(),
        ) {
            splashScreen(navigator = navigator)
            onboardingNavGraph(navigator = navigator)
            homeNavGraph(
                navigator = navigator,
                snackBarHostState = snackBarHostState,
            )
            missionScreen(navigator = navigator)
            fortuneNavGraph(
                navigator = navigator,
                snackBarHostState = snackBarHostState,
            )
            settingNavGraph(navigator = navigator)
            webViewScreen(navigator = navigator)
        }
    }
}

@Composable
private fun OrbitSnackBarHost(
    snackBarHostState: SnackbarHostState,
) {
    AnimatedVisibility(
        visible = snackBarHostState.currentSnackbarData != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
        ) + fadeOut(),
    ) {
        SnackbarHost(
            hostState = snackBarHostState,
            snackbar = { data ->
                val visuals = data.visuals as? CustomSnackBarVisuals

                OrbitSnackBar(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = visuals?.bottomPadding ?: 12.dp,
                    ),
                    label = visuals?.actionLabel ?: "",
                    iconRes = visuals?.iconRes,
                    message = visuals?.message ?: "",
                    onAction = { snackBarHostState.currentSnackbarData?.performAction() },
                )
            },
        )
    }
}
