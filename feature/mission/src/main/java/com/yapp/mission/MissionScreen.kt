package com.yapp.mission

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.MissionMode
import com.yapp.domain.model.MissionType
import com.yapp.mission.component.FlipCard
import com.yapp.mission.component.MissionProgressBar
import com.yapp.ui.component.dialog.OrbitDialog
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.extensions.customClickable
import com.yapp.ui.utils.heightForScreenPercentage
import com.yapp.ui.utils.paddingForScreenPercentage
import feature.mission.R

@Composable
fun MissionRoute(
    viewModel: MissionViewModel = hiltViewModel(),
    navigator: OrbitNavigator,
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val shakeDetector = remember {
        ShakeDetector(context) {
            viewModel.processAction(MissionContract.Action.ShakeCard)
        }
    }

    BackHandler {
        if (state.missionMode == MissionMode.PREVIEW) {
            navigator.navigateBack()
            return@BackHandler
        }

        viewModel.processAction(
            if (state.showExitDialog) {
                MissionContract.Action.HideExitDialog
            } else {
                MissionContract.Action.ShowExitDialog
            },
        )
    }

    LaunchedEffect(Unit) {
        shakeDetector.start()
    }

    DisposableEffect(Unit) {
        onDispose { shakeDetector.stop() }
    }

    MissionScreen(
        stateProvider = { state },
        eventDispatcher = viewModel::processAction,
        onFinish = {
            (context as? ComponentActivity)?.finish()
        },
    )
}

@Composable
fun MissionScreen(
    stateProvider: () -> MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
    onFinish: () -> Unit,
) {
    val state = stateProvider()
    val analytics = LocalAnalyticsHelper.current

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.isMissionTypeLoading || state.missionType == MissionType.NONE) {
            MissionLoadingScreen()
            return
        }

        Image(
            painter = painterResource(id = core.designsystem.R.drawable.img_mission_progress_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )

        MissionContent(
            state = state,
            eventDispatcher = eventDispatcher,
        )

        if (state.showExitDialog) {
            ExitDialog(state, eventDispatcher, onFinish, analytics)
        }

        if (state.isMissionCompleted) {
            MissionSuccessOverlay()
        }

        if (state.missionMode == MissionMode.PREVIEW) {
            val insets = WindowInsets.navigationBars.asPaddingValues()

            Button(
                onClick = {
                    eventDispatcher(MissionContract.Action.NavigateBack)
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrbitTheme.colors.white,
                    contentColor = OrbitTheme.colors.gray_900,
                ),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp,
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = insets.calculateBottomPadding() + 28.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.mission_preview_exit),
                    style = OrbitTheme.typography.body1SemiBold,
                )
            }
        }
    }
}

@Composable
fun MissionContent(
    state: MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MissionTopAppBar(
            mode = state.missionMode,
            onExit = { eventDispatcher(MissionContract.Action.ShowExitDialog) },
        )
        MissionProgressBarSection(state)
        MissionLabel(state)
        Spacer(modifier = Modifier.heightForScreenPercentage(0.0665f))

        when (state.missionType) {
            MissionType.SHAKE -> {
                if (state.currentCount == 0) {
                    MissionShakeInitialImage()
                } else {
                    FlipCard(state)
                }
            }

            MissionType.TAP -> {
                MissionClickCard(state, eventDispatcher)
            }

            MissionType.NONE -> {
                Log.e("MissionContent", "Invalid or NONE MissionType: ${state.missionType}")
            }
        }
    }
}

@Composable
fun MissionTopAppBar(
    mode: MissionMode,
    onExit: () -> Unit,
) {
    Spacer(modifier = Modifier.heightForScreenPercentage(0.066f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Row(
            modifier = Modifier
                .height(26.dp)
                .customClickable(
                    rippleEnabled = false,
                    fadeOnPress = true,
                    pressedAlpha = 0.5f,
                    onClick = onExit,
                ),
        ) {
            if (mode == MissionMode.REAL) {
                Icon(
                    painter = painterResource(id = core.designsystem.R.drawable.ic_cancel),
                    contentDescription = null,
                    tint = OrbitTheme.colors.white,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(id = R.string.exit),
                    color = OrbitTheme.colors.white,
                    style = OrbitTheme.typography.body1SemiBold,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically),
                )
            }
        }
    }
}

@Composable
fun MissionProgressBarSection(state: MissionContract.State) {
    Spacer(modifier = Modifier.heightForScreenPercentage(0.0246f))
    MissionProgressBar(
        currentProgress = state.currentCount,
        totalProgress = state.missionCount,
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .padding(horizontal = 20.dp),
    )
    Spacer(modifier = Modifier.heightForScreenPercentage(0.06f))
}

