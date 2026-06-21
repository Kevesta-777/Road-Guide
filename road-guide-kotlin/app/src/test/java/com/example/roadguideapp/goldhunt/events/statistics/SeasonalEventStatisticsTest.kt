package com.example.roadguideapp.goldhunt.events.statistics

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgress
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardDispatcher
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardGrantResult
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardHookRecordResult
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SeasonalEventStatisticsTest {
    @Test
    fun withTreasureGrant_incrementsJoinedCompletedAndRewards() {
        val outcome = requireNotNull(
            SeasonalEventRewardDispatcher.dispatchTreasureCollection(
                spec = TreasureSpec(
                    treasureId = "tr:event:v1:r:-0.5000:51.4000:winter_crystal:2026:L1:3:4:s0",
                    type = TreasureType.CRYSTAL,
                    lat = 51.45,
                    lng = -0.2,
                    creditAmount = 8,
                    placementKind = "EVENT_WINTER_CRYSTAL",
                    regionL1Id = "L1:3:4",
                ),
                explorerLevel = 10,
                evaluatedAt = LocalDate.of(2026, 12, 10),
            ),
        )
        val grantResult = SeasonalEventRewardGrantResult(
            creditsGranted = 10,
            xpGranted = 20L,
            rewardOutcome = outcome,
            hooks = SeasonalEventRewardHookRecordResult(storyFragmentRecorded = true),
        )
        val allProgress = listOf(
            SeasonalEventProgress(
                eventId = outcome.event.eventId,
                eventType = SeasonalEventType.WINTER_CRYSTAL,
                cycleYear = 2026,
                treasuresCollected = 1,
                xpEarned = 20L,
                creditsEarned = 10,
                fragmentsEarned = 1,
                completionPercent = 100.0,
                isCompleted = true,
            ),
        )

        val next = SeasonalEventStatistics.EMPTY.withTreasureGrant(
            isFirstJoin = true,
            grantResult = grantResult,
            completedCycleDelta = 1,
            allProgress = allProgress,
        )

        assertEquals(1, next.eventsJoined)
        assertEquals(1, next.eventsCompleted)
        assertEquals(1, next.rewardsEarned)
        assertEquals(1, next.treasuresCollected)
        assertEquals(20L, next.xpEarned)
        assertEquals(10, next.creditsEarned)
        assertEquals(1, next.fragmentsEarned)
        assertEquals(SeasonalEventType.WINTER_CRYSTAL, next.bestEventType)
        assertEquals(SeasonalEventType.WINTER_CRYSTAL, next.favoriteEventType)
    }
}
