package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistryAdapters
import org.junit.Assert.assertEquals
import org.junit.Test

class RelicVisibilityResolverTest {
    @Test
    fun hiddenCategory_isHidden() {
        val definition = LegendaryRelicRegistryAdapters.foundation(RelicCategory.HIDDEN)
        assertEquals(RelicVisibility.HIDDEN, RelicVisibilityResolver.forDefinition(definition))
    }

    @Test
    fun milestone500Relics_areHiddenRegardlessOfCategory() {
        val relicId = RelicSchema.MilestoneKeys.relicId(RelicCategory.EXPLORER, 500)
        assertEquals(
            RelicVisibility.HIDDEN,
            RelicVisibilityResolver.forRelic(
                relicId = relicId,
                category = RelicCategory.EXPLORER,
            ),
        )
    }

    @Test
    fun visibleCategories_remainVisible() {
        assertEquals(
            RelicVisibility.VISIBLE,
            RelicVisibilityResolver.forRelic(
                relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL),
                category = RelicCategory.MYTHICAL,
            ),
        )
    }
}