@Composable
fun MissionLabel(state: MissionContract.State) {
    val instruction = stringResource(
        id = when (state.missionType) {
            MissionType.SHAKE -> R.string.mission_instruction_shake
            else -> R.string.mission_instruction_tap
        },
        state.missionCount,
    )

    Text(
        text = instruction,
        color = OrbitTheme.colors.white,
        style = OrbitTheme.typography.heading2SemiBold,
    )
    Spacer(modifier = Modifier.heightForScreenPercentage(0.005f))
    Text(
        text = state.currentCount.toString(),
        color = OrbitTheme.colors.white,
        style = OrbitTheme.typography.displaySemiBold,
    )
}

@Composable
fun MissionShakeInitialImage() {
    Image(
        painter = painterResource(id = core.designsystem.R.drawable.img_mission_main),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .scale(1.1f),
    )
}

@Composable
fun MissionClickCard(
    state: MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
) {
    if (state.currentCount == 0) {
        Image(
            painter = painterResource(id = core.designsystem.R.drawable.ic_mission_main_letter),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .scale(1.0f)
                .pointerInput(Unit) {
                    detectTapGestures { eventDispatcher(MissionContract.Action.ClickCard) }
                },
        )
    } else {
        Crossfade(targetState = state.showFinalAnimation, animationSpec = tween(500)) { showFinal ->
            LottieAnimation(
                modifier = Modifier
                    .aspectRatio(12f / 9f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (!showFinal) eventDispatcher(MissionContract.Action.ClickCard)
                        }
                    },
                resId = if (showFinal) core.designsystem.R.raw.mission_letter_open else core.designsystem.R.raw.mission_letter_tap,
                play = state.playWhenClick || showFinal,
                restartOnPlay = true,
                iterations = 1,
            )
        }
    }
}

@Composable
fun ExitDialog(
    state: MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
    onFinish: () -> Unit,
    analytics: AnalyticsHelper,
) {
    OrbitDialog(
        title = stringResource(id = R.string.mission_exit_dialog_title),
        message = stringResource(id = R.string.mission_exit_dialog_message),
        confirmText = stringResource(id = R.string.exit),
        cancelText = stringResource(id = R.string.cancel),
        onConfirm = {
            analytics.logEvent(
                AnalyticsEvent(
                    type = "mission_fail",
                    properties = mapOf(
                        AnalyticsEvent.MissionPropertiesKeys.MISSION_TYPE to when (state.missionType) {
                            MissionType.SHAKE -> "shake"
                            MissionType.TAP -> "click"
                            else -> ""
                        },
                    ),
                ),
            )
            onFinish()
        },
        onCancel = { eventDispatcher(MissionContract.Action.HideExitDialog) },
    )
}

@Composable
fun MissionSuccessOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LottieAnimation(
                modifier = Modifier.matchParentSize(),
                scaleXAdjustment = 1.3f,
                scaleYAdjustment = 1.3f,
                resId = core.designsystem.R.raw.mission_success,
                iterations = 1,
                play = true,
            )
            Text(
                text = stringResource(id = R.string.mission_success),
                color = OrbitTheme.colors.white,
                style = OrbitTheme.typography.title1Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .paddingForScreenPercentage(topPercentage = 0.564f),
            )
        }
    }
}

@Composable
fun MissionLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            modifier = Modifier.size(70.dp),
            resId = core.designsystem.R.raw.star_loading,
        )
    }
}

@Composable
@Preview
private fun MissionRouteReal() {
    MissionScreen(
        stateProvider = {
            MissionContract.State(
                isMissionTypeLoading = false,
                missionType = MissionType.TAP,
                currentCount = 0,
                showFinalAnimation = false,
                playWhenClick = false,
                showExitDialog = false,
                isMissionCompleted = false,
            )
        },
        eventDispatcher = {},
        onFinish = {},
    )
}

@Composable
@Preview
private fun MissionRoutePreview() {
    MissionScreen(
        stateProvider = {
            MissionContract.State(
                missionMode = MissionMode.PREVIEW,
                isMissionTypeLoading = false,
                missionType = MissionType.TAP,
                currentCount = 0,
                showFinalAnimation = false,
                playWhenClick = false,
                showExitDialog = false,
                isMissionCompleted = false,
            )
        },
        eventDispatcher = {},
        onFinish = {},
    )
}
