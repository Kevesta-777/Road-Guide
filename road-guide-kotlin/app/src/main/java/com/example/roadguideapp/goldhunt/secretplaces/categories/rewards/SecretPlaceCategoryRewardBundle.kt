package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategorySchema

/**
 * Resolved reward package for a secret-place discovery or completion event.
 */
internal data class SecretPlaceCategoryRewardBundle(
    val category: SecretPlaceCategory,
    val credits: Int,
    val xp: Long,
    val flowerBonusSlots: Int = 0,
    val crystalBonusSlots: Int = 0,
    val creditBonusMultiplier: Double = 1.0,
    val panoramaBonusKey: String? = null,
    val storyFragmentId: String? = null,
    val achievementKey: String? = null,
    val treasureClusterBonusSlots: Int = 0,
    val legendaryRelicKey: String? = null,
    val effects: Set<SecretPlaceCategoryRewardEffect> = emptySet(),
    val schemaVersion: Int = SecretPlaceCategoryRewardSchema.VERSION,
    val extensionJson: String = SecretPlaceCategoryRewardSchema.EMPTY_EXTENSIONS_JSON,
) {
    val hasFlowerBonus: Boolean get() = flowerBonusSlots > 0
    val hasCrystalBonus: Boolean get() = crystalBonusSlots > 0
    val hasPanoramaBonus: Boolean get() = !panoramaBonusKey.isNullOrBlank()
    val hasStoryFragment: Boolean get() = !storyFragmentId.isNullOrBlank()
    val hasAchievement: Boolean get() = !achievementKey.isNullOrBlank()
    val hasTreasureClusterBonus: Boolean get() = treasureClusterBonusSlots > 0
    val hasLegendaryRelic: Boolean get() = !legendaryRelicKey.isNullOrBlank()

    init {
        require(credits >= 0) { "credits must not be negative" }
        require(xp >= 0L) { "xp must not be negative" }
        require(flowerBonusSlots >= 0) { "flowerBonusSlots must not be negative" }
        require(crystalBonusSlots >= 0) { "crystalBonusSlots must not be negative" }
        require(treasureClusterBonusSlots >= 0) { "treasureClusterBonusSlots must not be negative" }
        require(creditBonusMultiplier > 0.0) { "creditBonusMultiplier must be positive" }
    }
}

internal object SecretPlaceCategoryRewardSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val FLOWER_BONUS = "flowerBonus"
        const val CRYSTAL_BONUS = "crystalBonus"
        const val PANORAMA_BONUS = "panoramaBonus"
        const val STORY_FRAGMENT = "storyFragment"
        const val ACHIEVEMENT = "achievement"
        const val TREASURE_CLUSTER = "treasureCluster"
        const val LEGENDARY_RELIC = "legendaryRelic"
    }
}
