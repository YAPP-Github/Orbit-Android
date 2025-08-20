package com.yapp.domain.model

import java.time.DayOfWeek

enum class AlarmDay(val bitValue: Int) {
    SUN(0b0000001), // 1
    MON(0b0000010), // 2
    TUE(0b0000100), // 4
    WED(0b0001000), // 8
    THU(0b0010000), // 16
    FRI(0b0100000), // 32
    SAT(0b1000000), // 64
    ;
}

fun AlarmDay.toDayOfWeek(): DayOfWeek {
    return DayOfWeek.of(((this.ordinal + 6) % 7) + 1)
}

fun DayOfWeek.toAlarmDay(): AlarmDay {
    val index = (this.value % 7)
    return AlarmDay.entries[index]
}

fun Set<AlarmDay>.toRepeatDays(): Int {
    return this.fold(0) { acc, day ->
        acc or day.bitValue
    }
}

fun Int.toAlarmDays(): Set<AlarmDay> {
    return AlarmDay.entries.filterTo(mutableSetOf()) { (this and it.bitValue) != 0 }
}

fun Int.toAlarmDayNames(): List<String> {
    return AlarmDay.entries.filter { (this and it.bitValue) != 0 }
        .map { it.name.replaceFirstChar { char -> char.uppercase() } }
}
