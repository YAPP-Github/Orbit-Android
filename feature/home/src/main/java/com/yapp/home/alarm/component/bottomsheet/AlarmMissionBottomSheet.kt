package com.yapp.home.alarm.component.bottomsheet

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.MissionType
import com.yapp.home.alarm.addedit.AlarmAddEditContract
import com.yapp.home.alarm.component.SelectorItems
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.extensions.customClickable
import core.designsystem.R

enum class AlarmMissionSelectBottomSheetType {
    MISSION_SETTING,
    MISSION_SELECT,
    MISSION_DETAIL,
}

private val countOptions = listOf(5, 10, 15, 20, 30)

private fun MissionType.displayData(): Pair<Int, Int> = when (this) {
    MissionType.SHAKE -> Pair(R.drawable.ic_mission_shake, feature.home.R.string.alarm_add_edit_selected_mission_shake)
    MissionType.TAP -> Pair(R.drawable.ic_mission_tap, feature.home.R.string.alarm_add_edit_selected_mission_tap)
    else -> throw IllegalStateException("Invalid mission type")
}

val StepStackSaver: Saver<MutableState<List<AlarmMissionSelectBottomSheetType>>, out Any> =
    listSaver(
        save = { state -> state.value.map { it.name } },
        restore = { restored -> mutableStateOf(restored.map { AlarmMissionSelectBottomSheetType.valueOf(it) }) },
    )

@Composable
internal fun AlarmMissionBottomSheet(
    missionState: AlarmAddEditContract.AlarmMissionState,
    onDismiss: () -> Unit,
    onSaveMission: (MissionType, Int) -> Unit,
    onPreviewMission: (MissionType, Int) -> Unit,
) {
    var stepStack by rememberSaveable(saver = StepStackSaver) {
        mutableStateOf(listOf(AlarmMissionSelectBottomSheetType.MISSION_SETTING))
    }
    var selectedMissionType by remember { mutableStateOf(missionState.missionType) }
    var selectedMissionCount by remember { mutableIntStateOf(missionState.missionCount) }

    fun push(step: AlarmMissionSelectBottomSheetType) {
        stepStack = stepStack + step
    }

    fun pop() {
        if (stepStack.size > 1) {
            stepStack = stepStack.dropLast(1)
        }
    }

    val currentStep = stepStack.last()
    Log.d("AlarmMissionBottomSheet", "Current Step: $currentStep, Stack: $stepStack")

    when (currentStep) {
        AlarmMissionSelectBottomSheetType.MISSION_SETTING -> {
            if (selectedMissionType == MissionType.NONE) {
                MissionAddContent {
                    push(AlarmMissionSelectBottomSheetType.MISSION_SELECT)
                }
            } else {
                MissionSettingContent(
                    missionType = selectedMissionType,
                    missionCount = selectedMissionCount,
                    onDetail = { push(AlarmMissionSelectBottomSheetType.MISSION_DETAIL) },
                    onDelete = {
                        selectedMissionType = MissionType.NONE
                        onSaveMission(selectedMissionType, selectedMissionCount)
                    },
                    onChange = { push(AlarmMissionSelectBottomSheetType.MISSION_SELECT) },
                    onDone = {
                        onSaveMission(selectedMissionType, selectedMissionCount)
                        onDismiss()
                    },
                )
            }
        }

        AlarmMissionSelectBottomSheetType.MISSION_SELECT -> {
            MissionSelectContent(
                onBack = { pop() },
                onClose = {
                    onDismiss()
                },
                onSelect = { mission ->
                    selectedMissionType = mission
                    push(AlarmMissionSelectBottomSheetType.MISSION_DETAIL)
                },
            )
        }

        AlarmMissionSelectBottomSheetType.MISSION_DETAIL -> {
            MissionDetailContent(
                missionType = selectedMissionType,
                selectedMissionCount = selectedMissionCount,
                onCountChange = { selectedMissionCount = it },
                onBack = { pop() },
                onClose = {
                    onDismiss()
                },
                onSave = {
                    onSaveMission(selectedMissionType, selectedMissionCount)
                    onDismiss()
                },
                onPreview = {
                    onPreviewMission(selectedMissionType, selectedMissionCount)
                },
            )
        }
    }
}

