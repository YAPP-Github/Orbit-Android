package com.yapp.alarm.pendingIntent.interaction

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import com.yapp.alarm.AlarmConstants
import com.yapp.domain.model.Alarm

fun createAlarmAlertPendingIntent(
    context: Context,
    alarm: Alarm,
): PendingIntent {
    val alarmAlertIntent = createAlarmAlertIntent(
        context,
        alarm.id,
        alarm,
    )
    return PendingIntent.getActivity(
        context,
        alarm.id.toInt(),
        alarmAlertIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
    )
}

private fun createAlarmAlertIntent(
    context: Context,
    notificationId: Long,
    alarm: Alarm,
): Intent {
    return Intent("com.yapp.alarm.interaction.ACTION_ALARM_INTERACTION").apply {
        putExtra(AlarmConstants.EXTRA_NOTIFICATION_ID, notificationId)
        putExtra(AlarmConstants.EXTRA_ALARM, alarm)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        setPackage(context.packageName)
    }
}
