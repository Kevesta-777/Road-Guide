package com.example.roadguideapp.goldhunt.panorama

/**
 * Canonical panorama hunt families (offline tuning).
 *
 * [difficulty] ranks challenge level (1 = easiest).
 * [rewardMultiplier] scales base rewards for hunt completion.
 *
 * Nullable keys reserve hooks for story fragments, achievements, and legendary relics.
 * Hunt generation, UI, and persistence are implemented elsewhere.
 */
internal enum class PanoramaHuntType(
    val displayName: String,
    val difficulty: Int,
    val rewardMultiplier: Double,
    val achievementKey: String? = null,
    val storyFragmentKey: String? = null,
    val legendaryRelicKey: String? = null,
) {
    HIDDEN_SYMBOL(
        displayName = "Hidden Symbol",
        difficulty = 1,
        rewardMultiplier = 1.0,
        achievementKey = "panorama_hunt_hidden_symbol",
    ),
    OBJECT_HUNT(
        displayName = "Object Hunt",
        difficulty = 2,
        rewardMultiplier = 1.15,
        achievementKey = "panorama_hunt_object_hunt",
    ),
    PANORAMA_PUZZLE(
        displayName = "Panorama Puzzle",
        difficulty = 3,
        rewardMultiplier = 1.3,
        achievementKey = "panorama_hunt_panorama_puzzle",
        storyFragmentKey = "story_fragment_panorama_puzzle",
    ),
    SECRET_CODE(
        displayName = "Secret Code",
        difficulty = 4,
        rewardMultiplier = 1.5,
        achievementKey = "panorama_hunt_secret_code",
        storyFragmentKey = "story_fragment_secret_code",
    ),
    RELIC_HUNT(
        displayName = "Relic Hunt",
        difficulty = 5,
        rewardMultiplier = 2.0,
        achievementKey = "panorama_hunt_relic_hunt",
        storyFragmentKey = "story_fragment_relic_hunt",
        legendaryRelicKey = "legendary_relic_panorama_hunt",
    ),
    ;

    /** Stable persistence / catalog identifier (enum name). */
    val id: String get() = name

    companion object {
        /** Display and catalog order from easiest to hardest. */
        val ALL_ORDERED: List<PanoramaHuntType> = listOf(
            HIDDEN_SYMBOL,
            OBJECT_HUNT,
            PANORAMA_PUZZLE,
            SECRET_CODE,
            RELIC_HUNT,
        )

        fun fromId(id: String): PanoramaHuntType? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    init {
        require(difficulty > 0) {
            "$name difficulty must be positive"
        }
        require(rewardMultiplier > 0.0) {
            "$name rewardMultiplier must be positive"
        }
    }
}
