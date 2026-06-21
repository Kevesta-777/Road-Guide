package com.example.roadguideapp.goldhunt.relics.rewards

import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicRewardBadgeCatalogTest {
    @Test
    fun definitionForRelic_resolvesFoundationBadge() {
        val relic = LegendaryRelicCatalog.relicFromDefinition(
            LegendaryRelicCatalog.foundationDefinition(RelicCategory.MYTHICAL),
        )
        val definition = LegendaryRelicRewardBadgeCatalog.definitionForRelic(relic)
        assertNotNull(definition)
        assertEquals(
            RelicSchema.BadgeKeys.forRelic(relic.relicId),
            definition!!.badgeKey,
        )
    }

    @Test
    fun seedDefinitions_coversFoundationRelics() {
        val seeds = LegendaryRelicRewardBadgeCatalog.seedDefinitions()
        assertTrue(seeds.size >= RelicCategory.ALL_ORDERED.size)
    }
}
