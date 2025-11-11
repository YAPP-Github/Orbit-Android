package com.yapp.alarm.pendingIntent.interaction

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import com.yapp.alarm.AlarmConstants
import com.yapp.alarm.receivers.AlarmReceiver
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.toJson

fun createAlarmSnoozePendingIntent(
    context: Context,
    alarm: Alarm,
): PendingIntent {
    val snoozeIntent = createAlarmSnoozeIntent(context, alarm)
    return PendingIntent.getBroadcast(
        context,
        alarm.id.toInt(),
        snoozeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
    )
}

fun createAlarmSnoozeIntent(
    context: Context,
    alarm: Alarm,
): Intent {
    return Intent(context, AlarmReceiver::class.java).apply {
        action = AlarmConstants.ACTION_ALARM_SNOOZED
        putExtra(AlarmConstants.EXTRA_ALARM, alarm.toJson())
    }
}
