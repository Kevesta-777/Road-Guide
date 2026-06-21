package com.example.roadguideapp.goldhunt.secretplaces

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

/**
 * Stable identifiers for secret-place instances.
 * Generation consumes these builders; this module only defines the format.
 */
internal object SecretPlaceId {
    private val PARSE_REGEX = Regex(
        "^sp:v(\\d+):([^:]+):([^:]+):seed(\\d+)$",
    )

    data class Parsed(
        val playRegionId: String,
        val categoryId: String,
        val rewardSeed: Long,
    )

    fun forCategoryAnchor(
        playRegionId: String,
        category: SecretPlaceCategory,
        rewardSeed: Long,
    ): String = "sp:v${SecretPlaceSchema.VERSION}:$playRegionId:${category.id}:seed$rewardSeed"

    fun forCatalog(
        catalogId: String,
        rewardSeed: Long,
    ): String = "sp:v${SecretPlaceSchema.VERSION}:cat:$catalogId:seed$rewardSeed"

    fun parse(secretPlaceId: String): Parsed? {
        val match = PARSE_REGEX.matchEntire(secretPlaceId) ?: return null
        return Parsed(
            playRegionId = match.groupValues[2],
            categoryId = match.groupValues[3],
            rewardSeed = match.groupValues[4].toLong(),
        )
    }
}
