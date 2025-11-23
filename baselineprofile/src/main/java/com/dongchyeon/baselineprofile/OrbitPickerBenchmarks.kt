package com.dongchyeon.baselineprofile

import android.content.ComponentName
import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class OrbitPickerBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun orbitPickerScrollCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun orbitPickerScrollCompilationBaselineProfile() = benchmark(CompilationMode.Partial())

    private fun benchmark(compilationMode: CompilationMode) {
        val targetPackage = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: throw IllegalStateException("targetAppId not passed as instrumentation runner arg")

        rule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = {
                killProcess()
                pressHome()
            },
            measureBlock = {
                val intent = Intent().apply {
                    component = ComponentName(targetPackage, BENCHMARK_ACTIVITY_NAME)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(BENCHMARK_SCREEN_EXTRA, ORBIT_PICKER_SCREEN_KEY)
                }

                startActivityAndWait(intent)

                device.wait(Until.hasObject(By.desc(ORBIT_PICKER_SEMANTICS)), 5_000)
                val picker = device.findObject(By.desc(ORBIT_PICKER_SEMANTICS))
                    ?: error("OrbitPicker root not found")

                val bounds = picker.visibleBounds
                val x = bounds.centerX() + bounds.width() / 4
                val startY = bounds.centerY() + bounds.height() / 4
                val endY = bounds.centerY() - bounds.height() / 4

                device.swipe(x, startY, x, endY, 30)
                device.waitForIdle()
            },
        )
    }

    private companion object {
        private const val BENCHMARK_ACTIVITY_NAME =
            "com.yapp.orbit.benchmark.BenchmarkHostActivity"
        private const val BENCHMARK_SCREEN_EXTRA = "benchmark_screen_key"
        private const val ORBIT_PICKER_SCREEN_KEY = "orbit_picker"
        private const val ORBIT_PICKER_SEMANTICS = "orbit_picker_root"
    }
}
