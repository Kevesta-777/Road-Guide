package com.example.roadguideapp.goldhunt.relics.sources

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

/**
 * Domain gameplay events that may grant relic piece progress.
 */
internal sealed class RelicPieceSourceEvent {
    abstract val timestampMs: Long

    data class SecretPlaceDiscovered(
        val category: SecretPlaceCategory,
        val secretPlaceId: String,
        override val timestampMs: Long,
    ) : RelicPieceSourceEvent()

    data class TreasureClusterCompleted(
        val clusterType: ClusterType,
        val clusterId: String,
        override val timestampMs: Long,
    ) : RelicPieceSourceEvent()

    data class StoryFragmentUnlocked(
        val storyFragmentKey: String,
        override val timestampMs: Long,
    ) : RelicPieceSourceEvent()

    data class PanoramaHuntCompleted(
        val huntType: PanoramaHuntType,
        val huntId: String,
        override val timestampMs: Long,
    ) : RelicPieceSourceEvent()

    data class SeasonalEventGranted(
        val eventType: SeasonalEventType,
        val grantKey: String,
        override val timestampMs: Long,
    ) : RelicPieceSourceEvent()

    data class AchievementCompleted(
        val achievementKey: String,
        override val timestampMs: Long,
    ) : RelicPieceSourceEvent()

    data class RadarDiscovery(
        val radarType: RadarType,
        val targetId: String,
        override val timestampMs: Long,
    ) : RelicPieceSourceEvent()
}
