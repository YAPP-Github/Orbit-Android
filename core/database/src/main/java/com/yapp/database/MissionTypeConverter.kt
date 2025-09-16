package com.yapp.database

import androidx.room.TypeConverter
import com.yapp.domain.model.MissionType

class MissionTypeConverter {

    @TypeConverter
    fun fromInt(value: Int): MissionType {
        return MissionType.fromInt(value)
    }

    @TypeConverter
    fun toInt(missionType: MissionType): Int {
        return missionType.value
    }
}
