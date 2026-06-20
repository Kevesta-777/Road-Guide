package com.example.roadguideapp.goldhunt.secretplaces.categories

/**
 * Canonical secret-place category families (offline tuning).
 *
 * [rarityWeight] drives future weighted spawn rolls (higher = more common).
 * [difficultyLevel] ranks discovery / completion challenge (1 = easiest).
 * [rewardMultiplier] scales base rewards for visits and collections at this category.
 *
 * Nullable keys reserve hooks for story fragments, panorama hunts, achievements,
 * and legendary relics. Generation, UI, and persistence are implemented elsewhere.
 */
internal enum class SecretPlaceCategory(
    val displayName: String,
    val rarityWeight: Int,
    val difficultyLevel: Int,
    val rewardMultiplier: Double,
    val achievementKey: String? = null,
    val storyFragmentKey: String? = null,
    val panoramaHuntKey: String? = null,
    val legendaryRelicKey: String? = null,
) {
    NATURE_SANCTUARY(
        displayName = "Nature Sanctuary",
        rarityWeight = 380,
        difficultyLevel = 1,
        rewardMultiplier = 1.0,
    ),
    SCENIC_VIEWPOINT(
        displayName = "Scenic Viewpoint",
        rarityWeight = 320,
        difficultyLevel = 2,
        rewardMultiplier = 1.1,
        panoramaHuntKey = "panorama_hunt_scenic_viewpoint",
    ),
    HISTORIC_LANDMARK(
        displayName = "Historic Landmark",
        rarityWeight = 200,
        difficultyLevel = 3,
        rewardMultiplier = 1.25,
        achievementKey = "secret_place_historic_landmark",
        storyFragmentKey = "story_fragment_historic_landmark",
    ),
    MYSTERY_ZONE(
        displayName = "Mystery Zone",
        rarityWeight = 120,
        difficultyLevel = 4,
        rewardMultiplier = 1.4,
        achievementKey = "secret_place_mystery_zone",
    ),
    ANCIENT_RELIC_SITE(
        displayName = "Ancient Relic Site",
        rarityWeight = 80,
        difficultyLevel = 5,
        rewardMultiplier = 1.6,
        achievementKey = "secret_place_ancient_relic_site",
        storyFragmentKey = "story_fragment_ancient_relic_site",
    ),
    EXPLORER_HIDEOUT(
        displayName = "Explorer Hideout",
        rarityWeight = 45,
        difficultyLevel = 6,
        rewardMultiplier = 1.85,
        achievementKey = "secret_place_explorer_hideout",
        panoramaHuntKey = "panorama_hunt_explorer_hideout",
    ),
    MYTHICAL_PLACE(
        displayName = "Mythical Place",
        rarityWeight = 20,
        difficultyLevel = 7,
        rewardMultiplier = 2.2,
        achievementKey = "secret_place_mythical_place",
        storyFragmentKey = "story_fragment_mythical_place",
        panoramaHuntKey = "panorama_hunt_mythical_place",
    ),
    LEGENDARY_SITE(
        displayName = "Legendary Site",
        rarityWeight = 8,
        difficultyLevel = 8,
        rewardMultiplier = 3.0,
        achievementKey = "secret_place_legendary_site",
        storyFragmentKey = "story_fragment_legendary_site",
        panoramaHuntKey = "panorama_hunt_legendary_site",
        legendaryRelicKey = "legendary_relic_legendary_site",
    ),
    ;

    /** Stable persistence / catalog identifier (enum name). */
    val id: String get() = name

    companion object {
        /** Display and catalog order from common to legendary. */
        val ALL_ORDERED: List<SecretPlaceCategory> = listOf(
            NATURE_SANCTUARY,
            SCENIC_VIEWPOINT,
            HISTORIC_LANDMARK,
            MYSTERY_ZONE,
            ANCIENT_RELIC_SITE,
            EXPLORER_HIDEOUT,
            MYTHICAL_PLACE,
            LEGENDARY_SITE,
        )

        fun fromId(id: String): SecretPlaceCategory? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    init {
        require(rarityWeight > 0) {
            "$name rarityWeight must be positive"
        }
        require(difficultyLevel > 0) {
            "$name difficultyLevel must be positive"
        }
        require(rewardMultiplier > 0.0) {
            "$name rewardMultiplier must be positive"
        }
    }
}
