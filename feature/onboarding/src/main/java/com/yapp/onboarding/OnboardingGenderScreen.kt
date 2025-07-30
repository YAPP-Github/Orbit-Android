package com.yapp.onboarding

import android.net.Uri
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
import androidx.navigation.navOptions
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.common.navigation.OrbitNavigator
import com.yapp.common.navigation.route.OnboardingBaseRoute
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.onboarding.component.UserInfoBottomSheet
import com.yapp.ui.component.bottomsheet.OrbitBottomSheetLayout
import com.yapp.ui.component.bottomsheet.OrbitBottomSheetState
import com.yapp.ui.component.bottomsheet.rememberOrbitBottomSheetState
import com.yapp.ui.component.dialog.OrbitDialog
import com.yapp.ui.toggle.OrbitGenderToggle
import com.yapp.ui.utils.heightForScreenPercentage
import com.yapp.ui.utils.paddingForScreenPercentage
import feature.onboarding.R
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun OnboardingGenderRoute(
    navigator: OrbitNavigator,
    bottomSheetState: OrbitBottomSheetState,
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

    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(
            sideEffect = sideEffect,
            navigator = navigator,
            bottomSheetState = bottomSheetState,
            state = state,
            processAction = viewModel::processAction,
        )
    }

    OnboardingGenderScreen(
        state = state,
        bottomSheetState = bottomSheetState,
        currentStep = 5,
        totalSteps = 6,
        processAction = viewModel::processAction,
    )
}

private suspend fun handleSideEffect(
    sideEffect: OnboardingContract.SideEffect,
    navigator: OrbitNavigator,
    bottomSheetState: OrbitBottomSheetState,
    state: OnboardingContract.State,
    processAction: (OnboardingContract.Action) -> Unit,
) {
    when (sideEffect) {
        is OnboardingContract.SideEffect.NavigateToNextStep -> {
            navigator.navigateToOnboardingNextStep(sideEffect.currentStep)
        }

        OnboardingContract.SideEffect.NavigateBack -> {
            processAction(OnboardingContract.Action.Reset)
            navigator.navigateBack()
        }

        is OnboardingContract.SideEffect.ShowBottomSheet -> {
            bottomSheetState.show {
                UserInfoBottomSheet(
                    name = state.userName,
                    gender = state.selectedGender ?: "무지개",
                    birthDate = state.birthDateFormatted,
                    birthTime = state.birthTimeFormatted,
                    onDismiss = {
                        processAction(OnboardingContract.Action.HideBottomSheet)
                    },
                    onConfirm = {
                        processAction(OnboardingContract.Action.HideBottomSheet)
                        processAction(OnboardingContract.Action.Submit)
                    },
                )
            }
        }

        is OnboardingContract.SideEffect.HideBottomSheet -> {
            bottomSheetState.hide()
        }

        OnboardingContract.SideEffect.OnboardingCompleted -> {
            navigator.navigateToHome(
                navOptions = navOptions {
                    popUpTo<OnboardingBaseRoute> {
                        inclusive = true
                    }
                },
            )
        }

        is OnboardingContract.SideEffect.OpenWebView -> {
            navigator.navigateToWebView(Uri.encode(sideEffect.url))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingGenderScreen(
    state: OnboardingContract.State,
    bottomSheetState: OrbitBottomSheetState,
    currentStep: Int,
    totalSteps: Int,
    processAction: (OnboardingContract.Action) -> Unit,
    logEvent: (AnalyticsEvent) -> Unit = { },
) {
    BackHandler {
        if (state.isShowWarningDialog) {
            processAction(OnboardingContract.Action.HideWarningDialog)
        } else if (bottomSheetState.state.isVisible) {
            processAction(OnboardingContract.Action.HideBottomSheet)
        } else {
            processAction(OnboardingContract.Action.PreviousStep)
        }
    }

    OrbitBottomSheetLayout(sheetState = bottomSheetState) {
        OnboardingScreen(
            currentStep = currentStep,
            totalSteps = totalSteps,
            isButtonEnabled = state.selectedGender != null,
            onNextClick = {
                processAction(OnboardingContract.Action.ShowBottomSheet)
            },
            onBackClick = {
                processAction(OnboardingContract.Action.PreviousStep)
            },
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
                    listOf("남성", "여성").forEach { gender ->
                        Box(modifier = Modifier.weight(1f)) {
                            OrbitGenderToggle(
                                label = gender,
                                isSelected = state.selectedGender == gender,
                                onToggle = {
                                    logEvent(
                                        AnalyticsEvent(
                                            type = "onboarding_gender_select",
                                            properties = mapOf(
                                                AnalyticsEvent.OnboardingPropertiesKeys.GENDER to gender,
                                            ),
                                        ),
                                    )
                                    processAction(OnboardingContract.Action.UpdateGender(gender))
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.isShowWarningDialog) {
        OrbitDialog(
            title = stringResource(id = R.string.onboarding_warning_dialog_title),
            message = stringResource(id = R.string.onboarding_warning_dialog_message),
            confirmText = stringResource(id = R.string.onboarding_warning_dialog_btn_confirm),
            onConfirm = { processAction(OnboardingContract.Action.HideWarningDialog) },
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
        bottomSheetState = rememberOrbitBottomSheetState(),
        currentStep = 0,
        totalSteps = 0,
        processAction = {},
    )
}
