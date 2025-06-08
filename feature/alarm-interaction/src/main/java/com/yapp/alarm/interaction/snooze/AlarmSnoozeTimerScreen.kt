package com.yapp.alarm.interaction.snooze

import Pretendard
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.utils.heightForScreenPercentage
import feature.alarm.interaction.R

@Composable
internal fun AlarmSnoozeTimerRoute(
    viewModel: AlarmSnoozeTimerViewModel = hiltViewModel(),
    navigator: OrbitNavigator,
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val sideEffect = viewModel.container.sideEffectFlow

    LaunchedEffect(sideEffect) {
        sideEffect.collect { }
    }

    AlarmSnoozeTimerScreen(
        stateProvider = { state },
        eventDispatcher = viewModel::processAction,
    )
}

@Composable
internal fun AlarmSnoozeTimerScreen(
    stateProvider: () -> AlarmSnoozeTimerContract.State,
    eventDispatcher: (AlarmSnoozeTimerContract.Action) -> Unit,
) {
    val state = stateProvider()
    val context = LocalContext.current

    if (state.initialLoading) {
        AlarmSnoozeLoadingScreen()
    } else {
        AlarmSnoozeContent(
            remainingSeconds = state.remainingSeconds,
            totalSeconds = state.totalSeconds,
            isFirstMission = state.isFirstMission,
            onDismissClick = {
                eventDispatcher(AlarmSnoozeTimerContract.Action.Dismiss)
                (context as? ComponentActivity)?.finish()
            },
        )
    }
}

@Composable
private fun AlarmSnoozeLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3D5372)),
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
private fun AlarmSnoozeContent(
    remainingSeconds: Int,
    totalSeconds: Int,
    isFirstMission: Boolean?,
    onDismissClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3D5372)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.heightForScreenPercentage(0.14f))

        Text(
            text = stringResource(id = R.string.alarm_snooze_timer_title),
            style = OrbitTheme.typography.heading2SemiBold,
            color = OrbitTheme.colors.white,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AlarmSnoozeTimer(
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds,
            )
        }

        if (isFirstMission != null) {
            AlarmOffButton(
                onClick = onDismissClick,
                isFirstMission = isFirstMission,
            )
        } else {
            Spacer(modifier = Modifier.height(58.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun AlarmSnoozeTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
) {
    Box(
        modifier = Modifier.size(274.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = remainingSeconds.toFloat() / totalSeconds,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.alarm_snooze_timer_remaining_time),
                style = OrbitTheme.typography.headline2SemiBold,
                color = OrbitTheme.colors.white,
            )

            Text(
                text = formatSecondsToTime(remainingSeconds),
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard,
                    lineHeight = 62.sp,
                ),
                color = OrbitTheme.colors.white,
            )
        }
    }
}

@Composable
private fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    size: Dp = 274.dp,
    backgroundWidth: Dp = 20.dp,
    progressWidth: Dp = 12.dp,
    progressBlurRadius: Dp = 5.dp,
) {
    val backgroundColor = OrbitTheme.colors.white.copy(alpha = 0.2f)
    val progressColor = OrbitTheme.colors.sub_main
    val progressBlurColor = OrbitTheme.colors.main.copy(0.6f)

    Canvas(
        modifier = modifier.size(size),
    ) {
        val backgroundStrokePx = backgroundWidth.toPx()
        val progressStrokePx = progressWidth.toPx()

        val offset = (backgroundStrokePx - progressStrokePx) / 2

        val progressBlurEffect = BlurMaskFilter(
            progressBlurRadius.toPx(),
            BlurMaskFilter.Blur.NORMAL,
        )

        val progressPaint = Paint().apply {
            color = progressBlurColor.toArgb()
            maskFilter = progressBlurEffect
            style = Paint.Style.STROKE
            strokeWidth = progressStrokePx
            strokeCap = Paint.Cap.ROUND
        }

        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val radius = (size.toPx() - backgroundStrokePx) / 2

        drawCircle(
            color = backgroundColor,
            center = center,
            radius = radius,
            style = Stroke(width = backgroundStrokePx),
        )

        drawIntoCanvas { canvas ->
            val rectF = android.graphics.RectF(
                offset * 2.5f,
                offset * 2.5f,
                size.toPx() - (offset * 2.5f),
                size.toPx() - (offset * 2.5f),
            )
            canvas.nativeCanvas.drawArc(
                rectF,
                -90f,
                360 * progress,
                false,
                progressPaint,
            )
        }

        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            topLeft = Offset(progressStrokePx / 2 + offset, progressStrokePx / 2 + offset),
            size = Size(size.toPx() - progressStrokePx - 2 * offset, size.toPx() - progressStrokePx - 2 * offset),
            style = Stroke(width = progressStrokePx, cap = StrokeCap.Round),
        )
    }
}

private fun formatSecondsToTime(seconds: Int): String {
    val minutes = (seconds / 60).toString().padStart(2, '0')
    val remainingSeconds = (seconds % 60).toString().padStart(2, '0')
    return "$minutes:$remainingSeconds"
}

@Composable
private fun AlarmOffButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isFirstMission: Boolean,
    height: Dp = 58.dp,
    containerColor: Color = OrbitTheme.colors.white.copy(alpha = 0.2f),
    contentColor: Color = OrbitTheme.colors.white,
    shape: Shape = CircleShape,
) {
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier.height(height),
        contentPadding = PaddingValues(
            horizontal = 55.dp,
        ),
    ) {
        Text(
            text = if (isFirstMission) {
                stringResource(id = R.string.alarm_off_mission_start_btn)
            } else {
                stringResource(id = R.string.alarm_off_btn)
            },
            style = OrbitTheme.typography.headline2SemiBold,
        )
    }
}

@Preview
@Composable
internal fun PreviewAlarmSnoozeTimerScreen() {
    OrbitTheme {
        AlarmSnoozeTimerScreen(
            stateProvider = { AlarmSnoozeTimerContract.State() },
            eventDispatcher = {},
        )
    }
}
