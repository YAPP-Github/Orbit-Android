package com.yapp.alarm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yapp.alarm.AndroidAlarmScheduler
import com.yapp.domain.usecase.AlarmUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RescheduleAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmUseCase: AlarmUseCase

    @Inject
    lateinit var androidAlarmScheduler: AndroidAlarmScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarm()
        }
    }

    private fun rescheduleAlarm() {
        CoroutineScope(Dispatchers.IO).launch {
            alarmUseCase.getAllAlarms().collect { alarms ->
                alarms.forEach { alarm ->
                    androidAlarmScheduler.scheduleAlarm(alarm)
                }
            }
        }
    }
}
