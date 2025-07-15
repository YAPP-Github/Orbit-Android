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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
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
fun MissionRoute(viewModel: MissionViewModel = hiltViewModel()) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val shakeDetector = remember {
        ShakeDetector(context) {
            viewModel.processAction(MissionContract.Action.ShakeCard)
        }
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

/**
 * Mission 상태에 따라 적절한 화면을 구성하는 메인 컨테이너.
 * 로딩, 콘텐츠, 성공 오버레이, 다이얼로그 등 분기 처리 포함.
 */
@Composable
fun MissionScreen(
    stateProvider: () -> MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
    onFinish: () -> Unit,
) {
    val state = stateProvider()
    val analytics = LocalAnalyticsHelper.current

    BackHandler {
        eventDispatcher(
            if (state.showExitDialog) {
                MissionContract.Action.HideExitDialog
            } else {
                MissionContract.Action.ShowExitDialog
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        MissionContent(state, eventDispatcher)

        if (state.showExitDialog) {
            ExitDialog(state, eventDispatcher, onFinish, analytics)
        }

        if (state.isMissionCompleted) {
            MissionSuccessOverlay()
        }

        state.errorMessage?.let {
            ErrorDialog(message = it) {
                eventDispatcher(MissionContract.Action.RetryPostFortune)
            }
        }
    }
}

/**
 * 미션 콘텐츠 본문. TopBar, 진행 바, 상태별 게임 포함.
 */
@Composable
fun MissionContent(
    state: MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MissionTopAppBar(onExit = { eventDispatcher(MissionContract.Action.ShowExitDialog) })
        MissionProgressBarSection(state)
        MissionLabel(state)
        Spacer(modifier = Modifier.heightForScreenPercentage(0.0665f))

        when (state.missionType) {
            MissionType.SHAKE -> {
                if (state.shakeCount == 0) {
                    MissionShakeInitialImage()
                } else {
                    FlipCard(state = state, eventDispatcher = eventDispatcher)
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

/**
 * '나가기' 버튼이 포함된 미션 상단 앱바 영역.
 */
@Composable
fun MissionTopAppBar(onExit: () -> Unit) {
    Spacer(modifier = Modifier.heightForScreenPercentage(0.066f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Row(
            modifier = Modifier.customClickable(
                rippleEnabled = false,
                fadeOnPress = true,
                pressedAlpha = 0.5f,
                onClick = onExit,
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
}

/**
 * 미션 진행도 ProgressBar 섹션.
 */
@Composable
fun MissionProgressBarSection(state: MissionContract.State) {
    Spacer(modifier = Modifier.heightForScreenPercentage(0.0246f))
    MissionProgressBar(
        currentProgress = when (state.missionType) {
            MissionType.SHAKE -> state.shakeCount
            MissionType.TAP -> state.clickCount
            else -> 0
        },
        totalProgress = 10,
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .padding(horizontal = 20.dp),
    )
    Spacer(modifier = Modifier.heightForScreenPercentage(0.06f))
}

/**
 * 미션 안내 문구 및 현재 카운트.
 */
@Composable
fun MissionLabel(state: MissionContract.State) {
    val instruction =
        if (state.missionType == MissionType.SHAKE) "10회를 흔들어 부적을 뒤집어줘" else "10회를 눌러 편지를 열어줘"
    val count = if (state.missionType == MissionType.SHAKE) state.shakeCount else state.clickCount

    Text(
        text = instruction,
        color = OrbitTheme.colors.white,
        style = OrbitTheme.typography.heading2SemiBold,
    )
    Spacer(modifier = Modifier.heightForScreenPercentage(0.005f))
    Text(
        text = count.toString(),
        color = OrbitTheme.colors.white,
        style = OrbitTheme.typography.displaySemiBold,
    )
}

/**
 * 흔들기 미션 초기 이미지.
 */
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

/**
 * 클릭 미션 카드. 클릭 시 애니메이션 및 상태 변화.
 */
@Composable
fun MissionClickCard(
    state: MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
) {
    if (state.clickCount == 0) {
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

/**
 * 미션 종료 시 나가기 다이얼로그.
 */
@Composable
fun ExitDialog(
    state: MissionContract.State,
    eventDispatcher: (MissionContract.Action) -> Unit,
    onFinish: () -> Unit,
    analytics: AnalyticsHelper,
) {
    OrbitDialog(
        title = "나가면 운세를 받을 수 없어요",
        message = "미션을 수행하지 않고 나가시겠어요?",
        confirmText = "나가기",
        cancelText = "취소",
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

/**
 * 미션 성공 시 오버레이 화면.
 */
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

/**
 * 오류 발생 시 다이얼로그.
 */
@Composable
fun ErrorDialog(message: String, onConfirm: () -> Unit) {
    OrbitDialog(
        title = "오류",
        message = message,
        confirmText = "확인",
        onConfirm = onConfirm,
    )
}

/**
 * 로딩 화면. 미션 타입 로딩 중에 표시.
 */
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
private fun MissionRoutePreview() {
    MissionScreen(
        stateProvider = {
            MissionContract.State(
                isMissionTypeLoading = false,
                missionType = MissionType.SHAKE,
                shakeCount = 0,
                clickCount = 0,
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
