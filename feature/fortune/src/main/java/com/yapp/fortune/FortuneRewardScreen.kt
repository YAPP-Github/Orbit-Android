package com.yapp.fortune

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.fortune.component.FortuneTopAppBar
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.utils.heightForScreenPercentage

@Composable
fun FortuneRewardRoute(
    viewModel: FortuneViewModel = hiltViewModel(),
) {
    val analyticsHelper = LocalAnalyticsHelper.current
    val state = viewModel.container.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.value.fortuneImageId) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "fortune_talisman_view",
            ),
        )

        val imageId = state.value.fortuneImageId ?: viewModel.getRandomImage()
        viewModel.saveFortuneImageIdIfNeeded(imageId)
    }

    FortuneRewardScreen(
        state = state,
        onCloseClick = { viewModel.processAction(FortuneContract.Action.NavigateToHome) },
        onCompleteClick = { viewModel.processAction(FortuneContract.Action.NavigateToHome) },
        onSaveImage = {
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "fortune_talisman_save",
                ),
            )
            viewModel.processAction(FortuneContract.Action.SaveImage(it))
        },
    )
}

@Composable
fun FortuneRewardScreen(
    state: State<FortuneContract.State>,
    onCloseClick: () -> Unit,
    onCompleteClick: () -> Unit = {},
    onSaveImage: (Int) -> Unit,
) {
    val parts = state.value.dailyFortuneTitle.split(" ")
    val nickName = parts.getOrNull(0)?.trim() ?: ""

    val imageRes = state.value.fortuneImageId
        ?: core.designsystem.R.drawable.ic_fortune_reward1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        Image(
            painter = painterResource(id = core.designsystem.R.drawable.ic_fortune_reward_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FortuneTopAppBar(
                titleLabel = "행운 부적",
                onCloseClick = onCloseClick,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp)
                        .align(Alignment.Start),
                    text = "$nickName\n부적을 가지고 있으면\n행운이 찾아올거야",
                    style = OrbitTheme.typography.H1,
                    color = OrbitTheme.colors.white,
                )
                Spacer(modifier = Modifier.heightForScreenPercentage(0.0467f))
                Icon(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    painter = painterResource(id = core.designsystem.R.drawable.ic_shadow),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(4f / 1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OrbitButton(
                    label = "완료",
                    modifier = Modifier.weight(1f),
                    onClick = onCompleteClick,
                    enabled = true,
                    containerColor = OrbitTheme.colors.gray_600,
                    contentColor = OrbitTheme.colors.white,
                    pressedContainerColor = OrbitTheme.colors.gray_700,
                    pressedContentColor = OrbitTheme.colors.white,
                )
                OrbitButton(
                    label = "앨범에 저장",
                    modifier = Modifier.weight(1f),
                    onClick = { onSaveImage(imageRes) },
                    enabled = true,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFortuneRewardScreen() {
    val fakeState = remember {
        mutableStateOf(
            FortuneContract.State(
                dailyFortuneTitle = "오르비, 오늘은 기회가 많으니 적극적으로 움직여봐!",
                hasReward = true,
            ),
        )
    }

    FortuneRewardScreen(state = fakeState, onCloseClick = {}, onCompleteClick = {}, onSaveImage = {})
}
