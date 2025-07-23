package com.yapp.home.alarm.component.bottomsheet

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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.MissionType
import com.yapp.home.alarm.component.SelectorItems
import com.yapp.ui.component.OrbitBottomSheet
import com.yapp.ui.component.lottie.LottieAnimation
import com.yapp.ui.extensions.customClickable
import core.designsystem.R
import kotlinx.coroutines.launch

enum class AlarmMissionSelectBottomSheetType {
    MISSION_SETTING,
    MISSION_SELECT,
    MISSION_DETAIL,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmMissionSelectBottomSheet(
    missionType: MissionType,
    missionCount: Int,
    isSheetOpen: Boolean,
    onDismiss: () -> Unit,
    onSaveMission: (MissionType, Int) -> Unit,
    onPreviewMission: (MissionType) -> Unit,
) {
    var currentStep by remember { mutableStateOf(AlarmMissionSelectBottomSheetType.MISSION_SETTING) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMissionType by remember { mutableStateOf(missionType) }
    var selectedMissionCount by remember { mutableIntStateOf(missionCount) }

    OrbitBottomSheet(
        isSheetOpen = isSheetOpen,
        sheetState = sheetState,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion { onDismiss() }
        },
    ) {
        when (currentStep) {
            AlarmMissionSelectBottomSheetType.MISSION_SETTING -> {
                if (selectedMissionType == MissionType.NONE) {
                    MissionAddContent {
                        currentStep = AlarmMissionSelectBottomSheetType.MISSION_SELECT
                    }
                } else {
                    MissionSettingContent(
                        missionType = missionType,
                        missionCount = missionCount,
                        onDetail = {
                            currentStep = AlarmMissionSelectBottomSheetType.MISSION_DETAIL
                        },
                        onDelete = {
                            selectedMissionType = MissionType.NONE
                        },
                        onChange = {
                            currentStep = AlarmMissionSelectBottomSheetType.MISSION_SELECT
                        },
                        onDone = {
                            onSaveMission(selectedMissionType, selectedMissionCount)
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion { onDismiss() }
                        },
                    )
                }
            }

            AlarmMissionSelectBottomSheetType.MISSION_SELECT -> {
                MissionSelectContent(
                    onBack = {
                        currentStep = AlarmMissionSelectBottomSheetType.MISSION_SETTING
                    },
                    onClose = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion { onDismiss() }
                    },
                    onSelect = { mission ->
                        selectedMissionType = mission
                        currentStep = AlarmMissionSelectBottomSheetType.MISSION_DETAIL
                    },
                )
            }

            AlarmMissionSelectBottomSheetType.MISSION_DETAIL -> {
                MissionDetailContent(
                    missionType = selectedMissionType,
                    selectedMissionCount = selectedMissionCount,
                    onCountChange = { count ->
                        selectedMissionCount = count
                    },
                    onBack = {
                        currentStep = AlarmMissionSelectBottomSheetType.MISSION_SELECT
                    },
                    onClose = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion { onDismiss() }
                    },
                    onSave = {
                        onSaveMission(selectedMissionType, selectedMissionCount)
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion { onDismiss() }
                    },
                    onPreview = {
                        onPreviewMission(selectedMissionType)
                    },
                )
            }
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
            text = "미션",
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
                    text = "등록된 미션이 없어요",
                    style = OrbitTheme.typography.body1Bold,
                    color = OrbitTheme.colors.white,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "새 미션을 추가해보세요",
                    style = OrbitTheme.typography.label2Regular,
                    color = OrbitTheme.colors.white.copy(alpha = 0.8f),
                )

                Spacer(modifier = Modifier.height(32.dp))

