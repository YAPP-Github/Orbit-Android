package com.yapp.orbit

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import com.yapp.ui.component.bottomsheet.OrbitBottomSheetLayout
import com.yapp.ui.component.bottomsheet.OrbitBottomSheetState
import com.yapp.ui.component.bottomsheet.rememberOrbitBottomSheetState
import com.yapp.ui.component.snackbar.CustomSnackBarVisuals
import com.yapp.ui.component.snackbar.OrbitSnackBar
import com.yapp.webview.webViewScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun OrbitNavHost(
    modifier: Modifier = Modifier,
    navigator: OrbitNavigator = rememberOrbitNavigator(),
    bottomSheetState: OrbitBottomSheetState = rememberOrbitBottomSheetState(),
) {
    val snackBarHostState = remember { SnackbarHostState() }

    Box {
        OrbitBottomSheetLayout(sheetState = bottomSheetState) {
            Scaffold(
                modifier = modifier,
                snackbarHost = { OrbitSnackBarHost(snackBarHostState) },
                containerColor = OrbitTheme.colors.gray_900,
            ) {
                OrbitNavigationGraph(
                    navigator = navigator,
                    bottomSheetState = bottomSheetState,
                    snackBarHostState = snackBarHostState,
                )
            }
        }

        NavigationBarScrim()
    }
}

@Composable
private fun OrbitNavigationGraph(
    navigator: OrbitNavigator,
    bottomSheetState: OrbitBottomSheetState,
    snackBarHostState: SnackbarHostState,
) {
    NavHost(
        navController = navigator.navController,
        startDestination = navigator.startDestination,
    ) {
        splashScreen(navigator)
        onboardingNavGraph(navigator, bottomSheetState)
        homeNavGraph(navigator, bottomSheetState, snackBarHostState)
        missionScreen(navigator)
        fortuneNavGraph(navigator, snackBarHostState)
        settingNavGraph(navigator)
        webViewScreen(navigator)
    }
}

@Composable
private fun BoxScope.NavigationBarScrim() {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.navigationBars)
            .background(Color.Black)
            .zIndex(1f),
    )
}

@Composable
private fun OrbitSnackBarHost(
    snackBarHostState: SnackbarHostState,
) {
    AnimatedVisibility(
        visible = snackBarHostState.currentSnackbarData != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
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
                    label = visuals?.actionLabel.orEmpty(),
                    iconRes = visuals?.iconRes,
                    message = visuals?.message.orEmpty(),
                    onAction = { snackBarHostState.currentSnackbarData?.performAction() },
                )
            },
        )
    }
}
