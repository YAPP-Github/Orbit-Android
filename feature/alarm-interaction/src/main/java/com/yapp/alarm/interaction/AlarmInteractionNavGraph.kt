package com.yapp.alarm.interaction

import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.yapp.alarm.interaction.action.AlarmActionRoute
import com.yapp.alarm.interaction.snooze.AlarmSnoozeTimerRoute
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.route.AlarmInteractionBaseRoute
import com.yapp.common.navigation.route.AlarmInteractionDestination
import com.yapp.domain.model.Alarm
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

val AlarmArgType = object : NavType<Alarm>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Alarm? {
        return bundle.getString(key)?.let { Alarm.fromJson(it) }
    }

    override fun parseValue(value: String): Alarm {
        return Alarm.fromJson(value)
    }

    override fun put(bundle: Bundle, key: String, value: Alarm) {
        bundle.putString(key, Json.encodeToString(Alarm.serializer(), value))
    }
}

fun NavGraphBuilder.alarmInteractionNavGraph(
    navigator: OrbitNavigator,
    alarm: Alarm?,
) {
    navigation<AlarmInteractionBaseRoute>(
        startDestination = AlarmInteractionDestination.Route,
    ) {
        composable<AlarmInteractionDestination.Route> {
            LaunchedEffect(Unit) {
                alarm?.let {
                    navigator.navigateToAlarmAction(
                        it,
                        navOptions {
                            popUpTo(AlarmInteractionBaseRoute) {
                                inclusive = true
                            }
                        },
                    )
                } ?: run {
                    navigator.navigateToHome(
                        navOptions {
                            popUpTo(AlarmInteractionBaseRoute) {
                                inclusive = true
                            }
                        },
                    )
                }
            }
        }

        composable<AlarmInteractionDestination.AlarmAction>(
            typeMap = mapOf(typeOf<Alarm>() to AlarmArgType),
        ) {
            AlarmActionRoute(
                navigator = navigator,
            )
        }

        composable<AlarmInteractionDestination.AlarmSnoozeTimer>(
            typeMap = mapOf(typeOf<Alarm>() to AlarmArgType),
        ) {
            AlarmSnoozeTimerRoute(
                navigator = navigator,
            )
        }
    }
}
