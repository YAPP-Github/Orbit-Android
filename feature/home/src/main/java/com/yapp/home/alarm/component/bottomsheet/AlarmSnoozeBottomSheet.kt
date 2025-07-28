package com.yapp.home.alarm.component.bottomsheet

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.home.alarm.component.SelectorItems
import com.yapp.ui.component.button.OrbitButton
import com.yapp.ui.component.switch.OrbitSwitch
import feature.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmSnoozeBottomSheet(
    snoozeEnabled: Boolean,
    snoozeInterval: Int,
    snoozeCount: Int,
    onIntervalSelected: (Int) -> Unit,
    onCountSelected: (Int) -> Unit,
    onSnoozeToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val snoozeIntervalOptions = listOf(1, 3, 5, 10, 15)
    val snoozeCountOptions = listOf(1, 3, 5, 10, -1)

    val snoozeIntervals = snoozeIntervalOptions.map {
        stringResource(id = R.string.alarm_add_edit_interval_minute, it)
    }
    val snoozeCounts = listOf(
        stringResource(id = R.string.alarm_add_edit_repeat_count_times, 1),
        stringResource(id = R.string.alarm_add_edit_repeat_count_times, 3),
        stringResource(id = R.string.alarm_add_edit_repeat_count_times, 5),
        stringResource(id = R.string.alarm_add_edit_repeat_count_times, 10),
        stringResource(id = R.string.alarm_add_edit_repeat_count_infinite),
    )

    var selectedSnoozeEnabled by remember { mutableStateOf(snoozeEnabled) }
    var selectedSnoozeIntervalIndex by remember { mutableIntStateOf(snoozeIntervalOptions.indexOf(snoozeInterval)) }
    var selectedSnoozeCountIndex by remember {
        mutableIntStateOf(
            if (snoozeCount == -1) {
                snoozeCountOptions.lastIndex
            } else {
                snoozeCountOptions.indexOf(snoozeCount)
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        VibrationSection(selectedSnoozeEnabled) {
            selectedSnoozeEnabled = !selectedSnoozeEnabled
            onSnoozeToggle(selectedSnoozeEnabled)
        }
        Spacer(modifier = Modifier.height(20.dp))
        SelectorSection(
            title = stringResource(id = R.string.alarm_add_edit_interval),
            selectedIndex = selectedSnoozeIntervalIndex,
            items = snoozeIntervals,
            enabled = selectedSnoozeEnabled,
            onItemSelected = {
                selectedSnoozeIntervalIndex = it
                onIntervalSelected(snoozeIntervalOptions[it])
            },
        )
        Spacer(modifier = Modifier.height(32.dp))
        SelectorSection(
            title = stringResource(id = R.string.alarm_add_edit_repeat_count),
            selectedIndex = selectedSnoozeCountIndex,
            items = snoozeCounts,
            enabled = selectedSnoozeEnabled,
            onItemSelected = {
                selectedSnoozeCountIndex = it
                onCountSelected(snoozeCountOptions[it])
            },
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (selectedSnoozeEnabled) {
            AlarmSnoozeMessage(
                interval = snoozeIntervals[selectedSnoozeIntervalIndex],
                count = snoozeCounts[selectedSnoozeCountIndex],
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        OrbitButton(
            label = stringResource(id = R.string.alarm_add_edit_complete),
            enabled = true,
            containerColor = OrbitTheme.colors.gray_600,
            contentColor = OrbitTheme.colors.white,
            pressedContainerColor = OrbitTheme.colors.gray_500,
            pressedContentColor = OrbitTheme.colors.white.copy(alpha = 0.7f),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun VibrationSection(snoozeEnabled: Boolean, onSnoozeToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.alarm_add_edit_alarm_snooze),
            style = OrbitTheme.typography.heading2SemiBold,
            color = OrbitTheme.colors.white,
        )
        Spacer(modifier = Modifier.weight(1f))
        OrbitSwitch(
            isChecked = snoozeEnabled,
            isEnabled = true,
            onClick = onSnoozeToggle,
        )
    }
}

@Composable
private fun SelectorSection(
    title: String,
    selectedIndex: Int,
    items: List<String>,
    enabled: Boolean,
    onItemSelected: (Int) -> Unit,
) {
    Column {
        Text(
            text = title,
            style = OrbitTheme.typography.headline2Medium,
            color = OrbitTheme.colors.gray_50,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SelectorItems(
            items = items,
            selectedIndex = selectedIndex,
            enabled = enabled,
            onItemSelected = onItemSelected,
        )
    }
}

@Composable
private fun AlarmSnoozeMessage(interval: String, count: String) {
    val formattedCount = if (count == stringResource(id = R.string.alarm_add_edit_repeat_count_infinite)) "${count}번" else count

    Box(
        modifier = Modifier
            .background(
                color = OrbitTheme.colors.gray_700,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.alarm_add_edit_alarm_snooze_description, interval, formattedCount),
            style = OrbitTheme.typography.label1Medium,
            color = OrbitTheme.colors.main,
        )
    }
}

@Preview
@Composable
private fun AlarmSnoozeBottomSheetPreview() {
    var isSnoozeEnabled by remember { mutableStateOf(true) }
    var snoozeInterval by remember { mutableIntStateOf(5) }
    var snoozeCount by remember { mutableIntStateOf(5) }

    OrbitTheme {
        AlarmSnoozeBottomSheet(
            snoozeEnabled = isSnoozeEnabled,
            snoozeInterval = snoozeInterval,
            snoozeCount = snoozeCount,
            onSnoozeToggle = {
                isSnoozeEnabled = !isSnoozeEnabled
                Log.d("AlarmSnoozeBottomSheet", "Snooze Enabled: $isSnoozeEnabled")
            },
            onIntervalSelected = { interval ->
                snoozeInterval = interval
                Log.d("AlarmSnoozeBottomSheet", "Snooze Interval: $snoozeInterval")
            },
            onCountSelected = { count ->
                snoozeCount = count
                Log.d("AlarmSnoozeBottomSheet", "Snooze Count: $snoozeCount")
            },
            onDismiss = { },
        )
    }
}
