package com.yapp.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.onboarding.component.OnBoardingTopAppBar
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.timepicker.OrbitYearMonthPicker
import com.yapp.ui.utils.heightForScreenPercentage
import feature.onboarding.R

@Composable
fun OnboardingBirthdayRoute(viewModel: OnboardingViewModel) {
    val analyticsHelper = LocalAnalyticsHelper.current

    BackHandler {
        viewModel.processAction(OnboardingContract.Action.PreviousStep)
    }

    LaunchedEffect(Unit) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "onboarding_birthdate_view",
                properties = mapOf(
                    AnalyticsEvent.OnboardingPropertiesKeys.STEP to "생년월일",
                ),
            ),
        )
    }

    OnboardingBirthdayScreen(
        currentStep = 2,
        totalSteps = 6,
        onNextClick = {
            viewModel.processAction(OnboardingContract.Action.NextStep)
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "onboarding_birthdate_next",
                    properties = mapOf(
                        AnalyticsEvent.OnboardingPropertiesKeys.STEP to "생년월일",
                    ),
                ),
            )
        },
        onBackClick = { viewModel.processAction(OnboardingContract.Action.PreviousStep) },
        onBirthDateChange = { lunar, year, month, day ->
            viewModel.processAction(OnboardingContract.Action.UpdateBirthDate(lunar, year, month, day))
        },
        onTermsClick = {
            viewModel.processAction(
                OnboardingContract.Action.OpenWebView("https://www.orbitalarm.net/terms.html"),
            )
        },
        onPrivacyPolicyClick = {
            viewModel.processAction(
                OnboardingContract.Action.OpenWebView("https://www.orbitalarm.net/privacy.html"),
            )
        },
    )
}

@Composable
fun OnboardingBirthdayScreen(
    currentStep: Int,
    totalSteps: Int,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onBirthDateChange: (String, Int, Int, Int) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
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
            showTopAppBarActions = true,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            Spacer(modifier = Modifier.heightForScreenPercentage(0.05f))
            Text(
                text = stringResource(id = R.string.onboarding_step3_text_title),
                style = OrbitTheme.typography.heading1SemiBold,
                color = OrbitTheme.colors.white,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            OrbitYearMonthPicker(
                modifier = Modifier.padding(top = 60.dp),
                onValueChange = onBirthDateChange,
            )
            Spacer(modifier = Modifier.weight(1f))
            OrbitButton(
                label = "다음",
                onClick = onNextClick,
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )
            AnnotatedTermsText(
                onTermsClick = onTermsClick,
                onPrivacyPolicyClick = onPrivacyPolicyClick,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun AnnotatedTermsText(
    modifier: Modifier = Modifier,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    val annotatedText = buildAnnotatedString {
        append("서비스 시작 시 ")

        pushStringAnnotation(tag = "TERMS", annotation = "TERMS")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("이용약관")
        }
        pop()

        append(" 및 ")

        pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("개인정보처리방침")
        }
        pop()

        append("에 동의하게 됩니다.")
    }

    var layoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedText,
        style = OrbitTheme.typography.label2SemiBold.copy(textAlign = TextAlign.Center),
        color = OrbitTheme.colors.gray_500,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    layoutResultState?.let { layoutResult ->
                        val offset = layoutResult.getOffsetForPosition(tapOffset)
                        val annotation = annotatedText.getStringAnnotations(start = offset, end = offset).firstOrNull()

                        when (annotation?.tag) {
                            "TERMS" -> onTermsClick()
                            "PRIVACY" -> onPrivacyPolicyClick()
                        }
                    }
                }
            },
        onTextLayout = { layoutResult ->
            layoutResultState = layoutResult
        },
    )
}

@Composable
@Preview
fun OnboardingBirthdayScreenPreview() {
    OrbitTheme {
        OnboardingBirthdayScreen(
            currentStep = 3,
            totalSteps = 3,
            onNextClick = {},
            onBackClick = {},
            onBirthDateChange = { _, _, _, _ -> },
            onTermsClick = {},
            onPrivacyPolicyClick = {},
        )
    }
}