@Composable
private fun MissionAddContent(
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(26.dp))

        Text(
            modifier = Modifier.align(Alignment.Start),
            text = stringResource(id = feature.home.R.string.mission_bottom_sheet_title),
            style = OrbitTheme.typography.heading2SemiBold,
            color = OrbitTheme.colors.white,
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = feature.home.R.string.mission_add_content_empty_title),
                    style = OrbitTheme.typography.body1Bold,
                    color = OrbitTheme.colors.white,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = feature.home.R.string.mission_add_content_empty_description),
                    style = OrbitTheme.typography.label2Regular,
                    color = OrbitTheme.colors.white.copy(alpha = 0.8f),
                )

                Spacer(modifier = Modifier.height(32.dp))

                AddMissionButton { onNext() }
            }
        }
    }
}

@Composable
private fun AddMissionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = OrbitTheme.colors.white,
            contentColor = OrbitTheme.colors.gray_900,
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 12.dp,
        ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            tint = Color.Unspecified,
            contentDescription = "Add Mission",
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = stringResource(id = feature.home.R.string.mission_add_content_btn_add),
            style = OrbitTheme.typography.body1SemiBold,
        )
    }
}

@Composable
private fun MissionSettingContent(
    missionType: MissionType,
    missionCount: Int,
    onDetail: () -> Unit,
    onDelete: () -> Unit,
    onChange: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(26.dp))

        Text(
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.Start),
            text = stringResource(id = feature.home.R.string.mission_bottom_sheet_title),
            style = OrbitTheme.typography.heading2SemiBold,
            color = OrbitTheme.colors.white,
        )

        Spacer(modifier = Modifier.height(14.dp))

        SelectedMissionTypeItem(
            missionType = missionType,
            missionCount = missionCount,
            onDetail = onDetail,
            onDelete = onDelete,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OrbitButton(
                label = stringResource(id = feature.home.R.string.mission_setting_content_btn_change),
                onClick = onChange,
                enabled = true,
                containerColor = OrbitTheme.colors.gray_600,
                contentColor = OrbitTheme.colors.white,
                pressedContainerColor = OrbitTheme.colors.gray_500,
                pressedContentColor = OrbitTheme.colors.white.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f),
            )

            OrbitButton(
                label = stringResource(id = feature.home.R.string.mission_setting_content_btn_done),
                onClick = onDone,
                enabled = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SelectedMissionTypeItem(
    missionType: MissionType,
    missionCount: Int,
    onDetail: () -> Unit,
    onDelete: () -> Unit,
) {
    val (iconRes, titleRes) = missionType.displayData()
    val title = stringResource(id = titleRes)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    onClick = onDetail,
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = OrbitTheme.typography.headline2SemiBold,
                color = OrbitTheme.colors.white,
            )

            Spacer(modifier = Modifier.width(8.dp))

            MissionCountChip(count = missionCount)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    onClick = onDelete,
                )
                .padding(12.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete",
                modifier = Modifier.size(20.dp),
                tint = OrbitTheme.colors.gray_400,
            )
        }
    }
}

@Composable
private fun MissionCountChip(
    count: Int,
) {
    Row(
        modifier = Modifier
            .background(
                color = OrbitTheme.colors.main.copy(alpha = 0.1f),
                shape = CircleShape,
            )
            .padding(start = 5.dp, end = 3.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = feature.home.R.string.mission_count_chip_format, count),
            style = OrbitTheme.typography.label2Regular,
            color = OrbitTheme.colors.main.copy(alpha = 0.9f),
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "Close",
            modifier = Modifier.size(12.dp),
            tint = OrbitTheme.colors.main.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun MissionSelectContent(
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSelect: (MissionType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        MissionSelectTopAppBar(
            title = stringResource(id = feature.home.R.string.mission_bottom_sheet_title),
            onBack = onBack,
            onClose = onClose,
        )

        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            MissionTypeItem(
                missionType = MissionType.SHAKE,
                onClick = {
                    onSelect(MissionType.SHAKE)
                },
            )
            MissionTypeItem(
                missionType = MissionType.TAP,
                onClick = {
                    onSelect(MissionType.TAP)
                },
            )
        }
    }
}

@Composable
private fun MissionTypeItem(
    missionType: MissionType,
    onClick: () -> Unit,
) {
    val (iconRes, titleRes) = missionType.displayData()
    val title = stringResource(id = titleRes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
            )
            .padding(
                horizontal = 12.dp,
                vertical = 16.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier.size(28.dp),
            tint = Color.Unspecified,
        )

        Text(
            text = title,
            style = OrbitTheme.typography.headline2SemiBold,
            color = OrbitTheme.colors.white,
        )
    }
}

