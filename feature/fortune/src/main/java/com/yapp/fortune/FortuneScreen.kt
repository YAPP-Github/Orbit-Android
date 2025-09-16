package com.yapp.fortune

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.fortune.component.FortuneTopAppBar
import com.yapp.fortune.component.SlidingIndicator
import com.yapp.fortune.page.FortunePager
import com.yapp.ui.component.lottie.LottieAnimation
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun FortuneRoute(
    viewModel: FortuneViewModel = hiltViewModel(),
    navigator: OrbitNavigator,
) {
    val analyticsHelper = LocalAnalyticsHelper.current
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        initialPage = state.currentStep,
        pageCount = { state.fortunePages.size + 2 },
    )

    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var previousPage by remember { mutableIntStateOf(pagerState.currentPage) }

    BackHandler {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "fortune_exit",
                properties = mapOf(
                    AnalyticsEvent.FortunePropertiesKeys.FORTUNE_PAGE_NUMBER to pagerState.currentPage + 1,
                ),
            ),
        )
        navigator.navigateBack()
    }

    LaunchedEffect(pagerState.currentPage) {
        val eventType = when (pagerState.currentPage) {
            0 -> "fortune_view_today"
            1 -> "fortune_view_category1"
            2 -> "fortune_view_category2"
            3 -> "fortune_view_style"
            4 -> "fortune_view_refer"
            5 -> "fortune_view_end"
            else -> ""
        }

        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = eventType,
                properties = mapOf(
                    AnalyticsEvent.FortunePropertiesKeys.FORTUNE_PAGE_NUMBER to pagerState.currentPage + 1,
                ),
            ),
        )

        if (pagerState.currentPage != previousPage) {
            val endTime = System.currentTimeMillis()
            val duration = ((endTime - startTime).toDouble() / 1000) // 초 단위로 변환
            val truncatedDuration = BigDecimal(duration).setScale(2, RoundingMode.DOWN).toDouble()

            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "fortune_time_spent",
                    properties = mapOf(
                        AnalyticsEvent.FortunePropertiesKeys.FORTUNE_PAGE_NUMBER to previousPage + 1,
                        AnalyticsEvent.FortunePropertiesKeys.DURATION to truncatedDuration,
                    ),
                ),
            )

            startTime = endTime
            previousPage = pagerState.currentPage
        }

        if (state.currentStep != pagerState.currentPage) {
            viewModel.processAction(FortuneContract.Action.UpdateStep(pagerState.currentPage))
        }
    }

    FortuneScreen(
        state = state,
        pagerState = pagerState,
        onNextStep = { viewModel.processAction(FortuneContract.Action.NextStep) },
        onNavigateToHome = { viewModel.processAction(FortuneContract.Action.NavigateToHome) },
        onCloseClick = {
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "fortune_exit",
                    properties = mapOf(
                        AnalyticsEvent.FortunePropertiesKeys.FORTUNE_PAGE_NUMBER to pagerState.currentPage + 1,
                    ),
                ),
            )
            viewModel.processAction(FortuneContract.Action.NavigateToHome)
        },
    )
}

@Composable
fun FortuneScreen(
    state: FortuneContract.State,
    pagerState: PagerState,
    onNextStep: () -> Unit,
    onNavigateToHome: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val backgroundRes = when (state.currentStep) {
        0 -> core.designsystem.R.drawable.ic_fortune_letter_background
        in 1..4 -> core.designsystem.R.drawable.ic_fortune_horoscope_background
        else -> core.designsystem.R.drawable.ic_fortune_complete_background
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4891F0))
            .navigationBarsPadding(),
    ) {
        if (state.isLoading) {
            FortuneLoadingScreen()
        } else {
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FortuneTopAppBar(
                    titleLabel = "미래에서 온 편지",
                    onCloseClick = onCloseClick,
                )

                SlidingIndicator(
                    currentIndex = pagerState.currentPage,
                    count = 6,
                    dotHeight = 5.dp,
                    spacing = 4.dp,
                    inactiveColor = OrbitTheme.colors.white.copy(0.2f),
                    activeColor = OrbitTheme.colors.white,
                )

                FortunePager(state, pagerState, onNextStep, onNavigateToHome)
            }
        }
    }
}

@Composable
fun FortuneLoadingScreen() {
    var isDelivering by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            isDelivering = !isDelivering
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val imageRes = if (isDelivering) {
                core.designsystem.R.drawable.ic_fortune_delivering_speech_bubble
            } else {
                core.designsystem.R.drawable.ic_fortune_waiting_speech_bubble
            }
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
            )

            LottieAnimation(
                modifier = Modifier
                    .width(375.dp)
                    .height(267.dp),
                resId = core.designsystem.R.raw.fortune_loading,
            )
        }
    }
}

@Composable
@Preview
private fun FortuneLoadingScreenPreview() {
    OrbitTheme {
        FortuneLoadingScreen()
    }
}
