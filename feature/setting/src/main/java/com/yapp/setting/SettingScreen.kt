package com.yapp.setting

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.setting.component.InquiryCard
import com.yapp.setting.component.SettingItem
import com.yapp.setting.component.SettingTopAppBar
import com.yapp.setting.component.TableOfContentsText
import com.yapp.setting.component.UserInfoCard
import com.yapp.setting.component.VersionCodeText
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.extensions.customClickable

@Composable
fun SettingRoute(
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.onAction(SettingContract.Action.RefreshUserInfo)
    }
    SettingScreen(
        state = state,
        onNavigateToEditProfile = {
            viewModel.onAction(
                SettingContract.Action.NavigateToEditProfile,
            )
        },
        onBackClick = { viewModel.onAction(SettingContract.Action.PreviousStep) },
        onInquiryClick = {
            val kakaoUrl = "http://pf.kakao.com/_ykqxjn"
            val kakaoSchemeUrl = "kakaoplus://plusfriend/home/_ykqxjn"

            val kakaoIntent = Intent(Intent.ACTION_VIEW, kakaoSchemeUrl.toUri())

            try {
                context.startActivity(kakaoIntent) // 카카오톡 앱으로 이동
            } catch (e: Exception) {
                viewModel.onAction(
                    SettingContract.Action.OpenWebView(kakaoUrl), // 앱이 없으면 웹뷰로 열기
                )
            }
        },
        onTermsClick = {
            viewModel.onAction(
                SettingContract.Action.OpenWebView("https://www.orbitalarm.net/terms.html"),
            )
        },
        onPrivacyPolicyClick = {
            viewModel.onAction(
                SettingContract.Action.OpenWebView("https://www.orbitalarm.net/privacy.html"),
            )
        },
    )
}

@Composable
fun SettingScreen(
    state: SettingContract.State,
    onNavigateToEditProfile: () -> Unit,
    onBackClick: () -> Unit = {},
    onInquiryClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
) {
    if (state.initialLoading) {
        SettingLoadingScreen()
    } else {
        SettingContent(
            name = state.name,
            selectedGender = state.selectedGender ?: "",
            birthDate = state.birthDateFormatted,
            timeOfBirth = state.timeOfBirthFormatted,
            onNavigateToEditProfile = onNavigateToEditProfile,
            onBackClick = onBackClick,
            onInquiryClick = onInquiryClick,
            onTermsClick = onTermsClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
        )
    }
}

@Composable
private fun SettingContent(
    name: String,
    selectedGender: String,
    birthDate: String,
    timeOfBirth: String,
    onNavigateToEditProfile: () -> Unit,
    onBackClick: () -> Unit,
    onInquiryClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        SettingTopAppBar(
            onBackClick = onBackClick,
            showTopAppBarActions = true,
            title = "설정",
        )
        Spacer(modifier = Modifier.height(12.dp))
        UserInfoCard(
            name = name,
            gender = selectedGender,
            birth = birthDate,
            timeOfBirth = timeOfBirth,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .customClickable(
                    rippleEnabled = true,
                    fadeOnPress = true,
                    pressedAlpha = 0.5f,
                    onClick = { onNavigateToEditProfile() },
                ),
        )
        Spacer(modifier = Modifier.height(24.dp))
        InquiryCard(
            modifier = Modifier
                .padding(horizontal = 24.dp),
            onInquiryClick = onInquiryClick,
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            thickness = 8.dp,
            color = OrbitTheme.colors.gray_800,
        )
        Spacer(modifier = Modifier.height(24.dp))
        TableOfContentsText(
            contentsTitle = "서비스 약관",
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingItem(
            itemTitle = "이용약관",
            modifier = Modifier
                .customClickable(
                    rippleEnabled = true,
                    fadeOnPress = true,
                    pressedAlpha = 0.5f,
                    onClick = onTermsClick,
                )
                .padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingItem(
            itemTitle = "개인정보 처리방침",
            modifier = Modifier
                .customClickable(
                    rippleEnabled = true,
                    fadeOnPress = true,
                    pressedAlpha = 0.5f,
                    onClick = onPrivacyPolicyClick,
                )
                .padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        VersionCodeText()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900),
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
@Preview
fun SettingScreenPreview() {
    SettingScreen(
        state = SettingContract.State(),
        onNavigateToEditProfile = {},
    )
}
