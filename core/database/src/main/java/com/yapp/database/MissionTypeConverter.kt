package com.yapp.database

import androidx.room.TypeConverter
import com.yapp.domain.model.MissionType

class MissionTypeConverter {

    @TypeConverter
    fun fromString(missionType: String): MissionType {
        return try {
            MissionType.valueOf(missionType)
        } catch (e: IllegalArgumentException) {
            MissionType.TAP
        }
    }

    @TypeConverter
    fun toString(missionType: MissionType): String {
        return missionType.name
    }
}
