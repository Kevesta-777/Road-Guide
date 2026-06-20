package com.example.roadguideapp.goldhunt.relics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicCatalogTest {
    @Test
    fun foundationDefinitions_coverAllCategories() {
        val definitions = LegendaryRelicCatalog.foundationDefinitions()
        assertEquals(RelicCategory.ALL_ORDERED.size, definitions.size)
        RelicCategory.ALL_ORDERED.forEach { category ->
            assertNotNull(LegendaryRelicCatalog.findById(RelicSchema.RelicKeys.forCategory(category)))
        }
    }

    @Test
    fun scaledRewards_increaseWithCategoryAndRarity() {
        val explorerCredits = LegendaryRelicCatalog.scaledCompletionCredits(
            category = RelicCategory.EXPLORER,
            rarity = RelicRarity.COMMON,
        )
        val hiddenCredits = LegendaryRelicCatalog.scaledCompletionCredits(
            category = RelicCategory.HIDDEN,
            rarity = RelicRarity.LEGENDARY,
        )
        assertTrue(hiddenCredits > explorerCredits)
    }

    @Test
    fun foundationDefinition_resolvesFutureRewardKeys() {
        val definition = LegendaryRelicCatalog.foundationDefinition(RelicCategory.ANCIENT_CIVILIZATION)
        assertEquals(
            RelicSchema.BadgeKeys.forRelic(definition.relicId),
            definition.resolvedBadgeKey,
        )
        assertEquals(
            RelicSchema.TitleKeys.forRelic(definition.relicId),
            definition.resolvedTitleKey,
        )
        assertEquals(
            RelicSchema.PowerKeys.forRelic(definition.relicId),
            definition.resolvedPowerKey,
        )
    }
}
