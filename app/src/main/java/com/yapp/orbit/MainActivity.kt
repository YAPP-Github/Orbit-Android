package com.yapp.orbit

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.yapp.analytics.AnalyticsHelper
import com.yapp.analytics.LocalAnalyticsHelper
import com.yapp.common.navigation.rememberOrbitNavigator
import com.yapp.designsystem.theme.OrbitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            val navigator = rememberOrbitNavigator()

            OrbitTheme {
                CompositionLocalProvider(LocalAnalyticsHelper provides analyticsHelper) {
                    OrbitNavHost(
                        navigator = navigator,
                    )
                }
            }
        }
    }
}
