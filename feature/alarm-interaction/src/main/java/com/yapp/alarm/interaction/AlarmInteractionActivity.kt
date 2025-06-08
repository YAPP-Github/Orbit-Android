package com.yapp.alarm.interaction

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.navigation.compose.NavHost
import com.yapp.alarm.AlarmConstants
import com.yapp.alarm.receivers.AlarmInteractionActivityReceiver
import com.yapp.common.navigation.rememberOrbitNavigator
import com.yapp.common.navigation.route.AlarmInteractionBaseRoute
import com.yapp.designsystem.theme.OrbitTheme
import com.yapp.domain.model.Alarm
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmInteractionActivity : ComponentActivity() {

    private val broadcastReceiver = AlarmInteractionActivityReceiver(this)

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val alarm: Alarm? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(AlarmConstants.EXTRA_ALARM, Alarm::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(AlarmConstants.EXTRA_ALARM)
        }

        unlockScreen()

        registerAlarmInteractionActivityCloseReceiver()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.BLACK,
                android.graphics.Color.BLACK,
            ),
        )
        setContent {
            val navigator = rememberOrbitNavigator()

            Surface(
                color = OrbitTheme.colors.gray_900,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
            ) {
                NavHost(
                    navController = navigator.navController,
                    startDestination = AlarmInteractionBaseRoute,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    alarmInteractionNavGraph(
                        navigator = navigator,
                        alarm = alarm,
                    )
                }
            }

            DisposableEffect(this, navigator.navController) {
                val onNewIntentConsumer = Consumer<Intent> { newIntent ->
                    val newAlarm: Alarm? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        newIntent.getParcelableExtra(AlarmConstants.EXTRA_ALARM, Alarm::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        newIntent.getParcelableExtra(AlarmConstants.EXTRA_ALARM)
                    }
                    Log.d("AlarmInteractionActivity", "New Intent: $newIntent")
                    newAlarm?.let { alarm ->
                        navigator.navigateToAlarmAction(alarm = alarm)
                    }
                }

                this@AlarmInteractionActivity.addOnNewIntentListener(onNewIntentConsumer)

                onDispose {
                    this@AlarmInteractionActivity.removeOnNewIntentListener(onNewIntentConsumer)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterAlarmInteractionActivityCloseReceiver()
    }

    private fun unlockScreen() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val keyguardManager = getSystemService(KeyguardManager::class.java)
        keyguardManager.requestDismissKeyguard(this, null)
    }

    private fun registerAlarmInteractionActivityCloseReceiver() {
        val filter = IntentFilter(AlarmConstants.ACTION_ALARM_INTERACTION_ACTIVITY_CLOSE)
        registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED)
    }

    private fun unregisterAlarmInteractionActivityCloseReceiver() {
        unregisterReceiver(broadcastReceiver)
    }
}
