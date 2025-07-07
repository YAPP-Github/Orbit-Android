package com.yapp.database

import androidx.room.TypeConverter
import com.yapp.domain.model.MissionType

class MissionTypeConverter {

    @TypeConverter
    fun fromMissionType(missionType: String): MissionType {
        return MissionType.valueOf(missionType)
    }

    @TypeConverter
    fun toMissionType(missionType: MissionType): String {
        return missionType.name
    }
}
