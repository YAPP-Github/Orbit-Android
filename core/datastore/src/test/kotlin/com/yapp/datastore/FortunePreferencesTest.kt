package com.yapp.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class FortunePreferencesTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val fixedZoneOffsetUtc = ZoneOffset.UTC

    private val baseInstantAtT0: Instant = Instant.parse("2025-09-17T00:00:00Z")
    private val nextDayInstant: Instant = Instant.parse("2025-09-18T00:00:00Z")

    private val fixedClockAtT0: Clock = Clock.fixed(baseInstantAtT0, fixedZoneOffsetUtc)
    private val fixedClockNextDay: Clock = Clock.fixed(nextDayInstant, fixedZoneOffsetUtc)

    private fun createNewDataStoreWithFile(fileName: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            temporaryFolder.newFile(fileName)
        }

    private fun createFortunePreferencesWithClock(
        dataStore: DataStore<Preferences>,
        fixedClock: Clock,
    ): FortunePreferences = FortunePreferences(dataStore, fixedClock)

    @Test
    fun `오늘_운세를_생성했다면_hasTodayFortune이_참이다`() = runTest {
        // given
        val dataStore = createNewDataStoreWithFile("today.preferences_pb")
        val preferences = createFortunePreferencesWithClock(dataStore, fixedClockAtT0)

        // when
        preferences.markFortuneCreated(fortuneId = 1L)

        // then
        val result = preferences.hasTodayFortune()
        assertTrue(result)
    }

    @Test
    fun `운세_생성일과_현재_날짜가_다르면_hasTodayFortune이_거짓이다`() = runTest {
        // given
        val dataStore = createNewDataStoreWithFile("yesterday.preferences_pb")
        val preferencesAtT0 = createFortunePreferencesWithClock(dataStore, fixedClockAtT0)
        val preferencesNextDay = createFortunePreferencesWithClock(dataStore, fixedClockNextDay)

        // when
        preferencesAtT0.markFortuneCreated(fortuneId = 1L)

        // then
        val result = preferencesNextDay.hasTodayFortune()
        assertFalse(result)
    }
}
