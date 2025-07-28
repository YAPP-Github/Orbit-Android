package com.yapp.onboarding.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.utils.paddingForScreenPercentage
import com.yapp.ui.utils.widthForScreenPercentage
import feature.onboarding.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoBottomSheet(
    name: String,
    gender: String,
    birthDate: String,
    birthTime: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .paddingForScreenPercentage(allPercentage = 0.03f),
    ) {
        Text(
            text = stringResource(R.string.onboarding_step6_bs_title),
            modifier = Modifier
                .paddingForScreenPercentage(
                    topPercentage = 0.005f,
                    bottomPercentage = 0.027f,
                ),
            style = OrbitTheme.typography.heading2SemiBold,
            color = OrbitTheme.colors.white,
        )
        UserInfoRow(label = stringResource(R.string.onboarding_step6_bs_name), value = name)
        UserInfoRow(
            label = stringResource(R.string.onboarding_step6_bs_gender),
            value = gender,
        )
        UserInfoRow(
            label = stringResource(R.string.onboarding_step6_bs_birth),
            value = birthDate,
        )
        UserInfoRow(
            label = stringResource(R.string.onboarding_step6_bs_time),
            value = birthTime,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .paddingForScreenPercentage(topPercentage = 0.032f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrbitButton(
                label = stringResource(R.string.onboarding_step6_bs_btn_dismiss),
                modifier = Modifier.weight(1f),
                onClick = onDismiss,
                enabled = true,
                containerColor = OrbitTheme.colors.gray_600,
                contentColor = OrbitTheme.colors.white,
                pressedContainerColor = OrbitTheme.colors.gray_500,
                pressedContentColor = OrbitTheme.colors.white.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.widthForScreenPercentage(0.032f))
            OrbitButton(
                label = stringResource(R.string.onboarding_step6_bs_btn_confirm),
                modifier = Modifier.weight(1f),
                onClick = onConfirm,
                enabled = true,
                pressedContainerColor = OrbitTheme.colors.main.copy(alpha = 0.8f),
                pressedContentColor = OrbitTheme.colors.gray_600,
                shape = RoundedCornerShape(12.dp),

            )
        }
    }
}

@Composable
fun UserInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .paddingForScreenPercentage(verticalPercentage = 0.0148f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = OrbitTheme.typography.body1Regular,
            color = OrbitTheme.colors.gray_50,
        )
        Text(
            text = value,
            style = OrbitTheme.typography.body1SemiBold,
            color = OrbitTheme.colors.white,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun UserInfoBottomSheetPreview() {
    UserInfoBottomSheet(
        name = "홍길동",
        gender = "남성",
        birthDate = "1990년 1월 1일",
        birthTime = "12:00",
        onDismiss = { },
        onConfirm = { },
    )
}
