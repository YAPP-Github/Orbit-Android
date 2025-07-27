package com.yapp.home.alarm.component

import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toRepeatDays
import com.yapp.ui.component.checkbox.OrbitCheckBox
import com.yapp.ui.component.switch.OrbitSwitch
import feature.home.R
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun AlarmListItem(
    modifier: Modifier = Modifier,
    id: Long,
    repeatDays: Int,
    isHolidayAlarmOff: Boolean,
    swipeable: Boolean = true,
    selectable: Boolean = false,
    selected: Boolean = false,
    onClick: (Long) -> Unit,
    onLongPress: (Long, Float, Float) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onSwipe: (Long) -> Unit,
    hour: Int,
    minute: Int,
    isActive: Boolean,
    onToggleActive: (Long) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    var itemPosition by remember { mutableStateOf(Pair(0f, 0f)) }
    var itemSize by remember { mutableStateOf(IntSize(0, 0)) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                Handler(Looper.getMainLooper()).postDelayed({
                    onSwipe(id)
                }, 200,)
            }
            return@rememberSwipeToDismissBoxState it == SwipeToDismissBoxValue.EndToStart
        },
        positionalThreshold = {
            itemSize.width * 0.8f
        },
    )

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            rippleAlpha = RippleAlpha(
                pressedAlpha = 1f,
                focusedAlpha = 1f,
                hoveredAlpha = 1f,
                draggedAlpha = 1f,
            ),
        ),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = modifier,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = swipeable,
            gesturesEnabled = swipeable,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OrbitTheme.colors.gray_500)
                        .onGloballyPositioned {
                            itemSize = it.size
                            itemPosition = Pair(
                                it.positionInRoot().x,
                                it.positionInRoot().y,
                            )
                        },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        painter = painterResource(id = core.designsystem.R.drawable.ic_trash),
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .offset {
                                val offsetX = itemSize.width * (1 - dismissState.progress * 0.5f) - 12.dp.toPx()

                                IntOffset(
                                    x = offsetX.toInt(),
                                    y = 0,
                                )
                            },
                    )
                }
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) OrbitTheme.colors.gray_800 else OrbitTheme.colors.gray_900,
                    )
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = ripple(
                            color = OrbitTheme.colors.gray_800,
                        ),
                        onLongClick = {
                            if (selectable) return@combinedClickable

                            val minRequiredSpace = itemSize.height + with(density) { 42.dp.toPx() }
                            val adjustedY = if (itemPosition.second + minRequiredSpace > screenHeightPx) {
                                screenHeightPx - minRequiredSpace
                            } else {
                                itemPosition.second
                            }

                            onLongPress(id, itemPosition.first, adjustedY)
                        },
                    ) {
                        if (selectable) {
                            onToggleSelect(id)
                        } else {
                            onClick(id)
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectable) {
                    OrbitCheckBox(
                        checked = selected,
                        onCheckedChange = { onToggleSelect(id) },
                    )
                    Spacer(modifier = Modifier.width(26.dp))
                }

                AlarmListItemContent(
                    repeatDays = repeatDays,
                    isActive = isActive,
                    isHolidayAlarmOff = isHolidayAlarmOff,
                    hour = hour,
                    minute = minute,
                )

                if (!selectable) {
                    Spacer(modifier = Modifier.weight(1f))
                    OrbitSwitch(
                        isChecked = isActive,
                    ) {
                        onToggleActive(id)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmListItemContent(
    repeatDays: Int,
    isActive: Boolean,
    isHolidayAlarmOff: Boolean,
    hour: Int,
    minute: Int,
) {
    val (textColor, iconColor) = if (isActive) {
        OrbitTheme.colors.gray_300 to OrbitTheme.colors.gray_200
    } else {
        OrbitTheme.colors.gray_500 to OrbitTheme.colors.gray_500
    }

    val isAm = hour < 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = repeatDays.toRepeatDaysString(isAm, hour, minute),
                style = OrbitTheme.typography.label1SemiBold,
                color = textColor,
            )
            if (isHolidayAlarmOff) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = core.designsystem.R.drawable.ic_holiday),
                    contentDescription = "Holiday Alarm Off",
                    tint = iconColor,
                    modifier = Modifier.size(12.dp),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isAm) "오전" else "오후",
                style = OrbitTheme.typography.title2Medium,
                color = if (isActive) OrbitTheme.colors.white else OrbitTheme.colors.gray_500,
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "$displayHour",
                style = OrbitTheme.typography.title2Medium,
                color = if (isActive) OrbitTheme.colors.white else OrbitTheme.colors.gray_500,
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = ":",
                style = OrbitTheme.typography.title2Medium,
                color = if (isActive) OrbitTheme.colors.white else OrbitTheme.colors.gray_500,
                modifier = Modifier.offset(y = (-2).dp),
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = minute.toString().padStart(2, '0'),
                style = OrbitTheme.typography.title2Medium,
                color = if (isActive) OrbitTheme.colors.white else OrbitTheme.colors.gray_500,
            )
        }
    }
}

