package com.yapp.alarm.pendingIntent.schedule

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import com.yapp.alarm.AlarmConstants
import com.yapp.alarm.receivers.AlarmReceiver
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toJson

fun createAlarmReceiverPendingIntentForUnSchedule(
    app: Application,
    alarm: Alarm,
    day: AlarmDay? = null,
): PendingIntent {
    val alarmReceiverIntent = createAlarmReceiverIntent(app, alarm)
    return PendingIntent.getBroadcast(
        app,
        generateAlarmIntentId(alarm.id.toInt(), day),
        alarmReceiverIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun createAlarmReceiverIntent(app: Application, alarm: Alarm): Intent {
    return Intent(AlarmConstants.ACTION_ALARM_TRIGGERED).apply {
        setClass(app, AlarmReceiver::class.java)
        putExtra(AlarmConstants.EXTRA_ALARM, alarm.toJson())
    }
}
