package com.yapp.ui.component.timepicker

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import kotlinx.coroutines.launch
import java.time.LocalTime

enum class TimePeriod(val displayName: String) {
    AM("오전"),
    PM("오후"),
    ;

    override fun toString(): String = displayName
}

@Composable
fun OrbitPicker(
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 2.dp,
    initialTime: LocalTime = LocalTime.now(),
    onValueChange: (LocalTime) -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .wrapContentSize()
                .background(OrbitTheme.colors.gray_900),
        ) {
            val amPmItems = remember { TimePeriod.entries.toList().map { it.displayName } }
            val hourItems = remember { (1..12).toList() }
            val minuteItems = remember { (0..59).toList() }

            val amPmPickerState = rememberPickerState(
                initialIndex = if (initialTime.hour < 12) 0 else 1,
                items = amPmItems,
            )
            val hourPickerState = rememberPickerState(
                initialIndex = hourItems.indexOf(if (initialTime.hour % 12 == 0) 12 else initialTime.hour % 12),
                items = hourItems,
            )
            val minutePickerState = rememberPickerState(
                initialIndex = minuteItems.indexOf(initialTime.minute),
                items = minuteItems,
            )

            var previousHour by remember { mutableIntStateOf(initialTime.hour) }
            val scope = rememberCoroutineScope()

            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp)
                        .height(45.dp)
                        .background(OrbitTheme.colors.gray_700, shape = RoundedCornerShape(12.dp)),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OrbitPickerItem(
                        state = amPmPickerState,
                        items = amPmItems,
                        visibleItemsCount = 3,
                        itemSpacing = itemSpacing,
                        textStyle = OrbitTheme.typography.heading1SemiBold,
                        modifier = Modifier.weight(1f),
                        textModifier = Modifier.padding(8.dp),
                        infiniteScroll = false,
                        onValueChange = {
                            onPickerValueChange(
                                amPmPickerState,
                                hourPickerState,
                                minutePickerState,
                                onValueChange,
                            )
                        },
                    )

                    OrbitPickerItem(
                        state = hourPickerState,
                        items = hourItems,
                        visibleItemsCount = 5,
                        itemSpacing = itemSpacing,
                        textStyle = OrbitTheme.typography.heading1SemiBold,
                        modifier = Modifier.weight(1f),
                        textModifier = Modifier.padding(8.dp),
                        infiniteScroll = true,
                        onValueChange = {
                            onPickerValueChange(
                                amPmPickerState,
                                hourPickerState,
                                minutePickerState,
                                onValueChange,
                            )
                            scope.launch {
                                val currentHour = hourPickerState.selectedItem
                                val currentIndex = amPmPickerState.lazyListState.firstVisibleItemIndex % amPmItems.size
                                val nextIndex = (currentIndex + 1) % amPmItems.size

                                if ((currentHour == 12 && previousHour == 11) ||
                                    (currentHour == 11 && previousHour == 12)
                                ) {
                                    amPmPickerState.lazyListState.animateScrollToItem(nextIndex)
                                }
                                previousHour = currentHour
                            }
                        },
                    )

                    OrbitPickerItem(
                        state = minutePickerState,
                        items = minuteItems,
                        visibleItemsCount = 5,
                        itemSpacing = itemSpacing,
                        textStyle = OrbitTheme.typography.heading1SemiBold,
                        modifier = Modifier.weight(1f),
                        textModifier = Modifier.padding(8.dp),
                        infiniteScroll = true,
                        itemFormatter = { it.toString().padStart(2, '0') },
                        onValueChange = {
                            onPickerValueChange(
                                amPmPickerState,
                                hourPickerState,
                                minutePickerState,
                                onValueChange,
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun onPickerValueChange(
    amPmState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<Int>,
    onValueChange: (LocalTime) -> Unit,
) {
    val amPm = amPmState.selectedItem
    val hour = hourState.selectedItem
    val minute = minuteState.selectedItem

    val adjustedHour = if (amPm == TimePeriod.AM.displayName && hour == 12) {
        0
    } else if (amPm == TimePeriod.PM.displayName && hour != 12) {
        hour + 12
    } else {
        hour
    }

    val newTime = LocalTime.of(adjustedHour, minute)

    onValueChange(newTime)
}

@Preview(showBackground = true)
@Composable
fun OrbitPickerPreview() {
    OrbitPicker() { newTime ->
        Log.d("OrbitPicker", "selectedTime: $newTime")
    }
}
