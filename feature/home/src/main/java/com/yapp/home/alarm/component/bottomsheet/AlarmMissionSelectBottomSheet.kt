package com.yapp.home.alarm.component.bottomsheet

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.yapp.ui.component.OrbitBottomSheet
import com.yapp.ui.extensions.customClickable
import kotlinx.coroutines.launch

enum class AlarmMissionSelectBottomSheetType {
    MISSION_ADD,
    MISSION_SELECT,
    MISSION_DETAIL,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmMissionSelectBottomSheet(
    missionType: MissionType,
    isSheetOpen: Boolean,
    onDismiss: () -> Unit,
) {
    var currentStep by remember { mutableStateOf(AlarmMissionSelectBottomSheetType.MISSION_ADD) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            AlarmMissionSelectBottomSheetType.MISSION_ADD -> {
                MissionAddContent {
                    currentStep = AlarmMissionSelectBottomSheetType.MISSION_SELECT
                }
            }

            AlarmMissionSelectBottomSheetType.MISSION_SELECT -> {
                MissionSelectContent(
                    onBack = {
                        currentStep = AlarmMissionSelectBottomSheetType.MISSION_ADD
                    },
                    onClose = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion { onDismiss() }
                    },
                    onSelect = { selectedMissionType ->
                        currentStep = AlarmMissionSelectBottomSheetType.MISSION_DETAIL
                    },
                )
            }

            AlarmMissionSelectBottomSheetType.MISSION_DETAIL -> {
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
            .padding(
                horizontal = 24.dp,
                vertical = 12.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.align(Alignment.Start),
            text = "미션 선택",
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
            painter = painterResource(core.designsystem.R.drawable.ic_plus),
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
        Spacer(modifier = Modifier.height(10.dp))

        MissionSelectTopAppBar(
            title = "미션 선택",
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
            Pair(core.designsystem.R.drawable.ic_mission_shake, "흔들기")
        MissionType.TAP ->
            Pair(core.designsystem.R.drawable.ic_mission_tap, "터치하기")
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
            painter = painterResource(id = core.designsystem.R.drawable.ic_back),
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
                painter = painterResource(id = core.designsystem.R.drawable.ic_close),
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
            missionType = MissionType.NONE,
            isSheetOpen = true,
        ) { }
    }
}
