package com.example.roadguideapp.goldhunt.radar.statistics

import com.example.roadguideapp.goldhunt.radar.RadarScanResult
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory

/**
 * Lifetime radar statistics persisted on the explorer profile.
 */
internal data class RadarStatistics(
    val totalScans: Int = 0,
    val successfulScans: Int = 0,
    val treasuresFound: Int = 0,
    val clustersFound: Int = 0,
    val secretPlacesFound: Int = 0,
    val storyDiscoveries: Int = 0,
    val legendaryDiscoveries: Int = 0,
) {
    val successRate: Double
        get() = if (totalScans <= 0) 0.0 else successfulScans.toDouble() / totalScans.toDouble()

    fun withScan(scanResult: RadarScanResult): RadarStatistics {
        val categoryCounts = scanResult.signals
            .groupingBy { it.targetCategory }
            .eachCount()
        return copy(
            totalScans = totalScans + 1,
            successfulScans = successfulScans + if (scanResult.hasDetections) 1 else 0,
            treasuresFound = treasuresFound + categoryCounts.countFor(RadarTargetCategory.TREASURE),
            clustersFound = clustersFound + categoryCounts.countFor(RadarTargetCategory.CLUSTER),
            secretPlacesFound = secretPlacesFound + categoryCounts.countFor(RadarTargetCategory.SECRET_PLACE),
            storyDiscoveries = storyDiscoveries + categoryCounts.countFor(RadarTargetCategory.STORY_FRAGMENT),
            legendaryDiscoveries = legendaryDiscoveries +
                categoryCounts.countFor(RadarTargetCategory.LEGENDARY_RELIC),
        )
    }

    fun detectionCountFor(category: RadarTargetCategory): Int = when (category) {
        RadarTargetCategory.TREASURE -> treasuresFound
        RadarTargetCategory.CLUSTER -> clustersFound
        RadarTargetCategory.SECRET_PLACE -> secretPlacesFound
        RadarTargetCategory.STORY_FRAGMENT -> storyDiscoveries
        RadarTargetCategory.LEGENDARY_RELIC -> legendaryDiscoveries
    }

    fun detectionEntriesOrdered(): List<Pair<RadarTargetCategory, Int>> =
        RadarTargetCategory.ALL_ORDERED.map { it to detectionCountFor(it) }

    companion object {
        val EMPTY = RadarStatistics()
    }
}

private fun Map<RadarTargetCategory, Int>.countFor(category: RadarTargetCategory): Int =
    get(category) ?: 0
