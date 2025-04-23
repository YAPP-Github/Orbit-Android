package com.yapp.alarm.interaction.action

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.ui.component.banner.AdsBanner
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.utils.heightForScreenPercentage
import feature.alarm.interaction.R
import java.util.Locale

@Composable
internal fun AlarmActionRoute(
    viewModel: AlarmActionViewModel = hiltViewModel(),
    navigator: OrbitNavigator,
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val sideEffect = viewModel.container.sideEffectFlow

    LaunchedEffect(sideEffect) {
        sideEffect.collect { action ->
            when (action) {
                is AlarmActionContract.SideEffect.Navigate -> {
                    navigator.navigateTo(
                        route = action.route,
                        popUpTo = action.popUpTo,
                        inclusive = action.inclusive,
                    )
                }
            }
        }
    }

    AlarmActionScreen(
        stateProvider = { state },
        eventDispatcher = viewModel::processAction,
    )
}

@Composable
internal fun AlarmActionScreen(
    stateProvider: () -> AlarmActionContract.State,
    eventDispatcher: (AlarmActionContract.Action) -> Unit,
) {
    val state = stateProvider()
    val context = LocalContext.current

    if (state.initialLoading) {
        AlarmActionLoadingScreen()
    } else {
        AlarmActionContent(
            isAm = state.isAm,
            hour = state.hour,
            minute = state.minute,
            todayDate = state.todayDate,
            snoozeEnabled = state.snoozeEnabled,
            snoozeInterval = state.snoozeInterval,
            snoozeCount = state.snoozeCount,
            onSnoozeClick = { eventDispatcher(AlarmActionContract.Action.Snooze) },
            onDismissClick = {
                eventDispatcher(AlarmActionContract.Action.Dismiss)
                (context as? androidx.activity.ComponentActivity)?.finish()
            },
        )
    }
}

@Composable
private fun AlarmActionLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF496381)),
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.Center),
            resId = core.designsystem.R.raw.star_loading,
        )
    }
}

@Composable
private fun AlarmActionContent(
    isAm: Boolean,
    hour: Int,
    minute: Int,
    todayDate: String,
    snoozeEnabled: Boolean,
    snoozeInterval: Int,
    snoozeCount: Int,
    onSnoozeClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    Box(modifier = Modifier.statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFF496381),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier.heightForScreenPercentage(
                    0.17f,
                ),
            )

            AlarmTime(
                isAm = isAm,
                hour = hour,
                minute = minute,
                todayDate = todayDate,
            )

            Spacer(modifier = Modifier.height(102.dp))

            Icon(
                painter = painterResource(id = core.designsystem.R.drawable.ic_alarm_action_character),
                tint = Color(0xFF07203E),
                contentDescription = "Alarm Action Character",
            )

            Spacer(modifier = Modifier.height(56.dp))

            if (snoozeEnabled && snoozeCount != 0) {
                AlarmSnoozeButton(
                    snoozeInterval = snoozeInterval,
                    snoozeCount = snoozeCount,
                    onSnoozeClick = onSnoozeClick,
                )
            } else {
                Spacer(modifier = Modifier.height(54.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            OrbitButton(
                label = stringResource(id = R.string.alarm_off_mission_start_btn),
                enabled = true,
                modifier = Modifier
                    .padding(
                        start = 40.dp,
                        end = 40.dp,
                        bottom = 48.dp,
                    )
                    .height(62.dp),
                onClick = onDismissClick,
            )
        }

        AdsBanner()
    }
}

@Composable
private fun AlarmTime(
    isAm: Boolean,
    hour: Int,
    minute: Int,
    todayDate: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = if (isAm) "오전" else "오후",
                style = OrbitTheme.typography.title2Medium,
                color = OrbitTheme.colors.white,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$hour:${String.format(Locale.getDefault(), "%02d", minute)}",
                style = OrbitTheme.typography.displaySemiBold,
                color = OrbitTheme.colors.white,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = todayDate,
            style = OrbitTheme.typography.heading2SemiBold,
            color = OrbitTheme.colors.white,
        )
    }
}

@Composable
private fun AlarmSnoozeButton(
    snoozeInterval: Int,
    snoozeCount: Int,
    onSnoozeClick: () -> Unit,
) {
    Surface(
        color = OrbitTheme.colors.white.copy(
            alpha = 0.3f,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = OrbitTheme.colors.white.copy(
                alpha = 0.2f,
            ),
        ),
        shape = CircleShape,
        onClick = onSnoozeClick,
    ) {
        Row(
            modifier = Modifier
                .height(54.dp)
                .padding(
                    start = 20.dp,
                    end = 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.alarm_snooze_btn, snoozeInterval),
                style = OrbitTheme.typography.headline2SemiBold,
                color = OrbitTheme.colors.white,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                modifier = Modifier
                    .background(
                        color = OrbitTheme.colors.main.copy(
                            alpha = 0.3f,
                        ),
                        shape = CircleShape,
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 6.dp,
                    ),
                text = if (snoozeCount == -1) {
                    stringResource(id = R.string.alarm_snooze_count_infinite)
                } else {
                    stringResource(
                        id = R.string.alarm_snooze_count,
                        snoozeCount,
                    )
                },
                style = OrbitTheme.typography.body2Medium,
                color = OrbitTheme.colors.main,
            )
        }
    }
}

@Preview
@Composable
internal fun AlarmActionScreenPreview() {
    OrbitTheme {
        AlarmActionScreen(
            stateProvider = {
                AlarmActionContract.State(
                    initialLoading = false,
                    isAm = true,
                    hour = 10,
                    minute = 30,
                    todayDate = "10월 10일 월요일",
                    snoozeInterval = 5,
                    snoozeCount = -1,
                )
            },
            eventDispatcher = {},
        )
    }
}
