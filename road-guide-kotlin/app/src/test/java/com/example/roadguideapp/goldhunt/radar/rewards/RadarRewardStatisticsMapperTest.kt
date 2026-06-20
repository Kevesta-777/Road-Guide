package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarScanResult
import com.example.roadguideapp.goldhunt.radar.RadarSignalFactory
import com.example.roadguideapp.goldhunt.radar.RadarType
import org.junit.Assert.assertEquals
import org.junit.Test

class RadarRewardStatisticsMapperTest {
    @Test
    fun applyBundle_incrementsOnlyGrantedDetections() {
        val scannedAtMs = 1_700_000_000_300L
        val treasure = requireNotNull(
            RadarSignalFactory.treasure("t-stats", 0.0, 800, scannedAtMs),
        )
        val cluster = requireNotNull(
            RadarSignalFactory.cluster("c-stats", 50.0, 1_500, scannedAtMs),
        )
        val scanResult = RadarScanResult(
            latitude = 48.1,
            longitude = 11.5,
            scannedAtMs = scannedAtMs,
            radarType = RadarType.TREASURE_RADAR,
            detectionRangeMeters = 800,
            signals = listOf(treasure, cluster),
        )
        val bundle = RadarRewardCalculator.calculateForScan(scanResult)
        val granted = listOf(bundle.signalRewards.first())

        val updated = RadarRewardStatisticsMapper.applyBundle(
            current = RadarRewardStatistics.empty(scannedAtMs),
            grantedSignalRewards = granted,
            grantedXp = 12L,
            grantedCredits = 4,
            scanRecorded = true,
            timestampMs = scannedAtMs,
        )

        assertEquals(12L, updated.totalXpEarned)
        assertEquals(4, updated.totalCreditsEarned)
        assertEquals(1, updated.rewardedScans)
        assertEquals(1, updated.rewardedDetections)
        assertEquals(1, updated.treasureDetectionsRewarded)
        assertEquals(0, updated.clusterDetectionsRewarded)
    }

    @Test
    fun applyBundle_skipsUpdateWhenNoGrants() {
        val current = RadarRewardStatistics(
            totalXpEarned = 50L,
            totalCreditsEarned = 10,
            rewardedScans = 3,
            rewardedDetections = 5,
            updatedAtMs = 1_700_000_000_000L,
        )

        val unchanged = RadarRewardStatisticsMapper.applyBundle(
            current = current,
            grantedSignalRewards = emptyList(),
            grantedXp = 0L,
            grantedCredits = 0,
            scanRecorded = false,
            timestampMs = 1_700_000_000_400L,
        )

        assertEquals(current, unchanged)
    }

    @Test
    fun entityRoundTrip_preservesDomainFields() {
        val domain = RadarRewardStatistics(
            totalXpEarned = 100L,
            totalCreditsEarned = 25,
            rewardedScans = 4,
            rewardedDetections = 7,
            treasureDetectionsRewarded = 3,
            clusterDetectionsRewarded = 2,
            secretPlaceDetectionsRewarded = 1,
            storyFragmentDetectionsRewarded = 1,
            legendaryRelicDetectionsRewarded = 0,
            updatedAtMs = 1_700_000_000_500L,
        )

        val roundTrip = RadarRewardStatisticsMapper.toDomain(
            RadarRewardStatisticsMapper.toEntity(domain),
        )

        assertEquals(domain, roundTrip)
    }
}
