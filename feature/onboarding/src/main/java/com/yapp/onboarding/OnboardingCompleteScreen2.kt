package com.yapp.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.onboarding.component.OnBoardingTopAppBar
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.utils.heightForScreenPercentage
import feature.onboarding.R

@Composable
fun OnboardingCompleteRoute2(
    viewModel: OnboardingViewModel,
) {
    BackHandler {
        viewModel.processAction(OnboardingContract.Action.PreviousStep)
    }

    OnboardingCompleteScreen2(
        onNextClick = {
            viewModel.processAction(OnboardingContract.Action.CompleteOnboarding)
            viewModel.processAction(OnboardingContract.Action.CreateAlarm)
        },
        onBackClick = { viewModel.processAction(OnboardingContract.Action.PreviousStep) },
    )
}

@Composable
fun OnboardingCompleteScreen2(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    currentStep: Int = 0,
    totalSteps: Int = 0,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900)
            .imePadding(),
    ) {
        OnBoardingTopAppBar(
            currentStep = currentStep,
            totalSteps = totalSteps,
            onBackClick = onBackClick,
            showTopAppBarActions = false,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            Spacer(modifier = Modifier.heightForScreenPercentage(0.05f))
            Text(
                text = stringResource(id = R.string.onboarding_completed_step1_subtitle),
                style = OrbitTheme.typography.body2Regular,
                color = OrbitTheme.colors.main,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.onboarding_completed_step2_title),
                style = OrbitTheme.typography.heading1SemiBold,
                color = OrbitTheme.colors.white,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                LottieAnimation(
                    modifier = Modifier
                        .scale(1.2f)
                        .offset(y = (-70).dp),
                    resId = core.designsystem.R.raw.step3,
                )
                OrbitButton(
                    label = "시작하기",
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                        .align(Alignment.BottomCenter),
                    onClick = onNextClick,
                    enabled = true,
                )
            }
        }
    }
}

@Composable
@Preview
fun OnboardingCompleteScreen2Preview() {
    OrbitTheme {
        OnboardingCompleteScreen2(
            onNextClick = {},
            onBackClick = {},
        )
    }
}
