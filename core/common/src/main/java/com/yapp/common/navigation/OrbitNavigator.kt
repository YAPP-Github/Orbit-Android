package com.yapp.common.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.yapp.common.navigation.route.AlarmInteractionDestination
import com.yapp.common.navigation.route.FortuneBaseRoute
import com.yapp.common.navigation.route.FortuneDestination
import com.yapp.common.navigation.route.HomeBaseRoute
import com.yapp.common.navigation.route.HomeDestination
import com.yapp.common.navigation.route.MissionRoute
import com.yapp.common.navigation.route.OnboardingBaseRoute
import com.yapp.common.navigation.route.OnboardingDestination
import com.yapp.common.navigation.route.SettingBaseRoute
import com.yapp.common.navigation.route.SettingDestination
import com.yapp.common.navigation.route.SplashRoute
import com.yapp.common.navigation.route.WebViewRoute
import com.yapp.domain.model.Alarm

class OrbitNavigator(
    val navController: NavHostController,
) {
    val startDestination = SplashRoute

    fun navigateToOnboarding(navOptions: NavOptions? = null) {
        navController.navigate(OnboardingBaseRoute, navOptions)
    }

    fun navigateToOnboardingNextStep(currentStep: Int, navOptions: NavOptions? = null) {
        val instance = OnboardingDestination.getNextRouteForStep(currentStep)?.objectInstance
        if (instance != null) {
            navController.navigate(instance, navOptions)
        } else {
            Log.e("Navigator", "Invalid route at step: $currentStep")
        }
    }

    fun navigateToAddAlarm(navOptions: NavOptions? = null) {
        navController.navigate(HomeDestination.AlarmAddEdit(-1), navOptions)
    }

    fun navigateToEditAlarm(alarmId: Long, navOptions: NavOptions? = null) {
        navController.navigate(HomeDestination.AlarmAddEdit(alarmId), navOptions)
    }

    fun navigateToHome(navOptions: NavOptions? = null) {
        navController.navigate(HomeBaseRoute, navOptions)
    }

    fun navigateToAlarmAction(alarm: Alarm, navOptions: NavOptions? = null) {
        navController.navigate(AlarmInteractionDestination.AlarmAction(alarm), navOptions)
    }

    fun navigateToAlarmSnoozeTimer(alarm: Alarm, navOptions: NavOptions? = null) {
        navController.navigate(AlarmInteractionDestination.AlarmSnoozeTimer(alarm), navOptions)
    }

    fun navigateToMissionPreview(
        missionType: Int,
        missionCount: Int,
        navOptions: NavOptions? = null,
    ) {
        navController.navigate(
            MissionRoute(
                missionType = "$missionType",
                missionCount = "$missionCount",
                missionMode = "PREVIEW",
            ),
            navOptions,
        )
    }

    fun navigateToFortune(navOptions: NavOptions? = null) {
        navController.navigate(FortuneBaseRoute, navOptions)
    }

    fun navigateToFortuneReward(navOptions: NavOptions? = null) {
        navController.navigate(FortuneDestination.Reward, navOptions)
    }

    fun navigateToSetting(navOptions: NavOptions? = null) {
        navController.navigate(SettingBaseRoute, navOptions)
    }

    fun navigateToEditProfile(navOptions: NavOptions? = null) {
        navController.navigate(SettingDestination.EditProfile, navOptions)
    }

    fun navigateToEditBirthDay(navOptions: NavOptions? = null) {
        navController.navigate(SettingDestination.EditBirthday, navOptions)
    }

    fun navigateToWebView(url: String, navOptions: NavOptions? = null) {
        navController.navigate(WebViewRoute(url), navOptions)
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
