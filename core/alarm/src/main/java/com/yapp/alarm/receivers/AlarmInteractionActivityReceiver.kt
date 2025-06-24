package com.yapp.alarm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.yapp.alarm.AlarmConstants
import com.yapp.domain.repository.FortuneRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class AlarmInteractionActivityReceiver(private val activity: ComponentActivity) : BroadcastReceiver() {

    @Inject
    lateinit var fortuneRepository: FortuneRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        val isSnoozed = intent?.getBooleanExtra(AlarmConstants.EXTRA_IS_SNOOZED, false) ?: false

        if (intent?.action == AlarmConstants.ACTION_ALARM_INTERACTION_ACTIVITY_CLOSE) {
            activity.finish()

            if (!isSnoozed) {
                CoroutineScope(Dispatchers.IO).launch {
                    val fortuneDate = fortuneRepository.fortuneDateFlow.firstOrNull()
                    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                    if (fortuneDate != todayDate) {
                        context?.let {
                            val missionIntent =
                                Intent(Intent.ACTION_VIEW, "orbitapp://mission".toUri()).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    setPackage(context.packageName)
                                }
                            it.startActivity(missionIntent)
                        }
                    }
                }
            }
        }
    }
}
