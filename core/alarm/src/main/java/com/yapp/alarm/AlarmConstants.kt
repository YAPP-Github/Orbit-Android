package com.yapp.alarm

object AlarmConstants {
    const val ACTION_ALARM_TRIGGERED = "com.yapp.orbit.ACTION_TRIGGERED"
    const val ACTION_ALARM_DISMISSED = "com.yapp.orbit.ACTION_DISMISSED"
    const val ACTION_ALARM_SNOOZED = "com.yapp.orbit.ACTION_SNOOZED"
    const val ACTION_ALARM_INTERACTION_ACTIVITY_CLOSE = "com.yapp.orbit.ACTION_ALERT_INTERACTION_CLOSE"

    const val EXTRA_NOTIFICATION_ID = "com.yapp.orbit.EXTRA_NOTIFICATION_ID"
    const val EXTRA_MISSION_TYPE = "com.yapp.orbit.EXTRA_MISSION_TYPE"
    const val EXTRA_MISSION_COUNT = "com.yapp.orbit.EXTRA_MISSION_COUNT"

    const val EXTRA_ALARM = "com.yapp.orbit.EXTRA_ALARM"
    const val EXTRA_ALARM_DAY = "com.yapp.orbit.EXTRA_ALARM_DAY"

    const val EXTRA_IS_SNOOZED = "com.yapp.orbit.EXTRA_IS_SNOOZED"
    const val EXTRA_IS_DISMISS = "com.yapp.orbit.EXTRA_IS_DISMISS"

    const val SNOOZE_ID_OFFSET = 10000

    val HOLIDAYS_2025 = setOf(
        "2025-01-01", "2025-01-27", "2025-01-28", "2025-01-29", "2025-01-30",
        "2025-03-01", "2025-03-03", "2025-05-05", "2025-05-06", "2025-06-06",
        "2025-08-15", "2025-10-03", "2025-10-05", "2025-10-06", "2025-10-07",
        "2025-10-08", "2025-10-09", "2025-12-25",
    )
}
