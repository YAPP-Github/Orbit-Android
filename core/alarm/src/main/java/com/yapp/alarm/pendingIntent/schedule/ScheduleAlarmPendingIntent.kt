package com.yapp.alarm.pendingIntent.schedule

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import com.yapp.alarm.AlarmConstants
import com.yapp.alarm.receivers.AlarmReceiver
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toJson

fun createAlarmReceiverPendingIntentForSchedule(
    app: Application,
    alarm: Alarm,
    day: AlarmDay? = null,
): PendingIntent {
    val alarmReceiverIntent = createAlarmReceiverIntent(app, alarm, day)
    return PendingIntent.getBroadcast(
        app,
        generateAlarmIntentId(alarm.id.toInt(), day),
        alarmReceiverIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

private fun createAlarmReceiverIntent(
    app: Application,
    alarm: Alarm,
    day: AlarmDay? = null,
): Intent {
    return Intent(AlarmConstants.ACTION_ALARM_TRIGGERED).apply {
        setClass(app, AlarmReceiver::class.java)
        putExtra(AlarmConstants.EXTRA_ALARM, alarm.toJson())
        day?.let { putExtra(AlarmConstants.EXTRA_ALARM_DAY, it.name) }
    }
}

fun generateAlarmIntentId(id: Int, day: AlarmDay?): Int {
    return day?.let {
        (id * 10) + ((day.ordinal + 6) % 7) + 1
    } ?: id
}
