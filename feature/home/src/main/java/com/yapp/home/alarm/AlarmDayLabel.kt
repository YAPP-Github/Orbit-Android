package com.yapp.home.alarm

import com.yapp.domain.model.AlarmDay
import feature.home.R

fun AlarmDay.getLabelStringRes(): Int {
    return when (this) {
        AlarmDay.SUN -> R.string.alarm_add_edit_sunday
        AlarmDay.MON -> R.string.alarm_add_edit_monday
        AlarmDay.TUE -> R.string.alarm_add_edit_tuesday
        AlarmDay.WED -> R.string.alarm_add_edit_wednesday
        AlarmDay.THU -> R.string.alarm_add_edit_thursday
        AlarmDay.FRI -> R.string.alarm_add_edit_friday
        AlarmDay.SAT -> R.string.alarm_add_edit_saturday
    }
}
