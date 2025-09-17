package com.yapp.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        val UPDATE_NOTICE_DONT_SHOW_VERSION = stringPreferencesKey("update_notice_dont_show_version")
        val UPDATE_NOTICE_LAST_SHOWN_DATE_EPOCH = longPreferencesKey("update_notice_last_shown_date_epoch")
    }

    private fun todayEpoch(): Long = LocalDate.now().toEpochDay()

    val userIdFlow: Flow<Long?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.USER_ID] }
        .distinctUntilChanged()

    val userNameFlow: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.USER_NAME] }
        .distinctUntilChanged()

    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.ONBOARDING_COMPLETED] ?: false }
        .distinctUntilChanged()

    val updateNoticeDontShowVersionFlow: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.UPDATE_NOTICE_DONT_SHOW_VERSION] }
        .distinctUntilChanged()

    val updateNoticeLastShownDateEpochFlow: Flow<Long?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.UPDATE_NOTICE_LAST_SHOWN_DATE_EPOCH] }
        .distinctUntilChanged()

    suspend fun saveUserId(userId: Long) {
        dataStore.edit { it[Keys.USER_ID] = userId }
    }

    suspend fun saveUserName(userName: String) {
        dataStore.edit { it[Keys.USER_NAME] = userName }
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    suspend fun markUpdateNoticeDontShow(version: String) {
        dataStore.edit { it[Keys.UPDATE_NOTICE_DONT_SHOW_VERSION] = version }
    }

    suspend fun markUpdateNoticeShownToday() {
        dataStore.edit { pref ->
            pref[Keys.UPDATE_NOTICE_LAST_SHOWN_DATE_EPOCH] = todayEpoch()
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { it.clear() }
    }
}
