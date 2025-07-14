package com.yapp.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.setting.component.SettingTopAppBar
import com.yapp.ui.component.dialog.OrbitDialog
import com.yapp.ui.component.timepicker.OrbitYearMonthPicker
import com.yapp.ui.utils.heightForScreenPercentage

@Composable
fun EditBirthdayRoute(
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    EditBirthdayScreen(
        state = state,
        onBack = { viewModel.processAction(SettingContract.Action.PreviousStep) },
        onConfirmExit = {
            viewModel.processAction(SettingContract.Action.HideDialog)
            viewModel.processAction(SettingContract.Action.PreviousStep)
        },
        onCancelDialog = { viewModel.processAction(SettingContract.Action.HideDialog) },
        onUpdateBirthDate = { lunar, year, month, day ->
            viewModel.processAction(
                SettingContract.Action.UpdateBirthDate(
                    lunar,
                    year,
                    month,
                    day,
                ),
            )
        },
        onConfirm = { viewModel.processAction(SettingContract.Action.ConfirmAndNavigateBack) },
    )
}

@Composable
fun EditBirthdayScreen(
    state: SettingContract.State,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onConfirmExit: () -> Unit,
    onCancelDialog: () -> Unit,
    onUpdateBirthDate: (String, Int, Int, Int) -> Unit,
) {
    var selectedLunar by remember { mutableStateOf(state.birthType) }
    var selectedYear by remember { mutableStateOf(state.birthDate.split("-")[0].toInt()) }
    var selectedMonth by remember { mutableStateOf(state.birthDate.split("-")[1].toInt()) }
    var selectedDay by remember { mutableStateOf(state.birthDate.split("-")[2].toInt()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SettingTopAppBar(
            onBackClick = onBack,
            showTopAppBarActions = true,
            title = "생년월일 수정",
            actionTitle = "확인",
            isActionEnabled = true,
            onActionClick = {
                onUpdateBirthDate(selectedLunar, selectedYear, selectedMonth, selectedDay)
                onConfirm()
            },
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "생년월일을 알려주세요",
            style = OrbitTheme.typography.title3SemiBold,
            color = OrbitTheme.colors.white,
        )
        Spacer(modifier = Modifier.heightForScreenPercentage(0.16f))

        OrbitYearMonthPicker(
            initialLunar = selectedLunar,
            initialYear = selectedYear.toString(),
            initialMonth = selectedMonth.toString(),
            initialDay = selectedDay.toString(),
        ) { lunar, year, month, day ->
            selectedLunar = lunar
            selectedYear = year
            selectedMonth = month
            selectedDay = day
        }
    }

    if (state.isDialogVisible) {
        OrbitDialog(
            title = "변경 사항 삭제",
            message = "변경 사항을 저장하지 않고\n나가시겠어요?",
            confirmText = "나가기",
            cancelText = "취소",
            onConfirm = onConfirmExit,
            onCancel = onCancelDialog,
        )
    }
}

@Composable
@Preview
fun PreviewEditBirthdayScreen() {
    EditBirthdayScreen(
        state = SettingContract.State(),
        onBack = {},
        onConfirm = {},
        onConfirmExit = {},
        onCancelDialog = {},
        onUpdateBirthDate = { _, _, _, _ -> },
    )
}
