package com.yapp.mission

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.MissionType
import com.yapp.mission.component.FlipCard
import com.yapp.mission.component.MissionProgressBar
import com.yapp.ui.component.dialog.OrbitDialog
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.extensions.customClickable
import com.yapp.ui.utils.heightForScreenPercentage
import com.yapp.ui.utils.paddingForScreenPercentage

@Composable
fun MissionProgressRoute(viewModel: MissionViewModel = hiltViewModel()) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shakeDetector = remember { ShakeDetector(context) { viewModel.processAction(MissionContract.Action.ShakeCard) } }

    LaunchedEffect(Unit) {
        shakeDetector.start()
        viewModel.processAction(MissionContract.Action.StartOverlayTimer)
    }

    DisposableEffect(Unit) {
        onDispose { shakeDetector.stop() }
    }

    MissionProgressScreen(
        stateProvider = { state },
        eventDispatcher = viewModel::processAction,
    )
}

@Composable
fun MissionProgressScreen(
    stateProvider: () -> MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
) {
    val state = stateProvider()

    val analyticsHelper = LocalAnalyticsHelper.current
    val context = LocalContext.current

    BackHandler {
        if (state.showExitDialog) {
            eventDispatcher(MissionContract.Action.HideExitDialog)
        } else {
            eventDispatcher(MissionContract.Action.ShowExitDialog)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = core.designsystem.R.drawable.img_mission_progress_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
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
                            .customClickable(
                                rippleEnabled = false,
                                fadeOnPress = true,
                                pressedAlpha = 0.5f,
                                onClick = { eventDispatcher(MissionContract.Action.ShowExitDialog) },
                            ),
                    ) {
                        Icon(
                            painter = painterResource(id = core.designsystem.R.drawable.ic_cancel),
                            contentDescription = null,
                            tint = OrbitTheme.colors.white,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "나가기",
                            color = OrbitTheme.colors.white,
                            style = OrbitTheme.typography.body1SemiBold,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .align(Alignment.CenterVertically),
                        )
                    }
                }

                Spacer(modifier = Modifier.heightForScreenPercentage(0.0246f))
                MissionProgressBar(
                    currentProgress = when (state.missionType) {
                        is MissionType.Shake -> state.shakeCount
                        is MissionType.Click -> state.clickCount
                    },
                    totalProgress = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .padding(horizontal = 20.dp)
                        .alpha(if (state.showOverlay) 0f else 1f),
                )
                Spacer(modifier = Modifier.heightForScreenPercentage(0.06f))
                Text(
                    text = if (state.missionType is MissionType.Shake) "10회를 흔들어야 운세를 받아요" else "10회를 눌러야 운세를 받아요",
                    color = OrbitTheme.colors.white,
                    style = OrbitTheme.typography.heading2SemiBold,
                    modifier = Modifier.alpha(if (state.showOverlay) 0f else 1f),
                )
                Spacer(modifier = Modifier.heightForScreenPercentage(0.005f))
                Text(
                    text = when (state.missionType) {
                        is MissionType.Shake -> state.shakeCount.toString()
                        is MissionType.Click -> state.clickCount.toString()
                    },
                    color = OrbitTheme.colors.white,
                    style = OrbitTheme.typography.displaySemiBold,
                    modifier = Modifier.alpha(if (state.showOverlay) 0f else 1f),
                )

                Spacer(modifier = Modifier.heightForScreenPercentage(if (state.missionType is MissionType.Shake) 0.0665f else 0.1f))
                if (state.missionType is MissionType.Shake) {
                    FlipCard(
                        state = state,
                        eventDispatcher = eventDispatcher,
                    )
                } else if (state.missionType is MissionType.Click) {
                    Crossfade(
                        targetState = state.showFinalAnimation,
                        animationSpec = tween(durationMillis = 500),
                    ) { showFinal ->
                        LottieAnimation(
                            modifier = Modifier
                                .aspectRatio(12f / 9f)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!state.showFinalAnimation) {
                                                eventDispatcher(MissionContract.Action.ClickCard)
                                            }
                                        },
                                    )
                                },
                            resId = if (showFinal) {
                                core.designsystem.R.raw.mission_letter_open
                            } else {
                                core.designsystem.R.raw.mission_letter_tap
                            },
                            play = state.playWhenClick || showFinal,
                            restartOnPlay = true,
                            iterations = 1,
                        )
                    }
                }
            }
        }

        if (state.showOverlay) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OrbitTheme.colors.gray_900.copy(alpha = 0.7f))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.heightForScreenPercentage(0.226f))

                AnimatedVisibility(
                    visible = state.showOverlayText,
                    enter = scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                ) {
                    Text(
                        text = if (state.missionType is MissionType.Shake) "흔들기 시작!" else "누르기 시작!",
                        color = OrbitTheme.colors.white,
                        style = OrbitTheme.typography.title1Bold,
                    )
                }
            }
        }

        if (state.showExitDialog) {
            OrbitDialog(
                title = "나가면 운세를 받을 수 없어요",
                message = "미션을 수행하지 않고 나가시겠어요?",
                confirmText = "나가기",
                cancelText = "취소",
                onConfirm = {
                    analyticsHelper.logEvent(
                        AnalyticsEvent(
                            type = "mission_fail",
                            properties = mapOf(
                                AnalyticsEvent.MissionPropertiesKeys.MISSION_TYPE to when (state.missionType) {
                                    is MissionType.Shake -> "shake"
                                    is MissionType.Click -> "click"
                                },
                            ),
                        ),
                    )
                    (context as? androidx.activity.ComponentActivity)?.finish()
                },
                onCancel = {
                    eventDispatcher(MissionContract.Action.HideExitDialog)
                },
            )
        }

        if (state.isMissionCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OrbitTheme.colors.gray_900.copy(alpha = 0.7f))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LottieAnimation(
                        modifier = Modifier
                            .matchParentSize(),
                        scaleXAdjustment = 1.3f,
                        scaleYAdjustment = 1.3f,
                        resId = core.designsystem.R.raw.mission_success,
                        iterations = 1,
                        play = true,
                    )
                    Text(
                        text = "미션 성공!",
                        color = OrbitTheme.colors.white,
                        style = OrbitTheme.typography.title1Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .paddingForScreenPercentage(topPercentage = 0.564f),
                    )
                }
            }
        }

        if (state.errorMessage != null) {
            OrbitDialog(
                title = "오류",
                message = state.errorMessage,
                confirmText = "확인",
                onConfirm = {
                    eventDispatcher(MissionContract.Action.RetryPostFortune)
                },
            )
        }
    }
}

@Composable
@Preview
fun MissionProgressRoutePreview() {
    MissionProgressScreen(
        stateProvider = { MissionContract.State() },
        eventDispatcher = {},
    )
}
