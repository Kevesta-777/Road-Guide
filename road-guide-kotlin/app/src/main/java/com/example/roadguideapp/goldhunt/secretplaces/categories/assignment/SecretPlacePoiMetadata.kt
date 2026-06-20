package com.example.roadguideapp.goldhunt.secretplaces.categories.assignment

/**
 * Normalized POI context for category assignment (OpenMapTiles / Pelias tags).
 * Tags are lower-cased and de-duplicated for deterministic matching.
 */
internal data class SecretPlacePoiMetadata(
    val tags: Set<String> = emptySet(),
    val nearestPoiDistanceMeters: Double? = null,
    val primaryTag: String? = null,
) {
    val poiSignalStrength: Double
        get() = when {
            tags.isEmpty() -> 0.0
            nearestPoiDistanceMeters == null -> 0.55
            nearestPoiDistanceMeters <= 25.0 -> 1.0
            nearestPoiDistanceMeters <= 80.0 -> 0.75
            nearestPoiDistanceMeters <= 200.0 -> 0.45
            else -> 0.2
        }

    fun resolveEnvironmentType(): SecretPlaceEnvironmentType {
        if (tags.isEmpty()) return SecretPlaceEnvironmentType.UNKNOWN
        val normalized = tags.map { it.lowercase() }.toSet()
        return when {
            normalized.any { it in MYSTICAL_TAGS } -> SecretPlaceEnvironmentType.MYSTICAL
            normalized.any { it in WILDERNESS_TAGS } -> SecretPlaceEnvironmentType.WILDERNESS
            normalized.any { it in HISTORIC_TAGS } -> SecretPlaceEnvironmentType.HISTORIC
            normalized.any { it in WATERFRONT_TAGS } -> SecretPlaceEnvironmentType.WATERFRONT
            normalized.any { it in NATURAL_TAGS } -> SecretPlaceEnvironmentType.NATURAL
            normalized.any { it in URBAN_TAGS } -> SecretPlaceEnvironmentType.URBAN
            normalized.any { it in SUBURBAN_TAGS } -> SecretPlaceEnvironmentType.SUBURBAN
            else -> SecretPlaceEnvironmentType.UNKNOWN
        }
    }

    fun categoryAffinityMultiplier(categoryId: String): Double {
        if (tags.isEmpty()) return 1.0
        val normalized = tags.map { it.lowercase() }.toSet()
        val signal = poiSignalStrength
        val raw = when (categoryId) {
            "NATURE_SANCTUARY" -> tagBoost(normalized, NATURE_POI_TAGS, 2.2)
            "SCENIC_VIEWPOINT" -> tagBoost(normalized, VIEWPOINT_POI_TAGS, 2.4)
            "HISTORIC_LANDMARK" -> tagBoost(normalized, HISTORIC_TAGS, 2.5)
            "MYSTERY_ZONE" -> tagBoost(normalized, MYSTERY_POI_TAGS, 2.0)
            "ANCIENT_RELIC_SITE" -> tagBoost(normalized, RELIC_POI_TAGS, 2.3)
            "EXPLORER_HIDEOUT" -> tagBoost(normalized, HIDEOUT_POI_TAGS, 1.9)
            "MYTHICAL_PLACE" -> tagBoost(normalized, MYSTICAL_TAGS, 2.1)
            "LEGENDARY_SITE" -> tagBoost(normalized, LEGENDARY_POI_TAGS, 1.8)
            else -> 1.0
        }
        return 1.0 + (raw - 1.0) * signal
    }

    companion object {
        val EMPTY = SecretPlacePoiMetadata()

        fun fromTags(tags: Collection<String>): SecretPlacePoiMetadata {
            val normalized = tags
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toSet()
            val primary = normalized.firstOrNull()
            return SecretPlacePoiMetadata(
                tags = normalized,
                primaryTag = primary,
            )
        }

        private val NATURAL_TAGS = setOf(
            "park", "forest", "wood", "nature_reserve", "grass", "meadow", "garden",
        )
        private val WATERFRONT_TAGS = setOf(
            "beach", "water", "bay", "coastline", "marina", "pier", "waterfall",
        )
        private val HISTORIC_TAGS = setOf(
            "monument", "memorial", "castle", "ruins", "archaeological_site", "historic",
            "museum", "heritage", "fort",
        )
        private val WILDERNESS_TAGS = setOf(
            "cave_entrance", "peak", "ridge", "scrub", "heath", "wetland", "marsh",
        )
        private val MYSTICAL_TAGS = setOf(
            "stone", "megalith", "standing_stone", "shrine", "temple", "sacred",
            "labyrinth", "obelisk",
        )
        private val URBAN_TAGS = setOf(
            "commercial", "retail", "office", "apartments", "stadium", "theatre",
        )
        private val SUBURBAN_TAGS = setOf(
            "residential", "neighbourhood", "suburb", "village",
        )
        private val NATURE_POI_TAGS = NATURAL_TAGS + setOf("tree", "orchard")
        private val VIEWPOINT_POI_TAGS = setOf("viewpoint", "peak", "tower", "observation")
        private val MYSTERY_POI_TAGS = setOf("abandoned", "ruins", "yes", "mystery", "tunnel")
        private val RELIC_POI_TAGS = HISTORIC_TAGS + MYSTICAL_TAGS
        private val HIDEOUT_POI_TAGS = setOf("shelter", "hut", "cabin", "camp_site", "cave_entrance")
        private val LEGENDARY_POI_TAGS = MYSTICAL_TAGS + setOf("artwork", "attraction", "theme_park")

        private fun tagBoost(tags: Set<String>, matchers: Set<String>, peak: Double): Double {
            val hits = tags.count { tag -> matchers.any { matcher -> tag.contains(matcher) } }
            if (hits <= 0) return 1.0
            return 1.0 + (peak - 1.0) * (hits.coerceAtMost(3) / 3.0)
        }
    }
}
