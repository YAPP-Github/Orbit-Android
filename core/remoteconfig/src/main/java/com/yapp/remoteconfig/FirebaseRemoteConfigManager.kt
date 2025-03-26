package com.yapp.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            false
        }
    }

    fun getRawMissionType(): String {
        val rawValue = remoteConfig.getString(KEY_MISSION_TYPE)
        return rawValue
    }

    companion object {
        private const val KEY_MISSION_TYPE = "alarm_mission_type"
    }
}
