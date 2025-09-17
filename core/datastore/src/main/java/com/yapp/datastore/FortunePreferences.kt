package com.yapp.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FortunePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
) {
    private object Keys {
        val ID = longPreferencesKey("fortune_id")
        val DATE = longPreferencesKey("fortune_date_epoch")
        val IMAGE_ID = intPreferencesKey("fortune_image_id")
        val SCORE = intPreferencesKey("fortune_score")
        val SEEN = booleanPreferencesKey("fortune_seen")
        val TOOLTIP_SHOWN = booleanPreferencesKey("fortune_tooltip_shown")

        val FIRST_ALARM_DISMISSED_TODAY = booleanPreferencesKey("first_alarm_dismissed_today")
        val FIRST_ALARM_DISMISSED_DATE_EPOCH = longPreferencesKey("first_alarm_dismissed_date_epoch")

        val CREATING = booleanPreferencesKey("fortune_creating")
        val FAILED = booleanPreferencesKey("fortune_failed")
    }

    private fun todayEpoch(): Long = LocalDate.now(clock).toEpochDay()

    val fortuneIdFlow: Flow<Long?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.ID] }
        .distinctUntilChanged()

    val fortuneDateEpochFlow: Flow<Long?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.DATE] }
        .distinctUntilChanged()

    val fortuneImageIdFlow: Flow<Int?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.IMAGE_ID] }
        .distinctUntilChanged()

    val fortuneScoreFlow: Flow<Int?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.SCORE] }
        .distinctUntilChanged()

    val hasUnseenFortuneFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { pref ->
            val isToday = pref[Keys.DATE] == todayEpoch()
            isToday && (pref[Keys.ID] != null) && (pref[Keys.SEEN] != true)
        }
        .distinctUntilChanged()

    val shouldShowFortuneToolTipFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { pref ->
            val hasTodayFortune = (pref[Keys.DATE] == todayEpoch()) && (pref[Keys.ID] != null)
            val tooltipShown = pref[Keys.TOOLTIP_SHOWN] ?: false
            hasTodayFortune && !tooltipShown
        }
        .distinctUntilChanged()

    val isFortuneCreatingFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.CREATING] ?: false }
        .distinctUntilChanged()

    val isFortuneFailedFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.FAILED] ?: false }
        .distinctUntilChanged()

    val isFirstAlarmDismissedTodayFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { pref ->
            val flag = pref[Keys.FIRST_ALARM_DISMISSED_TODAY] ?: false
            val isToday = pref[Keys.FIRST_ALARM_DISMISSED_DATE_EPOCH] == todayEpoch()
            flag && isToday
        }
        .distinctUntilChanged()

    suspend fun markFortuneCreating() {
        dataStore.edit { pref ->
            pref[Keys.CREATING] = true
            pref[Keys.FAILED] = false
        }
    }

    suspend fun markFortuneCreated(fortuneId: Long) {
        dataStore.edit { pref ->
            val today = todayEpoch()
            val prevDate = pref[Keys.DATE]
            val isNewForToday = (pref[Keys.ID] != fortuneId) || (prevDate != today)

            pref[Keys.ID] = fortuneId
            pref[Keys.DATE] = today
            pref[Keys.CREATING] = false
            pref[Keys.FAILED] = false

            if (isNewForToday) {
                pref[Keys.SEEN] = false
                pref[Keys.TOOLTIP_SHOWN] = false
            }
        }
    }

    suspend fun markFortuneFailed() {
        dataStore.edit { pref ->
            pref[Keys.CREATING] = false
            pref[Keys.FAILED] = true
        }
    }

    suspend fun markFortuneSeen() {
        dataStore.edit { it[Keys.SEEN] = true }
    }

    suspend fun markFortuneTooltipShown() {
        dataStore.edit { it[Keys.TOOLTIP_SHOWN] = true }
    }

    suspend fun saveFortuneImageId(imageResId: Int) {
        dataStore.edit { it[Keys.IMAGE_ID] = imageResId }
    }

    suspend fun saveFortuneScore(score: Int) {
        dataStore.edit { it[Keys.SCORE] = score }
    }

    suspend fun markFirstAlarmDismissedToday() {
        dataStore.edit { pref ->
            pref[Keys.FIRST_ALARM_DISMISSED_TODAY] = true
            pref[Keys.FIRST_ALARM_DISMISSED_DATE_EPOCH] = todayEpoch()
        }
    }

    suspend fun clearFortuneData() {
        dataStore.edit { pref ->
            pref.remove(Keys.ID)
            pref.remove(Keys.DATE)
            pref.remove(Keys.IMAGE_ID)
            pref.remove(Keys.SCORE)
            pref.remove(Keys.SEEN)
            pref.remove(Keys.TOOLTIP_SHOWN)
            pref.remove(Keys.CREATING)
            pref.remove(Keys.FAILED)
        }
    }
}
