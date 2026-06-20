package com.example.roadguideapp.goldhunt.relics.story

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicStoryCatalogTest {
    @Test
    fun buildTemplate_includesChaptersLoreAndCompletionStory() {
        val definition = LegendaryRelicDefinition(
            relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.MYTHICAL),
            name = "Mythical Relic",
            description = "Whispered relic lore.",
            category = RelicCategory.MYTHICAL,
            rarity = RelicRarity.EPIC,
            pieceCount = 3,
        )
        val relic = LegendaryRelic(
            relicId = definition.relicId,
            name = definition.name,
            description = definition.description,
            category = definition.category,
            rarity = definition.rarity,
            pieceCount = definition.pieceCount,
            piecesCollected = 1,
            completed = false,
            completionDate = null,
            rewardCredits = 50,
            rewardXp = 80L,
        )

        val template = LegendaryRelicStoryCatalog.buildTemplate(
            relic = relic,
            definition = definition,
            visibility = RelicVisibility.VISIBLE,
        )

        assertEquals(3, template.chapters.size)
        assertEquals(2, template.loreEntries.size)
        assertNotNull(template.completionStory)
        assertEquals(
            RelicSchema.StoryFragmentKeys.completionForRelic(relic.relicId),
            template.completionStory?.storyFragmentKey,
        )
        assertTrue(template.totalFragmentCount >= 5)
    }

    @Test
    fun buildTemplate_hiddenRelic_includesRevealLore() {
        val definition = LegendaryRelicDefinition(
            relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN),
            name = "Hidden Relic",
            description = "Unknown until revealed.",
            category = RelicCategory.HIDDEN,
            rarity = RelicRarity.LEGENDARY,
            pieceCount = 3,
        )
        val relic = LegendaryRelic(
            relicId = definition.relicId,
            name = definition.name,
            description = definition.description,
            category = definition.category,
            rarity = definition.rarity,
            pieceCount = definition.pieceCount,
            completed = false,
            completionDate = null,
            rewardCredits = 50,
            rewardXp = 80L,
        )

        val template = LegendaryRelicStoryCatalog.buildTemplate(
            relic = relic,
            definition = definition,
            visibility = RelicVisibility.HIDDEN,
        )

        assertTrue(template.loreEntries.any { it.trigger == LegendaryRelicStoryLoreTrigger.REVEAL })
    }
}
