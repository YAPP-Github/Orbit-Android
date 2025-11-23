package com.yapp.orbit.benchmark

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.ui.component.timepicker.OrbitPicker
import java.time.LocalTime

internal const val ORBIT_PICKER_BENCHMARK_ROOT = "orbit_picker_root"

@Composable
internal fun OrbitPickerBenchmarkScreen(
    modifier: Modifier = Modifier,
    initialTime: LocalTime = LocalTime.of(8, 30),
) {
    var selectedTime by remember { mutableStateOf(initialTime) }
    var isDrawn by remember { mutableStateOf(false) }

    ReportDrawnWhen { isDrawn }

    LaunchedEffect(Unit) {
        isDrawn = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900)
            .semantics { contentDescription = ORBIT_PICKER_BENCHMARK_ROOT },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            OrbitPicker(
                initialTime = selectedTime,
                onValueChange = { selectedTime = it },
            )
        }
    }
}

@Preview
@Composable
private fun OrbitPickerBenchmarkPreview() {
    OrbitTheme {
        OrbitPickerBenchmarkScreen()
    }
}