private fun Int.toRepeatDaysString(isAm: Boolean, hour: Int, minute: Int): String {
    val days = AlarmDay.entries.filter { (this and it.bitValue) != 0 }

    return when {
        days.size == 7 -> "매일"
        days.isNotEmpty() -> "매주 " + days.joinToString(", ") { it.toKoreanString() }
        else -> getNextAlarmDateWithTime(
            hour = hour,
            minute = minute,
        )
    }
}

private fun AlarmDay.toKoreanString(): String {
    return when (this) {
        AlarmDay.SUN -> "일"
        AlarmDay.MON -> "월"
        AlarmDay.TUE -> "화"
        AlarmDay.WED -> "수"
        AlarmDay.THU -> "목"
        AlarmDay.FRI -> "금"
        AlarmDay.SAT -> "토"
    }
}

private fun getNextAlarmDateWithTime(hour: Int, minute: Int): String {
    val now = LocalDateTime.now()

    val alarmTime = LocalTime.of(hour, minute)
    val todayAlarm = LocalDateTime.of(now.toLocalDate(), alarmTime)

    // 오늘 시간 이미 지났으면 내일로 설정
    val nextAlarmDate = if (todayAlarm.isAfter(now)) {
        todayAlarm.toLocalDate()
    } else {
        todayAlarm.plusDays(1).toLocalDate()
    }

    return nextAlarmDate.format(DateTimeFormatter.ofPattern("M월 d일"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmListItemMenu(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            rippleAlpha = RippleAlpha(
                pressedAlpha = 1f,
                focusedAlpha = 1f,
                hoveredAlpha = 1f,
                draggedAlpha = 1f,
            ),
        ),
    ) {
        Surface(
            modifier = Modifier
                .width(120.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(
                        bounded = false,
                        color = OrbitTheme.colors.gray_600,
                    ),
                    onClick = onClick,
                ),
            color = OrbitTheme.colors.gray_700,
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = text,
                    style = OrbitTheme.typography.body1SemiBold,
                    color = OrbitTheme.colors.alert,
                )

                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = iconRes),
                    contentDescription = "Icon",
                    tint = OrbitTheme.colors.alert,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AlarmListItemPreview() {
    OrbitTheme {
        val selectedDays = setOf(AlarmDay.MON, AlarmDay.WED, AlarmDay.FRI).toRepeatDays()
        var isActive by remember { mutableStateOf(true) }
        var selected by remember { mutableStateOf(true) }

        Column {
            AlarmListItem(
                id = 0,
                repeatDays = selectedDays,
                isHolidayAlarmOff = true,
                selectable = true,
                swipeable = false,
                selected = selected,
                hour = 6,
                minute = 0,
                isActive = isActive,
                onClick = { },
                onLongPress = { _, _, _ -> },
                onToggleActive = {
                    isActive = !isActive
                },
                onToggleSelect = {
                    selected = !selected
                },
                onSwipe = { },
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OrbitTheme.colors.gray_800)
                    .padding(horizontal = 24.dp),
            )
            AlarmListItem(
                id = 0,
                repeatDays = emptySet<AlarmDay>().toRepeatDays(),
                isHolidayAlarmOff = false,
                selectable = false,
                selected = false,
                swipeable = true,
                hour = 6,
                minute = 0,
                isActive = isActive,
                onClick = { },
                onLongPress = { _, _, _ -> },
                onToggleActive = {
                    isActive = !isActive
                },
                onToggleSelect = { },
                onSwipe = { },
            )
        }
    }
}

@Preview
@Composable
private fun AlarmListItemMenuPreview() {
    OrbitTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AlarmListItem(
                id = 0,
                repeatDays = 0,
                isHolidayAlarmOff = false,
                selectable = false,
                swipeable = false,
                selected = false,
                hour = 6,
                minute = 0,
                isActive = true,
                onClick = { },
                onLongPress = { _, _, _ -> },
                onToggleActive = { },
                onToggleSelect = { },
                onSwipe = { },
            )

            AlarmListItemMenu(
                text = stringResource(id = R.string.alarm_delete_dialog_btn_delete),
                iconRes = core.designsystem.R.drawable.ic_trash,
            ) {
            }
        }
    }
}
