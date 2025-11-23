package com.yapp.orbit.benchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yapp.designsystem.theme.OrbitTheme

internal const val EXTRA_BENCHMARK_SCREEN = "benchmark_screen_key"
internal const val BENCHMARK_SCREEN_ORBIT_PICKER = "orbit_picker"
internal const val BENCHMARK_UNKNOWN_SCREEN_ROOT = "benchmark_unknown_screen"

class BenchmarkHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screenKey = intent?.getStringExtra(EXTRA_BENCHMARK_SCREEN)
        setContent {
            OrbitTheme {
                BenchmarkScreenContainer(screenKey)
            }
        }
    }
}

@Composable
private fun BenchmarkScreenContainer(screenKey: String?) {
    when (screenKey) {
        BENCHMARK_SCREEN_ORBIT_PICKER -> OrbitPickerBenchmarkScreen()
        else -> BenchmarkScreenMissing(screenKey)
    }
}

@Composable
private fun BenchmarkScreenMissing(requestedKey: String?) {
    ReportDrawnWhen { true }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrbitTheme.colors.gray_900)
            .semantics { contentDescription = BENCHMARK_UNKNOWN_SCREEN_ROOT },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Benchmark screen '${requestedKey ?: "unknown"}' not registered.",
            color = OrbitTheme.colors.white,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
