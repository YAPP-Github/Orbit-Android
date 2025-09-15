package com.yapp.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

        val FORTUNE_ID = longPreferencesKey("fortune_id")
        val FORTUNE_DATE = stringPreferencesKey("fortune_date")
        val FORTUNE_IMAGE_ID = intPreferencesKey("fortune_image_id")
        val FORTUNE_SCORE = intPreferencesKey("fortune_score")
        val FORTUNE_SEEN = booleanPreferencesKey("fortune_seen")
        val FORTUNE_TOOLTIP_SHOWN = booleanPreferencesKey("fortune_tooltip_shown")
        val FORTUNE_CREATING = booleanPreferencesKey("fortune_creating")
        val FORTUNE_FAILED = booleanPreferencesKey("fortune_failed")

        val FIRST_ALARM_DISMISSED_TODAY = booleanPreferencesKey("first_alarm_dismissed_today")
        val FIRST_ALARM_DISMISSED_DATE = stringPreferencesKey("first_alarm_dismissed_date")

        val UPDATE_BOTTOM_SHEET_DONT_SHOW_VERSION = stringPreferencesKey("update_bottom_sheet_dont_show_version")
        val UPDATE_BOTTOM_SHEET_LAST_CLOSED_DATE = stringPreferencesKey("update_bottom_sheet_last_closed_date")
    }

    private fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

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

    val fortuneIdFlow: Flow<Long?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FORTUNE_ID] }
        .distinctUntilChanged()

    val fortuneDateFlow: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FORTUNE_DATE] }
        .distinctUntilChanged()

    val fortuneImageIdFlow: Flow<Int?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FORTUNE_IMAGE_ID] }
        .distinctUntilChanged()

    val fortuneScoreFlow: Flow<Int?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FORTUNE_SCORE] }
        .distinctUntilChanged()

    val hasUnseenFortuneFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { pref ->
            pref[Keys.FORTUNE_DATE] == today() &&
                pref[Keys.FORTUNE_ID] != null &&
                (pref[Keys.FORTUNE_SEEN] != true)
        }
        .distinctUntilChanged()

    val shouldShowFortuneToolTipFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { pref ->
            val hasTodayFortune = pref[Keys.FORTUNE_DATE] == today() && pref[Keys.FORTUNE_ID] != null
            val tooltipNotShown = pref[Keys.FORTUNE_TOOLTIP_SHOWN] ?: false
            hasTodayFortune && !tooltipNotShown
        }
        .distinctUntilChanged()

    val isFortuneCreatingFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FORTUNE_CREATING] ?: false }
        .distinctUntilChanged()

    val isFortuneFailedFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FORTUNE_FAILED] ?: false }
        .distinctUntilChanged()

    val isFirstAlarmDismissedTodayFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { pref ->
            val flag = pref[Keys.FIRST_ALARM_DISMISSED_TODAY] ?: false
            val date = pref[Keys.FIRST_ALARM_DISMISSED_DATE]
            flag && date == today()
        }
        .distinctUntilChanged()

    val updateBottomSheetDontShowVersionFlow: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.UPDATE_BOTTOM_SHEET_DONT_SHOW_VERSION] }
        .distinctUntilChanged()

    val updateBottomSheetLastClosedDateFlow: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.UPDATE_BOTTOM_SHEET_LAST_CLOSED_DATE] }
        .distinctUntilChanged()

    suspend fun saveUserId(userId: Long) {
        dataStore.edit { pref ->
            pref[Keys.USER_ID] = userId
        }
    }

    suspend fun saveUserName(userName: String) {
        dataStore.edit { pref ->
            pref[Keys.USER_NAME] = userName
        }
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { pref ->
            pref[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun markFortuneCreating() {
        dataStore.edit { pref ->
            pref[Keys.FORTUNE_CREATING] = true
            pref[Keys.FORTUNE_FAILED] = false
        }
    }

    suspend fun markFortuneCreated(fortuneId: Long) {
        dataStore.edit { pref ->
            val isNewForToday = pref[Keys.FORTUNE_ID] != fortuneId || pref[Keys.FORTUNE_DATE] != today()

            pref[Keys.FORTUNE_ID] = fortuneId
            pref[Keys.FORTUNE_DATE] = today()
            pref[Keys.FORTUNE_CREATING] = false
            pref[Keys.FORTUNE_FAILED] = false

            if (isNewForToday) {
                pref[Keys.FORTUNE_SEEN] = false
                pref[Keys.FORTUNE_TOOLTIP_SHOWN] = false
            }
        }
    }

    suspend fun markFortuneFailed() {
        dataStore.edit { pref ->
            pref[Keys.FORTUNE_CREATING] = false
            pref[Keys.FORTUNE_FAILED] = true
        }
    }

    suspend fun markFortuneSeen() {
        dataStore.edit { pref ->
            pref[Keys.FORTUNE_SEEN] = true
        }
    }

    suspend fun markFortuneTooltipShown() {
        dataStore.edit { pref ->
            pref[Keys.FORTUNE_TOOLTIP_SHOWN] = true
        }
    }

    suspend fun saveFortuneImageId(imageResId: Int) {
        dataStore.edit { pref ->
            pref[Keys.FORTUNE_IMAGE_ID] = imageResId
        }
    }

    suspend fun saveFortuneScore(score: Int) {
        dataStore.edit { pref ->
            pref[Keys.FORTUNE_SCORE] = score
        }
    }

    suspend fun markFirstAlarmDismissedToday() {
        dataStore.edit { pref ->
            pref[Keys.FIRST_ALARM_DISMISSED_TODAY] = true
            pref[Keys.FIRST_ALARM_DISMISSED_DATE] = today()
        }
    }

    suspend fun markUpdateBottomSheetDontShow(version: String) {
        dataStore.edit { pref ->
            pref[Keys.UPDATE_BOTTOM_SHEET_DONT_SHOW_VERSION] = version
        }
    }

    suspend fun markUpdateBottomSheetClosedToday() {
        dataStore.edit { pref ->
            pref[Keys.UPDATE_BOTTOM_SHEET_LAST_CLOSED_DATE] = today()
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { pref -> pref.clear() }
    }

    suspend fun clearFortuneData() {
        dataStore.edit { pref ->
            pref.remove(Keys.FORTUNE_ID)
            pref.remove(Keys.FORTUNE_DATE)
            pref.remove(Keys.FORTUNE_IMAGE_ID)
            pref.remove(Keys.FORTUNE_SCORE)
            pref.remove(Keys.FORTUNE_SEEN)
            pref.remove(Keys.FORTUNE_TOOLTIP_SHOWN)
            pref.remove(Keys.FORTUNE_CREATING)
            pref.remove(Keys.FORTUNE_FAILED)
        }
    }
}
