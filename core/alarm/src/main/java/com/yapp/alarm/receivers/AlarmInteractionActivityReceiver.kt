package com.yapp.alarm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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
                val notificationId = intent.getLongExtra(AlarmConstants.EXTRA_NOTIFICATION_ID, -1L)
                val missionType = intent.getIntExtra(AlarmConstants.EXTRA_MISSION_TYPE, -1)
                val missionCount = intent.getIntExtra(AlarmConstants.EXTRA_MISSION_COUNT, -1)

                if (notificationId == -1L || missionType == -1 || missionCount == -1) {
                    Log.e("AlarmInteraction", "필수 값 누락")
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val fortuneDate = fortuneRepository.fortuneDateFlow.firstOrNull()
                    val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                    if (fortuneDate != todayDate) {
                        context?.let {
                            val uriString =
                                "orbitapp://mission?notificationId=$notificationId&missionType=$missionType&missionCount=$missionCount"
                            val missionIntent =
                                Intent(Intent.ACTION_VIEW, uriString.toUri()).apply {
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
