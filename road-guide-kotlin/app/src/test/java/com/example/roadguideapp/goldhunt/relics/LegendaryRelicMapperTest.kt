package com.example.roadguideapp.goldhunt.relics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicMapperTest {
    @Test
    fun fromDefinition_buildsIncompleteRelic() {
        val definition = LegendaryRelicCatalog.foundationDefinition(RelicCategory.ROYAL)
        val relic = LegendaryRelicMapper.fromDefinition(definition)

        assertEquals(definition.relicId, relic.relicId)
        assertEquals(definition.name, relic.name)
        assertEquals(RelicCategory.ROYAL, relic.category)
        assertEquals(RelicRarity.LEGENDARY, relic.rarity)
        assertEquals(7, relic.pieceCount)
        assertEquals(0, relic.piecesCollected)
        assertFalse(relic.completed)
        assertNull(relic.completionDate)
        assertTrue(relic.rewardCredits > 0)
        assertTrue(relic.rewardXp > 0L)
        assertTrue(relic.hasBadgeReward)
        assertTrue(relic.hasTitleReward)
        assertTrue(relic.hasPowerReward)
    }

    @Test
    fun fromDefinition_completedRelic_recordsCompletionDate() {
        val definition = LegendaryRelicCatalog.foundationDefinition(RelicCategory.STORY)
        val relic = LegendaryRelicMapper.fromDefinition(
            definition = definition,
            piecesCollected = 1,
            completed = true,
            completionDate = 8_000L,
        )
        assertTrue(relic.completed)
        assertEquals(8_000L, relic.completionDate)
        assertEquals(1, relic.piecesCollected)
    }

    @Test
    fun entityRoundTrip_preservesFields() {
        val original = LegendaryRelicCatalog.relicFromDefinition(
            definition = LegendaryRelicCatalog.foundationDefinition(RelicCategory.WARRIOR),
            piecesCollected = 2,
            completed = false,
        )
        val entity = LegendaryRelicMapper.toEntity(original, updatedAtMs = 9_500L)
        val restored = LegendaryRelicMapper.toDomain(entity)

        assertNotNull(restored)
        assertEquals(original.relicId, restored!!.relicId)
        assertEquals(original.name, restored.name)
        assertEquals(original.description, restored.description)
        assertEquals(original.category, restored.category)
        assertEquals(original.rarity, restored.rarity)
        assertEquals(original.pieceCount, restored.pieceCount)
        assertEquals(original.piecesCollected, restored.piecesCollected)
        assertEquals(original.completed, restored.completed)
        assertEquals(original.completionDate, restored.completionDate)
        assertEquals(original.rewardCredits, restored.rewardCredits)
        assertEquals(original.rewardXp, restored.rewardXp)
        assertEquals(original.badgeKey, restored.badgeKey)
        assertEquals(original.titleKey, restored.titleKey)
        assertEquals(original.powerKey, restored.powerKey)
    }

    @Test
    fun toDomain_returnsNullForUnknownCategoryOrRarity() {
        val entity = LegendaryRelicMapper.toEntity(
            LegendaryRelicCatalog.foundationRelics().first(),
        ).copy(category = "unknown_category")
        assertNull(LegendaryRelicMapper.toDomain(entity))
    }
}