                AddMissionButton {
                    onNext()
                }
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
            text = "미션추가",
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
            text = "미션",
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
            Button(
                onClick = onChange,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrbitTheme.colors.gray_600,
                    contentColor = OrbitTheme.colors.white,
                ),
                contentPadding = PaddingValues(
                    horizontal = 28.dp,
                    vertical = 14.dp,
                ),
            ) {
                Text(
                    text = "미션 변경",
                    style = OrbitTheme.typography.body1SemiBold,
                    color = OrbitTheme.colors.white,
                )
            }

            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrbitTheme.colors.main,
                    contentColor = OrbitTheme.colors.gray_900,
                ),
                contentPadding = PaddingValues(
                    horizontal = 28.dp,
                    vertical = 14.dp,
                ),
            ) {
                Text(
                    text = "완료",
                    style = OrbitTheme.typography.body1SemiBold,
                    color = OrbitTheme.colors.gray_900,
                )
            }
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
    if (missionType == MissionType.NONE) return

    val (iconRes, title) = when (missionType) {
        MissionType.SHAKE ->
            Pair(R.drawable.ic_mission_shake, "흔들기")
        MissionType.TAP ->
            Pair(R.drawable.ic_mission_tap, "터치하기")
        else -> return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
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
            text = "${count}회",
            style = OrbitTheme.typography.label2Regular,
            color = OrbitTheme.colors.main.copy(alpha = 0.9f),
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "Close",
            modifier = Modifier
                .size(12.dp),
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
            title = "미션",
            onBack = onBack,
            onClose = onClose,
        )

        Column {
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
    if (missionType == MissionType.NONE) return

    val (iconRes, title) = when (missionType) {
        MissionType.SHAKE ->
            Pair(R.drawable.ic_mission_shake, "흔들기")
        MissionType.TAP ->
            Pair(R.drawable.ic_mission_tap, "터치하기")
        else -> return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
            )
            .padding(
                horizontal = 20.dp,
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
    onPreview: (MissionType) -> Unit,
) {
    val (title, lottieRes) = when (missionType) {
        MissionType.SHAKE ->
            Pair("흔들기", R.raw.mission_shake)
        MissionType.TAP ->
            Pair("터치하기", R.raw.mission_tap)
        else -> return
    }
    val countOptions = listOf(5, 10, 15, 20, 30)
    val selectedMissionCountIndex = countOptions.indexOf(selectedMissionCount)

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
                    vertical = 24.dp,
                ),
        ) {
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
                text = "횟수",
                style = OrbitTheme.typography.headline2SemiBold,
                color = OrbitTheme.colors.gray_50,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "쉬움",
                    style = OrbitTheme.typography.label2SemiBold,
                    color = OrbitTheme.colors.gray_300,
                )

                Text(
                    text = "어려움",
                    style = OrbitTheme.typography.label2SemiBold,
                    color = OrbitTheme.colors.gray_300,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SelectorItems(
                items = countOptions.map { "${it}회" },
                selectedIndex = selectedMissionCountIndex,
                enabled = true,
                onItemSelected = { index -> onCountChange(countOptions[index]) },
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = {
                        onPreview(missionType)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrbitTheme.colors.gray_600,
                        contentColor = OrbitTheme.colors.white,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 28.dp,
                        vertical = 14.dp,
                    ),
                ) {
                    Text(
                        text = "미리보기",
                        style = OrbitTheme.typography.body1SemiBold,
                        color = OrbitTheme.colors.white,
                    )
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrbitTheme.colors.main,
                        contentColor = OrbitTheme.colors.gray_900,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 28.dp,
                        vertical = 14.dp,
                    ),
                ) {
                    Text(
                        text = "미션 저장",
                        style = OrbitTheme.typography.body1SemiBold,
                        color = OrbitTheme.colors.gray_900,
                    )
                }
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

@Preview
@Composable
private fun AlarmMissionSelectBottomSheetPreview() {
    OrbitTheme {
        AlarmMissionSelectBottomSheet(
            missionType = MissionType.SHAKE,
            missionCount = 15,
            isSheetOpen = true,
            onDismiss = {},
            onSaveMission = { _, _ -> },
            onPreviewMission = {},
        )
    }
}
