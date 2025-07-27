package com.yapp.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.onboarding.component.UserInfoBottomSheet
import com.yapp.ui.component.dialog.OrbitDialog
import com.yapp.ui.toggle.OrbitGenderToggle
import com.yapp.ui.utils.heightForScreenPercentage
import com.yapp.ui.utils.paddingForScreenPercentage
import feature.onboarding.R

@Composable
fun OnboardingGenderRoute(
    viewModel: OnboardingViewModel,
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    val analyticsHelper = LocalAnalyticsHelper.current

    LaunchedEffect(Unit) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "onboarding_gender_view",
                properties = mapOf(
                    AnalyticsEvent.OnboardingPropertiesKeys.STEP to "성별",
                ),
            ),
        )
    }

    BackHandler {
        viewModel.processAction(OnboardingContract.Action.PreviousStep)
    }

    OnboardingGenderScreen(
        state = state,
        currentStep = 5,
        totalSteps = 6,
        onNextClick = { viewModel.processAction(OnboardingContract.Action.ToggleBottomSheet) },
        onBackClick = { viewModel.processAction(OnboardingContract.Action.PreviousStep) },
        onGenderSelect = { gender ->
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "onboarding_gender_select",
                    properties = mapOf(
                        AnalyticsEvent.OnboardingPropertiesKeys.GENDER to gender,
                    ),
                ),
            )
            viewModel.processAction(OnboardingContract.Action.UpdateGender(gender))
        },
        onDismissRequest = {
            viewModel.processAction(OnboardingContract.Action.ToggleBottomSheet)
        },
        onConfirmRequest = {
            viewModel.processAction(OnboardingContract.Action.ToggleBottomSheet)
            viewModel.processAction(OnboardingContract.Action.Submit)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingGenderScreen(
    state: OnboardingContract.State,
    currentStep: Int,
    totalSteps: Int,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onGenderSelect: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
) {
    OnboardingScreen(
        currentStep = currentStep,
        totalSteps = totalSteps,
        isButtonEnabled = state.selectedGender != null,
        onNextClick = onNextClick,
        onBackClick = onBackClick,
        buttonLabel = "다음",
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.heightForScreenPercentage(0.05f))
            Text(
                text = stringResource(id = R.string.onboarding_step6_text_title),
                style = OrbitTheme.typography.heading1SemiBold,
                color = OrbitTheme.colors.white,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 38.dp)
                    .paddingForScreenPercentage(topPercentage = 0.11f),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OrbitGenderToggle(
                        label = "남성",
                        isSelected = state.selectedGender == "남성",
                        onToggle = { onGenderSelect("남성") },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OrbitGenderToggle(
                        label = "여성",
                        isSelected = state.selectedGender == "여성",
                        onToggle = { onGenderSelect("여성") },
                    )
                }
            }
        }
    }

    if (state.isShowWarningDialog) {
        OrbitDialog(
            title = stringResource(id = R.string.onboarding_warning_dialog_title),
            message = stringResource(id = R.string.onboarding_warning_dialog_message),
            confirmText = stringResource(id = R.string.onboarding_warning_dialog_btn_confirm),
            onConfirm = {
                onConfirmRequest()
            },
        )
    }

    if (state.isBottomSheetOpen) {
        UserInfoBottomSheet(
            onDismissRequest = onDismissRequest,
            onConfirmRequest = onConfirmRequest,
            name = state.userName,
            gender = state.selectedGender ?: "무지개",
            birthDate = state.birthDateFormatted,
            birthTime = state.birthTimeFormatted,
        )
    }
}

@Composable
@Preview
fun OnboardingGenderScreenPreview() {
    OnboardingGenderScreen(
        state = OnboardingContract.State(
            isButtonEnabled = true,
        ),
        currentStep = 0,
        totalSteps = 0,
        onNextClick = {},
        onBackClick = {},
        onGenderSelect = {},
        onDismissRequest = {},
        onConfirmRequest = {},
    )
}
