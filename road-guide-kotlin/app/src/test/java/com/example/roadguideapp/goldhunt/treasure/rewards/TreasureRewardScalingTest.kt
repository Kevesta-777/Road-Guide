package com.example.roadguideapp.goldhunt.treasure.rewards

import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionCatalog
import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionKey
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureRewardScalingTest {
    @Test
    fun multipliers_matchDesign() {
        assertEquals(1.0, TreasureRewardMultipliers.forRarity(TreasureRarity.COMMON), 0.0001)
        assertEquals(1.5, TreasureRewardMultipliers.forRarity(TreasureRarity.UNCOMMON), 0.0001)
        assertEquals(3.0, TreasureRewardMultipliers.forRarity(TreasureRarity.RARE), 0.0001)
        assertEquals(5.0, TreasureRewardMultipliers.forRarity(TreasureRarity.EPIC), 0.0001)
        assertEquals(10.0, TreasureRewardMultipliers.forRarity(TreasureRarity.LEGENDARY), 0.0001)
        assertEquals(25.0, TreasureRewardMultipliers.forRarity(TreasureRarity.MYTHIC), 0.0001)
    }

    @Test
    fun scale_epicCredits_isFiveTimesBase() {
        val reward = TreasureRewardScaling.scale(baseCredits = 10, baseXp = 3L, rarity = TreasureRarity.EPIC)
        assertEquals(50, reward.scaledCredits)
        assertEquals(15L, reward.scaledXp)
        assertEquals(5.0, reward.rewardMultiplier, 0.0001)
    }

    @Test
    fun scale_mythicXp_isTwentyFiveTimesBase() {
        val reward = TreasureRewardScaling.scale(baseCredits = 4, baseXp = 10L, rarity = TreasureRarity.MYTHIC)
        assertEquals(100, reward.scaledCredits)
        assertEquals(250L, reward.scaledXp)
    }

    @Test
    fun scale_definitionWithRolledRarity_usesCatalogBases() {
        val definition = requireNotNull(
            TreasureDefinitionCatalog.definitionForKey("test-star", TreasureDefinitionKey.GOLD_STAR),
        )
        val reward = TreasureRewardScaling.scale(definition, TreasureRarity.RARE)
        assertEquals(definition.baseCreditReward, reward.baseCredits)
        assertEquals(definition.baseXpReward * 3L, reward.scaledXp)
    }

    @Test
    fun legacyRarityRewards_delegateToScaling() {
        assertEquals(50, com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarityRewards.scaledCredits(10, TreasureRarity.EPIC))
        assertEquals(75L, com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarityRewards.scaledXp(3L, TreasureRarity.MYTHIC))
    }
}
