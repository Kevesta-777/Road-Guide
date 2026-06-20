package com.example.roadguideapp.goldhunt.secretplaces

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategoryCatalog
import org.maplibre.android.geometry.LatLng

/**
 * Runtime model for a secret-place instance.
 *
 * Tracks discovery and completion progress. Nullable hooks wire explorer-level gates,
 * linked treasure clusters, story fragments, and legendary relics without requiring
 * schema changes on [SecretPlaceCategory].
 */
internal data class SecretPlace(
    val secretPlaceId: String,
    val category: SecretPlaceCategory,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val rarity: SecretPlaceRarity,
    val discovered: Boolean = false,
    val completed: Boolean = false,
    val discoveryDate: Long? = null,
    val completionDate: Long? = null,
    val rewardSeed: Long,
    val minimumExplorerLevel: Int? = null,
    val linkedClusterIds: List<String> = emptyList(),
    val storyFragmentId: String? = null,
    val legendaryRelicKey: String? = null,
    val achievementKey: String? = null,
    val schemaVersion: Int = SecretPlaceSchema.VERSION,
    val extensionJson: String = SecretPlaceSchema.EMPTY_EXTENSIONS_JSON,
) {
    val latLng: LatLng
        get() = LatLng(latitude, longitude)

    /** Instance override, then category difficulty gate. */
    val resolvedMinimumExplorerLevel: Int
        get() = minimumExplorerLevel
            ?: SecretPlaceSchema.defaultMinimumExplorerLevel(category)

    /** Instance override, then category-family default. */
    val resolvedStoryFragmentId: String?
        get() = storyFragmentId ?: category.storyFragmentKey

    /** Instance override, then category-family default. */
    val resolvedLegendaryRelicKey: String?
        get() = legendaryRelicKey ?: category.legendaryRelicKey

    /** Instance override, then category-family default. */
    val resolvedAchievementKey: String?
        get() = achievementKey ?: category.achievementKey

    val hasClusterLinks: Boolean
        get() = linkedClusterIds.isNotEmpty()

    val hasStoryFragmentHook: Boolean
        get() = !resolvedStoryFragmentId.isNullOrBlank()

    val hasLegendaryRelicHook: Boolean
        get() = !resolvedLegendaryRelicKey.isNullOrBlank()

    val effectiveRewardMultiplier: Double
        get() = category.rewardMultiplier * rarity.rewardMultiplier

    init {
        require(secretPlaceId.isNotBlank()) { "secretPlaceId must not be blank" }
        require(latitude in -90.0..90.0) { "latitude out of range" }
        require(longitude in -180.0..180.0) { "longitude out of range" }
        require(radiusMeters > 0.0) { "radiusMeters must be positive" }
        require(minimumExplorerLevel == null || minimumExplorerLevel > 0) {
            "minimumExplorerLevel must be positive when set"
        }
        require(linkedClusterIds.all { it.isNotBlank() }) {
            "linkedClusterIds must not contain blank ids"
        }
        if (completed) {
            require(discovered) { "completed secret places must be discovered" }
        }
        if (discoveryDate != null) {
            require(discoveryDate > 0L) { "discoveryDate must be positive when set" }
        }
        if (completionDate != null) {
            require(completionDate > 0L) { "completionDate must be positive when set" }
        }
    }

    fun isAccessibleAtLevel(explorerLevel: Int): Boolean =
        SecretPlaceExplorerLevelGate.isAccessible(this, explorerLevel)

    fun withDiscovered(
        discovered: Boolean = true,
        discoveryDate: Long? = this.discoveryDate,
    ): SecretPlace = copy(
        discovered = discovered,
        discoveryDate = discoveryDate,
        completed = if (!discovered) false else completed,
        completionDate = if (!discovered) null else completionDate,
    )

    fun withCompleted(
        completed: Boolean = true,
        completionDate: Long? = this.completionDate,
    ): SecretPlace {
        val nextDiscovered = discovered || completed
        val nextDiscoveryDate = when {
            !nextDiscovered -> null
            discoveryDate != null -> discoveryDate
            completed && completionDate != null -> completionDate
            else -> discoveryDate
        }
        return copy(
            discovered = nextDiscovered,
            discoveryDate = nextDiscoveryDate,
            completed = completed,
            completionDate = if (completed) completionDate else null,
        )
    }

    fun withLinkedClusterId(clusterId: String): SecretPlace {
        require(clusterId.isNotBlank()) { "clusterId must not be blank" }
        if (clusterId in linkedClusterIds) return this
        return copy(linkedClusterIds = linkedClusterIds + clusterId)
    }

    companion object {
        fun fromIdParts(
            secretPlaceId: String,
            categoryId: String,
            latitude: Double,
            longitude: Double,
            radiusMeters: Double,
            rarity: SecretPlaceRarity,
            rewardSeed: Long,
            discovered: Boolean = false,
            completed: Boolean = false,
            discoveryDate: Long? = null,
            completionDate: Long? = null,
        ): SecretPlace? {
            val category = SecretPlaceCategoryCatalog.fromId(categoryId) ?: return null
            return SecretPlace(
                secretPlaceId = secretPlaceId,
                category = category,
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters,
                rarity = rarity,
                discovered = discovered,
                completed = completed,
                discoveryDate = discoveryDate,
                completionDate = completionDate,
                rewardSeed = rewardSeed,
            )
        }
    }
}
