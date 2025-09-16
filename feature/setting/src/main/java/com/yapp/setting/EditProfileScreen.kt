package com.yapp.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.setting.component.SettingTopAppBar
import com.yapp.ui.component.checkbox.OrbitCheckBox
import com.yapp.ui.component.dialog.OrbitDialog
import com.yapp.ui.component.textfield.OrbitTextField
import com.yapp.ui.component.textfield.WarningMessage
import com.yapp.ui.toggle.OrbitGenderToggle

@Composable
fun EditProfileRoute(
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.shouldFetchUserInfo) {
        if (state.shouldFetchUserInfo) {
            viewModel.processAction(SettingContract.Action.RefreshUserInfo)
        }
    }

    EditProfileScreen(
        state = state,
        onBack = { viewModel.processAction(SettingContract.Action.ShowDialog) },
        onUpdateName = { name -> viewModel.processAction(SettingContract.Action.UpdateName(name)) },
        onToggleGender = { isMale -> viewModel.processAction(SettingContract.Action.ToggleGender(isMale)) },
        onToggleTimeUnknown = { isChecked ->
            viewModel.processAction(
                SettingContract.Action.ToggleTimeUnknown(
                    isChecked,
                ),
            )
        },
        onUpdateTimeOfBirth = { time ->
            viewModel.processAction(
                SettingContract.Action.UpdateTimeOfBirth(
                    time,
                ),
            )
        },
        onNavigateToEditBirthday = { viewModel.processAction(SettingContract.Action.NavigateToEditBirthday) },
        onConfirmExit = {
            viewModel.processAction(SettingContract.Action.HideDialog)
            viewModel.processAction(SettingContract.Action.PreviousStep)
        },
        onCancelDialog = { viewModel.processAction(SettingContract.Action.HideDialog) },
        onSaveUserInfo = { viewModel.processAction(SettingContract.Action.SubmitUserInfo) },
    )
}

