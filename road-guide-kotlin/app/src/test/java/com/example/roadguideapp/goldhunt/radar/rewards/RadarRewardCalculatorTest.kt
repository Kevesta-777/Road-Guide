package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarScanResult
import com.example.roadguideapp.goldhunt.radar.RadarSignalFactory
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarRewardCalculatorTest {
    @Test
    fun calculateForSignal_scalesByRadarTypeStrengthAndRarity() {
        val signal = requireNotNull(
            RadarSignalFactory.treasure(
                treasureId = "treasure-reward-test",
                distanceMeters = 0.0,
                detectionRangeMeters = 800,
                detectedTime = 1_700_000_000_000L,
                radarType = RadarType.TREASURE_RADAR,
            ),
        )
        val reward = RadarRewardCalculator.calculateForSignal(signal, RadarType.TREASURE_RADAR)
        val rarity = RadarTargetRarityResolver.resolve(signal)
        val expectedXp = (
            RadarRewardConfig.baseXp(RadarType.TREASURE_RADAR) *
                RadarRewardConfig.strengthFactor(signal.signalStrength) *
                rarity.xpMultiplier *
                RadarRewardConfig.radarTypeFactor(RadarType.TREASURE_RADAR)
            ).toLong()

        assertEquals(expectedXp, reward.xpAwarded)
        assertEquals(rarity, reward.targetRarity)
        assertTrue(reward.xpAwarded >= 1L)
        assertTrue(reward.creditsAwarded >= 1)
    }

    @Test
    fun calculateForScan_includesPulseRewardsAndStableEventIds() {
        val scannedAtMs = 1_700_000_000_100L
        val signal = requireNotNull(
            RadarSignalFactory.cluster(
                clusterId = "cluster-reward-test",
                distanceMeters = 100.0,
                detectionRangeMeters = 1_500,
                detectedTime = scannedAtMs,
                radarType = RadarType.CLUSTER_RADAR,
            ),
        )
        val scanResult = RadarScanResult(
            latitude = 48.1,
            longitude = 11.5,
            scannedAtMs = scannedAtMs,
            radarType = RadarType.CLUSTER_RADAR,
            detectionRangeMeters = 1_500,
            signals = listOf(signal),
        )

        val bundle = RadarRewardCalculator.calculateForScan(scanResult)
        val grants = RadarRewardDispatcher.grantsFor(bundle)

        assertEquals(1, bundle.detectionCount)
        assertTrue(bundle.totalXp > bundle.signalRewards.single().xpAwarded)
        assertTrue(bundle.totalCredits > bundle.signalRewards.single().creditsAwarded)
        assertEquals(
            "radar:reward:pulse:xp:cluster_radar:$scannedAtMs",
            bundle.pulseXpEventId,
        )
        assertEquals(2, grants.size)
        assertEquals(RewardRuleType.RADAR_DETECTION, grants.first().ruleType)
        assertEquals(RewardRuleType.RADAR_PULSE, grants.last().ruleType)
    }

    @Test
    fun calculateForScan_emptyPulseUsesHalfCompletionRewards() {
        val scannedAtMs = 1_700_000_000_200L
        val scanResult = RadarScanResult(
            latitude = 48.1,
            longitude = 11.5,
            scannedAtMs = scannedAtMs,
            radarType = RadarType.BASIC_RADAR,
            detectionRangeMeters = 800,
            signals = emptyList(),
        )

        val bundle = RadarRewardCalculator.calculateForScan(scanResult)

        assertEquals(0, bundle.detectionCount)
        assertEquals(
            RadarRewardConfig.pulseCompletionXp(RadarType.BASIC_RADAR, hasDetections = false),
            bundle.pulseXp,
        )
        assertEquals(
            RadarRewardConfig.pulseCompletionCredits(RadarType.BASIC_RADAR, hasDetections = false),
            bundle.pulseCredits,
        )
    }
}
