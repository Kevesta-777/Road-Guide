package com.example.roadguideapp.goldhunt.treasure.metadata

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpValues
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureTypeRarity

/**
 * Canonical treasure metadata catalog (offline).
 * Generators and reward dispatchers remain unchanged until explicitly integrated.
 */
internal object TreasureDefinitionCatalog {
    val GoldStar = TreasureTypeTemplate(
        key = TreasureDefinitionKey.GOLD_STAR,
        displayName = "Gold Star",
        treasureType = TreasureType.STAR,
        rarity = TreasureRarity.COMMON,
        baseCreditReward = GoldHuntConfig.TREASURE_STAR_CREDIT_MIN,
        baseXpReward = GameplayXpValues.TREASURE_STAR,
        category = TreasureDefinitionCategory.STANDARD_COLLECTIBLE,
    )

    val Flower = TreasureTypeTemplate(
        key = TreasureDefinitionKey.FLOWER,
        displayName = "Flower",
        treasureType = TreasureType.FLOWER,
        rarity = TreasureRarity.UNCOMMON,
        baseCreditReward = GoldHuntConfig.TREASURE_FLOWER_CREDIT_MIN,
        baseXpReward = GameplayXpValues.TREASURE_FLOWER,
        category = TreasureDefinitionCategory.STANDARD_COLLECTIBLE,
    )

    val Crystal = TreasureTypeTemplate(
        key = TreasureDefinitionKey.CRYSTAL,
        displayName = "Crystal",
        treasureType = TreasureType.CRYSTAL,
        rarity = TreasureRarity.RARE,
        baseCreditReward = GoldHuntConfig.TREASURE_CRYSTAL_CREDIT_MIN,
        baseXpReward = GameplayXpValues.TREASURE_CRYSTAL,
        category = TreasureDefinitionCategory.STANDARD_COLLECTIBLE,
    )

    val GiftBox = TreasureTypeTemplate(
        key = TreasureDefinitionKey.GIFT_BOX,
        displayName = "Gift Box",
        treasureType = TreasureType.GIFT,
        rarity = TreasureRarity.EPIC,
        baseCreditReward = GoldHuntConfig.TREASURE_GIFT_CREDIT_MIN,
        baseXpReward = GameplayXpValues.TREASURE_GIFT,
        category = TreasureDefinitionCategory.STANDARD_COLLECTIBLE,
    )

    /** Reserved template — not spawnable via current [TreasureType] picker. */
    val LegendaryRelic = TreasureTypeTemplate(
        key = TreasureDefinitionKey.LEGENDARY_RELIC,
        displayName = "Legendary Relic",
        treasureType = null,
        rarity = TreasureRarity.LEGENDARY,
        baseCreditReward = 0,
        baseXpReward = 0L,
        category = TreasureDefinitionCategory.LEGENDARY_RELIC,
        enabled = false,
    )

    /** Reserved template for narrative collectibles. */
    val StoryFragment = TreasureTypeTemplate(
        key = TreasureDefinitionKey.STORY_FRAGMENT,
        displayName = "Story Fragment",
        treasureType = null,
        rarity = TreasureRarity.RARE,
        baseCreditReward = 0,
        baseXpReward = 0L,
        category = TreasureDefinitionCategory.STORY_FRAGMENT,
        enabled = false,
    )

    /** Reserved template for secret-place-linked rewards. */
    val SecretPlaceReward = TreasureTypeTemplate(
        key = TreasureDefinitionKey.SECRET_PLACE_REWARD,
        displayName = "Secret Place Reward",
        treasureType = null,
        rarity = TreasureRarity.EPIC,
        baseCreditReward = 0,
        baseXpReward = GameplayXpValues.SECRET_PLACE,
        category = TreasureDefinitionCategory.SECRET_PLACE_REWARD,
        enabled = false,
    )

    private val templatesByKey: Map<TreasureDefinitionKey, TreasureTypeTemplate> = mapOf(
        TreasureDefinitionKey.GOLD_STAR to GoldStar,
        TreasureDefinitionKey.FLOWER to Flower,
        TreasureDefinitionKey.CRYSTAL to Crystal,
        TreasureDefinitionKey.GIFT_BOX to GiftBox,
        TreasureDefinitionKey.LEGENDARY_RELIC to LegendaryRelic,
        TreasureDefinitionKey.STORY_FRAGMENT to StoryFragment,
        TreasureDefinitionKey.SECRET_PLACE_REWARD to SecretPlaceReward,
    )

    private val templatesByType: Map<TreasureType, TreasureTypeTemplate> = listOf(
        GoldStar,
        Flower,
        Crystal,
        GiftBox,
    ).associateBy { it.treasureType!! }

    val standardCollectibles: List<TreasureTypeTemplate> = listOf(
        GoldStar,
        Flower,
        Crystal,
        GiftBox,
    )

    val futureTemplates: List<TreasureTypeTemplate> = listOf(
        LegendaryRelic,
        StoryFragment,
        SecretPlaceReward,
    )

    fun templateForKey(key: TreasureDefinitionKey): TreasureTypeTemplate? = templatesByKey[key]

    fun templateForType(type: TreasureType): TreasureTypeTemplate? = templatesByType[type]

    fun definitionFor(spec: TreasureSpec): TreasureDefinition? =
        definitionFor(treasureId = spec.treasureId, type = spec.type)

    fun definitionFor(treasureId: String, type: TreasureType): TreasureDefinition? {
        val template = templateForType(type) ?: return null
        return template.toDefinition(treasureId)
    }

    fun definitionForKey(
        treasureId: String,
        key: TreasureDefinitionKey,
    ): TreasureDefinition? {
        val template = templateForKey(key) ?: return null
        if (!template.enabled) return null
        return template.toDefinition(treasureId)
    }

    /**
     * Fallback metadata when [TreasureType] has no catalog entry (e.g. HINT).
     * Uses [TreasureTypeRarity] and zero baselines.
     */
    fun fallbackDefinition(treasureId: String, type: TreasureType): TreasureDefinition =
        TreasureDefinition(
            treasureId = treasureId,
            treasureType = type,
            rarity = TreasureTypeRarity.rarityFor(type),
            baseCreditReward = 0,
            baseXpReward = GameplayXpValues.forTreasureType(type) ?: 0L,
            catalogKey = TreasureDefinitionKey.GOLD_STAR,
            category = TreasureDefinitionCategory.STANDARD_COLLECTIBLE,
            displayName = type.id,
        )

    private fun TreasureTypeTemplate.toDefinition(treasureId: String): TreasureDefinition {
        require(treasureType != null) {
            "Template $key is reserved and cannot produce a standard collectible definition"
        }
        return TreasureDefinition(
            treasureId = treasureId,
            treasureType = treasureType,
            rarity = rarity,
            baseCreditReward = baseCreditReward,
            baseXpReward = baseXpReward,
            catalogKey = key,
            category = category,
            displayName = displayName,
        )
    }
}
