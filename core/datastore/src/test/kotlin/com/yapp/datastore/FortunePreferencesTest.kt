package com.yapp.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class FortunePreferencesTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val fixedZoneOffsetUtc = ZoneOffset.UTC

    private val baseInstantAtT0: Instant = Instant.parse("2025-09-17T00:00:00Z")
    private val baseInstantAtT0Plus2Seconds: Instant = Instant.parse("2025-09-17T00:00:02Z")

    private val fixedClockAtT0: Clock = Clock.fixed(baseInstantAtT0, fixedZoneOffsetUtc)
    private val fixedClockAtT0Plus2Seconds: Clock =
        Clock.fixed(baseInstantAtT0Plus2Seconds, fixedZoneOffsetUtc)

    private val referenceInstantForAnyDay: Instant = Instant.parse("2025-09-17T00:00:00Z")
    private val fixedClockForReferenceDay: Clock =
        Clock.fixed(referenceInstantForAnyDay, fixedZoneOffsetUtc)

    private fun createNewDataStoreWithFile(fileName: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            temporaryFolder.newFile(fileName)
        }

    private fun createFortunePreferencesWithClock(
        dataStore: DataStore<Preferences>,
        fixedClock: Clock,
    ): FortunePreferences = FortunePreferences(dataStore, fixedClock)

    @Test
    fun `운세_생성_상태_Creating_만료_시_Failure로_교정된다`() = runTest {
        // given: t0 시점에서 Creating(lease 1초) 설정
        val dataStoreAtT0 = createNewDataStoreWithFile("prefs_expire.preferences_pb")
        val preferencesAtT0 =
            createFortunePreferencesWithClock(dataStoreAtT0, fixedClockAtT0)

        val generatedAttemptId = UUID.randomUUID().toString()
        preferencesAtT0.markFortuneCreating(attemptId = generatedAttemptId, lease = 1_000L)

        // when: t0 + 2초 경과 후 같은 DataStore를 새로운 Clock으로 읽음
        val preferencesAtT0Plus2Seconds =
            createFortunePreferencesWithClock(dataStoreAtT0, fixedClockAtT0Plus2Seconds)

        // then: Creating → false, Failed → true
        val creating = preferencesAtT0Plus2Seconds.isFortuneCreatingFlow.first()
        assertEquals(false, creating)
        val failed = preferencesAtT0Plus2Seconds.isFortuneFailedFlow.first()
        assertEquals(true, failed)
    }

    @Test
    fun `만료_정보_없는_운세_생성_상태_Creating은_즉시_Failure로_교정된다`() = runTest {
        // given: 과거 버전 데이터 (Creating=true만 존재)
        val dataStoreWithLegacyCreating = createNewDataStoreWithFile("prefs_legacy.preferences_pb")
        val keyCreatingOnly = booleanPreferencesKey("fortune_creating")
        dataStoreWithLegacyCreating.edit { it[keyCreatingOnly] = true }

        val preferencesFromLegacyData =
            createFortunePreferencesWithClock(dataStoreWithLegacyCreating, fixedClockForReferenceDay)

        // when: Failure로 교정 로직이 표시된 Flow 구독 시작

        // then: 즉시 Creating → false, Failed → true
        val creating = preferencesFromLegacyData.isFortuneCreatingFlow.first()
        assertEquals(false, creating)
        val failed = preferencesFromLegacyData.isFortuneFailedFlow.first()
        assertEquals(true, failed)
    }

    @Test
    fun `생성_성공_시_attemptId가_일치할_때만_운세_생성_상태가_Creating에서_Success로_전환된다`() = runTest {
        // given: 운세 Creating 상태
        val dataStore = createNewDataStoreWithFile("prefs_success.preferences_pb")
        val preferences = createFortunePreferencesWithClock(dataStore, fixedClockForReferenceDay)
        val validAttemptId = "ATTEMPT_VALID"
        val invalidAttemptId = "ATTEMPT_INVALID"
        val createdFortuneId = 99L
        preferences.markFortuneCreating(attemptId = validAttemptId, lease = 60_000L)

        // when: 잘못된 attemptId로 생성 성공 처리 시도
        preferences.markFortuneCreatedIfAttemptMatches(
            attemptId = invalidAttemptId,
            fortuneId = createdFortuneId
        )

        // then: 여전히 Creating 상태 (무시됨)
        val stillCreating = preferences.isFortuneCreatingFlow.first()
        assertEquals(true, stillCreating)

        // when: 올바른 attemptId로 생성 성공 처리 시도
        preferences.markFortuneCreatedIfAttemptMatches(
            attemptId = validAttemptId,
            fortuneId = createdFortuneId
        )

        // then: Creating → false, Failed → false, fortuneId 및 날짜 설정
        val creatingAfterSuccess = preferences.isFortuneCreatingFlow.first()
        assertEquals(false, creatingAfterSuccess)
        val failedAfterSuccess = preferences.isFortuneFailedFlow.first()
        assertEquals(false, failedAfterSuccess)
        val savedId = preferences.fortuneIdFlow.first()
        assertEquals(createdFortuneId, savedId)
        val savedEpoch = preferences.fortuneDateEpochFlow.first()
        assertEquals(LocalDate.now(fixedClockForReferenceDay).toEpochDay(), savedEpoch)
    }

    @Test
    fun `운세_생성_실패_시_attemptId가_일치할_때만_Failure로_전환된다`() = runTest {
        // given: 운세 Creating 상태
        val dataStore = createNewDataStoreWithFile("prefs_fail.preferences_pb")
        val preferences = createFortunePreferencesWithClock(dataStore, fixedClockForReferenceDay)
        val validAttemptId = "ATTEMPT_VALID"
        val invalidAttemptId = "ATTEMPT_INVALID"
        preferences.markFortuneCreating(attemptId = validAttemptId, lease = 60_000L)

        // when: 잘못된 attemptId로 실패 처리 시도
        preferences.markFortuneFailedIfAttemptMatches(invalidAttemptId)

        // then: 아직 Creating 상태 (무시됨)
        val stillCreating = preferences.isFortuneCreatingFlow.first()
        assertEquals(true, stillCreating)

        // when: 올바른 attemptId로 실패 처리 시도
        preferences.markFortuneFailedIfAttemptMatches(validAttemptId)

        // then: Creating → false, Failed → true
        val creatingAfterFail = preferences.isFortuneCreatingFlow.first()
        assertEquals(false, creatingAfterFail)
        val failed = preferences.isFortuneFailedFlow.first()
        assertEquals(true, failed)
    }

    @Test
    fun `운세_생성_상태_Creating_만료_시_Success_처리는_거부되고_Failure로_교정된다`() = runTest {
        // given: t0에서 Creating(lease 1초) 설정
        val dataStore = createNewDataStoreWithFile("prefs_expired_success_guard.preferences_pb")
        val prefsAtT0 = createFortunePreferencesWithClock(dataStore, fixedClockAtT0)

        val attemptId = UUID.randomUUID().toString()
        val fortuneId = 999L
        prefsAtT0.markFortuneCreating(attemptId = attemptId, lease = 1_000L)

        // when: t0+2초(만료 이후)로 시계를 바꾸고, 같은 DataStore로 성공 처리 시도
        val prefsAtT0Plus2 = createFortunePreferencesWithClock(dataStore, fixedClockAtT0Plus2Seconds)
        // 만료된 상태이므로, 아래 호출은 내부에서 return@edit 되어 성공 반영이 되면 안 된다.
        prefsAtT0Plus2.markFortuneCreatedIfAttemptMatches(
            attemptId = attemptId,
            fortuneId = fortuneId
        )

        // then: 성공 반영이 거부되었으므로 fortuneId는 여전히 null이어야 한다
        val savedId = prefsAtT0Plus2.fortuneIdFlow.first()
        assertEquals(null, savedId)

        // 그리고 isFortuneCreatingFlow 구독 시 만료 교정 로직이 작동하여
        // CREATING → false, FAILED → true 로 자동 교정되어야 한다.
        val creatingAfter = prefsAtT0Plus2.isFortuneCreatingFlow.first()
        assertEquals(false, creatingAfter)

        val failedAfter = prefsAtT0Plus2.isFortuneFailedFlow.first()
        assertEquals(true, failedAfter)
    }

    @Test
    fun `오늘_운세가_있고_확인한_경우_hasUnseenFortune가_false`() = runTest {
        // given: 오늘 운세가 생성되어 있고(미확인)
        val dataStore = createNewDataStoreWithFile("prefs_seen.preferences_pb")
        val preferences = createFortunePreferencesWithClock(dataStore, fixedClockForReferenceDay)
        val attemptId = "ATTEMPT_FOR_SEEN"
        val fortuneId = 777L
        preferences.markFortuneCreating(attemptId = attemptId, lease = 60_000L)
        preferences.markFortuneCreatedIfAttemptMatches(attemptId = attemptId, fortuneId = fortuneId)

        // when: 사용자가 오늘 운세를 확인
        preferences.markFortuneSeen()

        // then: hasUnseenFortune → false
        val unseen = preferences.hasUnseenFortuneFlow.first()
        assertEquals(false, unseen)
    }

    @Test
    fun `오늘_운세가_있고_아직_확인하지_않은_경우_hasUnseenFortune가_true`() = runTest {
        // given: 오늘 운세가 생성되어 있는 상태(미확인)
        val dataStore = createNewDataStoreWithFile("prefs_unseen.preferences_pb")
        val preferences = createFortunePreferencesWithClock(dataStore, fixedClockForReferenceDay)
        val attemptId = "ATTEMPT_FOR_UNSEEN"
        val fortuneId = 123L
        preferences.markFortuneCreating(attemptId = attemptId, lease = 60_000L)
        preferences.markFortuneCreatedIfAttemptMatches(attemptId = attemptId, fortuneId = fortuneId)

        // when: hasUnseenFortuneFlow 구독

        // then: 오늘 운세 존재 + 아직 읽지 않음 = hasUnseenFortune → true
        val unseen = preferences.hasUnseenFortuneFlow.first()
        assertEquals(true, unseen)
    }

    @Test
    fun `오늘_운세가_있고_Tooltip을_보여주었다면_shouldShowFortuneToolTip이_false`() = runTest {
        // given: 오늘 운세가 생성되어 있는 상태(툴팁 미표시)
        val dataStore = createNewDataStoreWithFile("prefs_tooltip_true.preferences_pb")
        val preferences = createFortunePreferencesWithClock(dataStore, fixedClockForReferenceDay)
        val attemptId = "ATTEMPT_FOR_TOOLTIP_TRUE"
        val fortuneId = 888L
        preferences.markFortuneCreating(attemptId = attemptId, lease = 60_000L)
        preferences.markFortuneCreatedIfAttemptMatches(attemptId = attemptId, fortuneId = fortuneId)

        // when: ToolTip을 보여줌
        preferences.markFortuneTooltipShown()

        // then: shouldShowFortuneToolTip → false
        val showTooltip = preferences.shouldShowFortuneToolTipFlow.first()
        assertEquals(false, showTooltip)
    }

    @Test
    fun `오늘_운세가_있고_Tooltip을_아직_보여주지_않았다면_shouldShowFortuneToolTip이_true`() = runTest {
        // given: 오늘 운세가 생성되어 있는 상태(툴팁 미표시)
        val dataStore = createNewDataStoreWithFile("prefs_tooltip.preferences_pb")
        val preferences = createFortunePreferencesWithClock(dataStore, fixedClockForReferenceDay)
        val attemptId = "ATTEMPT_FOR_TOOLTIP"
        val fortuneId = 456L
        preferences.markFortuneCreating(attemptId = attemptId, lease = 60_000L)
        preferences.markFortuneCreatedIfAttemptMatches(attemptId = attemptId, fortuneId = fortuneId)

        // when: shouldShowFortuneToolTipFlow 구독

        // then: 오늘 운세 존재 + 툴팁 미표시 = shouldShowFortuneToolTip → true
        val showTooltip = preferences.shouldShowFortuneToolTipFlow.first()
        assertEquals(true, showTooltip)
    }
}
