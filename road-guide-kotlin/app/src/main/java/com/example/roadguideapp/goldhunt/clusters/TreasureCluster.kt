package com.example.roadguideapp.goldhunt.clusters

import org.maplibre.android.geometry.LatLng

/**
 * Runtime model for a procedurally placed treasure cluster instance.
 *
 * Tracks discovery and per-treasure collection progress. Nullable hooks wire future
 * secret-place links, achievements, story fragments, and legendary relics without
 * requiring schema changes on [ClusterType].
 */
internal data class TreasureCluster(
    val clusterId: String,
    val clusterType: ClusterType,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Double,
    val treasureCount: Int,
    val clusterSeed: Long,
    val discovered: Boolean = false,
    val collectedCount: Int = 0,
    val secretPlaceId: String? = null,
    val storyFragmentId: String? = null,
    val achievementKey: String? = null,
    val legendaryRelicKey: String? = null,
    val schemaVersion: Int = ClusterSchema.VERSION,
    val extensionJson: String = ClusterSchema.EMPTY_EXTENSIONS_JSON,
) {
    val remainingCount: Int
        get() = (treasureCount - collectedCount).coerceAtLeast(0)

    val isComplete: Boolean
        get() = treasureCount > 0 && collectedCount >= treasureCount

    val centerLatLng: LatLng
        get() = LatLng(centerLatitude, centerLongitude)

    /** Instance override, then cluster-family default from [ClusterType]. */
    val resolvedStoryFragmentId: String?
        get() = storyFragmentId ?: clusterType.storyFragmentKey

    /** Instance override, then cluster-family default from [ClusterType]. */
    val resolvedAchievementKey: String?
        get() = achievementKey ?: clusterType.achievementKey

    val hasSecretPlaceLink: Boolean
        get() = !secretPlaceId.isNullOrBlank()

    val hasLegendaryRelicHook: Boolean
        get() = !legendaryRelicKey.isNullOrBlank()

    init {
        require(clusterId.isNotBlank()) { "clusterId must not be blank" }
        require(treasureCount > 0) { "treasureCount must be positive" }
        require(radiusMeters > 0.0) { "radiusMeters must be positive" }
        require(collectedCount >= 0) { "collectedCount must not be negative" }
        require(collectedCount <= treasureCount) {
            "collectedCount must not exceed treasureCount"
        }
        require(centerLatitude in -90.0..90.0) { "centerLatitude out of range" }
        require(centerLongitude in -180.0..180.0) { "centerLongitude out of range" }
    }

    fun withDiscovered(discovered: Boolean = true): TreasureCluster =
        copy(discovered = discovered)

    fun withCollectedCount(count: Int): TreasureCluster =
        copy(collectedCount = count.coerceIn(0, treasureCount))

    fun incrementCollectedCount(): TreasureCluster =
        withCollectedCount(collectedCount + 1)
}