@Composable
private fun MissionDetailContent(
    missionType: MissionType,
    selectedMissionCount: Int,
    onCountChange: (Int) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onPreview: () -> Unit,
) {
    val (title, lottieRes) = when (missionType) {
        MissionType.SHAKE ->
            Pair(stringResource(id = feature.home.R.string.alarm_add_edit_selected_mission_shake), R.raw.mission_shake)
        MissionType.TAP ->
            Pair(stringResource(id = feature.home.R.string.alarm_add_edit_selected_mission_tap), R.raw.mission_tap)
        else -> return
    }
    val selectedMissionCountIndex = countOptions.indexOf(selectedMissionCount).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        MissionSelectTopAppBar(
            title = title,
            onBack = onBack,
            onClose = onClose,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = OrbitTheme.colors.gray_700,
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                LottieAnimation(
                    resId = lottieRes,
                    scaleXAdjustment = 0.85f,
                    scaleYAdjustment = 0.85f,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(id = feature.home.R.string.mission_detail_content_count_title),
                style = OrbitTheme.typography.headline2SemiBold,
                color = OrbitTheme.colors.gray_50,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(id = feature.home.R.string.mission_detail_content_count_level_easy),
                    style = OrbitTheme.typography.label2SemiBold,
                    color = OrbitTheme.colors.gray_300,
                )

                Text(
                    text = stringResource(id = feature.home.R.string.mission_detail_content_count_level_hard),
                    style = OrbitTheme.typography.label2SemiBold,
                    color = OrbitTheme.colors.gray_300,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SelectorItems(
                items = countOptions.map { stringResource(id = feature.home.R.string.mission_count_chip_format, it) },
                selectedIndex = selectedMissionCountIndex,
                enabled = true,
                onItemSelected = { index -> onCountChange(countOptions[index]) },
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OrbitButton(
                    label = stringResource(id = feature.home.R.string.mission_detail_content_btn_preview),
                    onClick = onPreview,
                    useFillMaxWidth = false,
                    enabled = true,
                    containerColor = OrbitTheme.colors.gray_600,
                    contentColor = OrbitTheme.colors.white,
                    pressedContainerColor = OrbitTheme.colors.gray_500,
                    pressedContentColor = OrbitTheme.colors.white.copy(alpha = 0.7f),
                )

                OrbitButton(
                    label = stringResource(id = feature.home.R.string.mission_detail_content_btn_save),
                    onClick = onSave,
                    enabled = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionSelectTopAppBar(
    title: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back",
            tint = OrbitTheme.colors.white,
            modifier = Modifier
                .customClickable(
                    rippleEnabled = false,
                    fadeOnPress = true,
                    pressedAlpha = 0.5f,
                    onClick = onBack,
                )
                .align(Alignment.CenterStart),
        )

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = OrbitTheme.typography.body1SemiBold,
            color = OrbitTheme.colors.white,
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable { onClose() }
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Close",
                modifier = Modifier.size(24.dp),
                tint = OrbitTheme.colors.white,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AlarmMissionSelectBottomSheetPreview() {
    OrbitTheme {
        AlarmMissionBottomSheet(
            missionState = AlarmAddEditContract.AlarmMissionState(),
            onDismiss = { },
            onSaveMission = { _, _ -> },
            onPreviewMission = { _, _ -> },
        )
    }
}
