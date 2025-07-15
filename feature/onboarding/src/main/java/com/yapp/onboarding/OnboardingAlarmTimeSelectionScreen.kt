package com.yapp.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.ui.component.timepicker.OrbitPicker
import com.yapp.ui.utils.heightForScreenPercentage
import feature.onboarding.R
import java.time.LocalTime

@Composable
fun OnboardingAlarmTimeSelectionRoute(
    viewModel: OnboardingViewModel,
) {
    val analyticsHelper = LocalAnalyticsHelper.current

    BackHandler {
        viewModel.processAction(OnboardingContract.Action.PreviousStep)
    }

    LaunchedEffect(Unit) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "onboarding_alarm_view",
                properties = mapOf(
                    AnalyticsEvent.OnboardingPropertiesKeys.STEP to "초기 알람 생성",
                ),
            ),
        )
    }

    OnboardingAlarmTimeSelectionScreen(
        currentStep = 1,
        totalSteps = 6,
        onNextClick = {
            viewModel.processAction(OnboardingContract.Action.NextStep)
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "onboarding_alarm_create",
                    properties = mapOf(
                        AnalyticsEvent.OnboardingPropertiesKeys.STEP to "초기 알람 생성",
                    ),
                ),
            )
        },
        onBackClick = { viewModel.processAction(OnboardingContract.Action.PreviousStep) },
        setAlarmTime = { newTime ->
            viewModel.processAction(OnboardingContract.Action.SetAlarmTime(newTime))
        },
    )
}

@Composable
fun OnboardingAlarmTimeSelectionScreen(
    currentStep: Int,
    totalSteps: Int,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    setAlarmTime: (LocalTime) -> Unit,
) {
    OnboardingScreen(
        currentStep = currentStep,
        totalSteps = totalSteps,
        isButtonEnabled = true,
        onNextClick = onNextClick,
        onBackClick = onBackClick,
        buttonLabel = "만들기",
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.heightForScreenPercentage(0.05f))
            Text(
                text = stringResource(id = R.string.onboarding_step2_text_title),
                style = OrbitTheme.typography.heading1SemiBold,
                color = OrbitTheme.colors.white,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(id = R.string.onboarding_step2_text_subtitle),
                style = OrbitTheme.typography.body1Regular,
                color = OrbitTheme.colors.gray_100,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center,
            )

            OrbitPicker(
                modifier = Modifier.padding(top = 90.dp),
            ) { newTime ->
                setAlarmTime(newTime)
            }
        }
    }
}

@Composable
@Preview
fun OnboardingAlarmTimeSelectionScreenPreview() {
    OrbitTheme {
        OnboardingAlarmTimeSelectionScreen(
            currentStep = 0,
            totalSteps = 0,
            onNextClick = {},
            onBackClick = {},
            setAlarmTime = { _ -> },
        )
    }
}
