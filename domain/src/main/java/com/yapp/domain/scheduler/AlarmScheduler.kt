package com.yapp.domain.scheduler

import com.yapp.domain.model.Alarm

interface AlarmScheduler {
    fun scheduleAlarm(alarm: Alarm)
    fun unScheduleAlarm(alarm: Alarm)
}
