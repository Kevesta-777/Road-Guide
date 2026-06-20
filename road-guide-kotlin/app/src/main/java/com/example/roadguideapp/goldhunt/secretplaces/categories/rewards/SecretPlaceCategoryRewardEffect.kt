package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

/**
 * Reward effect families granted by secret-place categories.
 * Dispatchers and future unlockers consume these flags without schema changes.
 */
internal enum class SecretPlaceCategoryRewardEffect {
    CREDITS,
    XP,
    FLOWER_BONUS,
    CRYSTAL_BONUS,
    PANORAMA_BONUS,
    STORY_FRAGMENT,
    ACHIEVEMENT,
    TREASURE_CLUSTER,
    LEGENDARY_RELIC,
}
