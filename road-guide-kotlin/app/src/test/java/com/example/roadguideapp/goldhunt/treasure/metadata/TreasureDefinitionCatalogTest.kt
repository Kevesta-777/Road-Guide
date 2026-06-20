package com.example.roadguideapp.goldhunt.treasure.metadata

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpValues
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TreasureDefinitionCatalogTest {
    @Test
    fun standardTemplates_matchGameplayBaselines() {
        assertEquals("Gold Star", TreasureDefinitionCatalog.GoldStar.displayName)
        assertEquals(TreasureType.STAR, TreasureDefinitionCatalog.GoldStar.treasureType)
        assertEquals(TreasureRarity.COMMON, TreasureDefinitionCatalog.GoldStar.rarity)
        assertEquals(GoldHuntConfig.TREASURE_STAR_CREDIT_MIN, TreasureDefinitionCatalog.GoldStar.baseCreditReward)
        assertEquals(GameplayXpValues.TREASURE_STAR, TreasureDefinitionCatalog.GoldStar.baseXpReward)

        assertEquals(TreasureRarity.EPIC, TreasureDefinitionCatalog.GiftBox.rarity)
        assertEquals(GameplayXpValues.TREASURE_GIFT, TreasureDefinitionCatalog.GiftBox.baseXpReward)
    }

    @Test
    fun definitionFor_spec_populatesAllFields() {
        val spec = TreasureSpec(
            treasureId = "t-42",
            type = TreasureType.FLOWER,
            lat = 51.5,
            lng = -0.1,
            creditAmount = 99,
            placementKind = "PROCEDURAL_L1",
            regionL1Id = "L1:0:0",
        )
        val definition = TreasureDefinitionCatalog.definitionFor(spec)
        assertNotNull(definition)
        requireNotNull(definition)
        assertEquals("t-42", definition.treasureId)
        assertEquals(TreasureType.FLOWER, definition.treasureType)
        assertEquals(TreasureRarity.UNCOMMON, definition.rarity)
        assertEquals(GoldHuntConfig.TREASURE_FLOWER_CREDIT_MIN, definition.baseCreditReward)
        assertEquals(GameplayXpValues.TREASURE_FLOWER, definition.baseXpReward)
        assertEquals(TreasureDefinitionCategory.STANDARD_COLLECTIBLE, definition.category)
    }

    @Test
    fun futureTemplates_areDisabled() {
        assertFalse(TreasureDefinitionCatalog.LegendaryRelic.enabled)
        assertFalse(TreasureDefinitionCatalog.StoryFragment.enabled)
        assertFalse(TreasureDefinitionCatalog.SecretPlaceReward.enabled)
        assertNull(
            TreasureDefinitionCatalog.definitionForKey(
                treasureId = "relic-1",
                key = TreasureDefinitionKey.LEGENDARY_RELIC,
            ),
        )
    }
}
