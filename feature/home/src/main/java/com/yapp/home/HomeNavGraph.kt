package com.yapp.home

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.route.HomeBaseRoute
import com.yapp.common.navigation.route.HomeDestination
import com.yapp.home.alarm.addedit.AlarmAddEditRoute

const val ADD_ALARM_RESULT_KEY = "addAlarmResult"
const val UPDATE_ALARM_RESULT_KEY = "updateAlarmResult"
const val DELETE_ALARM_RESULT_KEY = "deleteAlarmResult"

fun NavGraphBuilder.homeNavGraph(
    navigator: OrbitNavigator,
    snackBarHostState: SnackbarHostState,
) {
    navigation<HomeBaseRoute>(
        startDestination = HomeDestination.Route,
    ) {
        composable<HomeDestination.Route> {
            HomeRoute(
                navigator = navigator,
                snackBarHostState = snackBarHostState,
            )
        }

        composable<HomeDestination.AlarmAddEdit> {
            AlarmAddEditRoute(
                navigator = navigator,
                snackBarHostState = snackBarHostState,
            )
        }
    }
}
