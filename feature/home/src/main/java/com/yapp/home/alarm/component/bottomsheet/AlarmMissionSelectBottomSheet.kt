package com.yapp.home.alarm.component.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.MissionType
import com.yapp.ui.component.OrbitBottomSheet
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
        Spacer(modifier = Modifier.height(8.dp))

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