@Composable
fun EditProfileScreen(
    state: SettingContract.State,
    onBack: () -> Unit,
    onUpdateName: (String) -> Unit,
    onToggleGender: (Boolean) -> Unit,
    onToggleTimeUnknown: (Boolean) -> Unit,
    onUpdateTimeOfBirth: (String) -> Unit,
    onNavigateToEditBirthday: () -> Unit,
    onConfirmExit: () -> Unit,
    onCancelDialog: () -> Unit,
    onSaveUserInfo: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    val nameTextFieldValue = remember { mutableStateOf(TextFieldValue(state.name)) }
    val birthTimeTextFieldValue = remember { mutableStateOf(TextFieldValue(state.timeOfBirth)) }

    LaunchedEffect(state.name) {
        if (state.name != nameTextFieldValue.value.text) {
            nameTextFieldValue.value = TextFieldValue(state.name)
        }
    }

    LaunchedEffect(state.timeOfBirth) {
        if (state.timeOfBirth != birthTimeTextFieldValue.value.text) {
            birthTimeTextFieldValue.value = TextFieldValue(state.timeOfBirth)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() },
            ),
    ) {
        SettingTopAppBar(
            onBackClick = onBack,
            showTopAppBarActions = true,
            title = "프로필 수정",
            actionTitle = "저장",
            onActionClick = onSaveUserInfo,
            isActionEnabled = state.isActionEnabled,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(rememberScrollState()),
        ) {
            ContentsTitle(
                contentsTitle = "이름",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OrbitTextField(
                text = nameTextFieldValue.value,
                onTextChange = { newValue ->
                    nameTextFieldValue.value = newValue
                    onUpdateName(newValue.text)
                },
                hint = "이름 입력",
                isValid = state.isNameValid,
                showWarning = !state.isNameValid,
                warningMessage = "입력한 내용을 확인해 주세요.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                textAlign = TextAlign.Start,
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
            )
            Spacer(modifier = Modifier.height(18.dp))
            ContentsTitle(
                contentsTitle = "생년월일",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            BirthCard(
                birthDate = state.birthDateFormatted,
                onNavigateToEditBirthday = onNavigateToEditBirthday,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            ContentsTitle(
                contentsTitle = "성별",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OrbitGenderToggle(
                        label = "남성",
                        isSelected = state.isMaleSelected,
                        onToggle = { onToggleGender(true) },
                        height = 52.dp,
                        textStyle = OrbitTheme.typography.body1Regular,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OrbitGenderToggle(
                        label = "여성",
                        isSelected = state.isFemaleSelected,
                        onToggle = { onToggleGender(false) },
                        height = 52.dp,
                        textStyle = OrbitTheme.typography.body1Regular,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            ContentsTitle(
                contentsTitle = "태어난 시간",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OrbitTextField(
                        text = birthTimeTextFieldValue.value,
                        onTextChange = { newValue ->
                            val formattedValue = formatTimeInput(newValue.text, state.timeOfBirth)
                            birthTimeTextFieldValue.value = formattedValue
                            onUpdateTimeOfBirth(formattedValue.text)
                        },
                        hint = "23:59",
                        isValid = state.isTimeValid,
                        showWarning = !state.isTimeValid,
                        enabled = !state.isTimeUnknown,
                        modifier = Modifier
                            .weight(1f),
                        textAlign = TextAlign.Start,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            },
                        ),
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    OrbitCheckBox(
                        checked = state.isTimeUnknown,
                        onCheckedChange = {
                            onToggleTimeUnknown(!state.isTimeUnknown)
                        },
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "시간모름",
                        style = OrbitTheme.typography.body1Medium,
                        color = if (state.isTimeUnknown) OrbitTheme.colors.main else OrbitTheme.colors.white,
                    )
                }
                if (!state.isTimeUnknown && !state.isTimeValid) {
                    WarningMessage(
                        message = "올바른 시간을 입력해주세요.",
                        textAlign = TextAlign.Start,
                    )
                }
            }
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

fun formatTimeInput(input: String, previousText: String): TextFieldValue {
    val sanitizedValue = input.filter { it.isDigit() }
    val isDeleting = sanitizedValue.length < previousText.filter { it.isDigit() }.length

    val newText = when {
        isDeleting && previousText.endsWith(":") -> sanitizedValue
        sanitizedValue.length > 2 -> {
            val hours = sanitizedValue.take(2)
            val minutes = sanitizedValue.drop(2).take(2)
            "$hours:$minutes"
        }

        sanitizedValue.length == 2 -> {
            if (previousText.length == 3 && previousText.endsWith(":")) {
                sanitizedValue
            } else {
                "$sanitizedValue:"
            }
        }

        else -> sanitizedValue
    }
    val cursorPosition = if (newText.length == 3 && newText.endsWith(":")) {
        3
    } else {
        newText.length
    }

    return TextFieldValue(newText, TextRange(cursorPosition))
}

@Composable
private fun ContentsTitle(
    contentsTitle: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = contentsTitle,
        modifier = modifier.fillMaxWidth(),
        style = OrbitTheme.typography.body1Medium,
        color = OrbitTheme.colors.white,
    )
}

@Composable
private fun BirthCard(
    modifier: Modifier = Modifier,
    birthDate: String,
    onNavigateToEditBirthday: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(
                color = OrbitTheme.colors.gray_800,
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = OrbitTheme.colors.gray_700,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onNavigateToEditBirthday() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = birthDate,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = OrbitTheme.typography.body1Regular,
            color = OrbitTheme.colors.gray_50,
        )
    }
}

@Composable
@Preview
fun EditProfileScreenPreview() {
    EditProfileScreen(
        state = SettingContract.State(),
        onBack = {},
        onToggleGender = {},
        onToggleTimeUnknown = {},
        onUpdateTimeOfBirth = {},
        onUpdateName = {},
        onNavigateToEditBirthday = {},
        onConfirmExit = {},
        onCancelDialog = {},
        onSaveUserInfo = {},
    )
}
