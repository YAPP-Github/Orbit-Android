package com.yapp.alarm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.yapp.alarm.AlarmConstants
import com.yapp.domain.model.MissionType
import com.yapp.domain.repository.FortuneRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
                val missionTypeRaw = intent.getIntExtra(AlarmConstants.EXTRA_MISSION_TYPE, -1)
                val missionCount = intent.getIntExtra(AlarmConstants.EXTRA_MISSION_COUNT, -1)

                val missionType = MissionType.fromInt(missionTypeRaw)

                val hasValidMissionData = (
                    notificationId != -1L &&
                        missionType != MissionType.NONE &&
                        missionCount != -1
                    )

                CoroutineScope(Dispatchers.IO).launch {
                    if (!hasValidMissionData) {
                        val hasUnseenFortune = fortuneRepository.hasUnseenFortuneFlow.first()

                        if (hasUnseenFortune) {
                            context?.let { ctx ->
                                val uri = "orbitapp://fortune".toUri()
                                val fortuneIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    setPackage(ctx.packageName)
                                }
                                ctx.startActivity(fortuneIntent)
                            }
                        }
                        return@launch
                    }

                    context?.let { ctx ->
                        val uriString =
                            "orbitapp://mission?notificationId=$notificationId&missionType=${missionType.value}&missionCount=$missionCount"
                        val missionIntent = Intent(Intent.ACTION_VIEW, uriString.toUri()).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            setPackage(ctx.packageName)
                        }
                        ctx.startActivity(missionIntent)
                    }
                }
            }
        }
    }
}
