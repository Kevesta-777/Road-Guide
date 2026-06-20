package com.example.roadguideapp.goldhunt.radar

/**
 * Canonical explorer radar families (offline tuning).
 *
 * [unlockLevel] is the minimum explorer level required to equip the radar.
 * [detectionRangeMeters] is the maximum scan radius for a single pulse.
 * [cooldownSeconds] is the minimum wait between pulses.
 *
 * Achievement, seasonal event, and legendary relic hook keys are resolved in [RadarTemplate].
 */
internal enum class RadarType(
    val displayName: String,
    val unlockLevel: Int,
    val detectionRangeMeters: Int,
    val cooldownSeconds: Int,
) {
    BASIC_RADAR(
        displayName = "Basic Radar",
        unlockLevel = 1,
        detectionRangeMeters = 500,
        cooldownSeconds = 60,
    ),
    TREASURE_RADAR(
        displayName = "Treasure Radar",
        unlockLevel = 3,
        detectionRangeMeters = 800,
        cooldownSeconds = 45,
    ),
    SECRET_PLACE_RADAR(
        displayName = "Secret Place Radar",
        unlockLevel = 5,
        detectionRangeMeters = 1_200,
        cooldownSeconds = 90,
    ),
    CLUSTER_RADAR(
        displayName = "Cluster Radar",
        unlockLevel = 7,
        detectionRangeMeters = 1_500,
        cooldownSeconds = 120,
    ),
    STORY_RADAR(
        displayName = "Story Radar",
        unlockLevel = 10,
        detectionRangeMeters = 2_000,
        cooldownSeconds = 180,
    ),
    LEGENDARY_RADAR(
        displayName = "Legendary Radar",
        unlockLevel = 15,
        detectionRangeMeters = 3_000,
        cooldownSeconds = 300,
    ),
    ;

    /** Stable persistence / catalog identifier (enum name). */
    val id: String get() = name

    companion object {
        /** Display and unlock order from introductory to legendary. */
        val ALL_ORDERED: List<RadarType> = listOf(
            BASIC_RADAR,
            TREASURE_RADAR,
            SECRET_PLACE_RADAR,
            CLUSTER_RADAR,
            STORY_RADAR,
            LEGENDARY_RADAR,
        )

        fun fromId(id: String): RadarType? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    init {
        require(unlockLevel >= 1) { "$name unlockLevel must be at least 1" }
        require(detectionRangeMeters > 0) { "$name detectionRangeMeters must be positive" }
        require(cooldownSeconds > 0) { "$name cooldownSeconds must be positive" }
    }
}
