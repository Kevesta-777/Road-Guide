package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HiddenRelicDiscoverySchemaTest {
    @Test
    fun eventIds_areStablePerRelic() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN)
        assertEquals(
            "hidden_relic_discovery:reveal:$relicId",
            HiddenRelicDiscoverySchema.revealEventId(relicId),
        )
        assertEquals(
            "hidden_relic_discovery:story:$relicId",
            HiddenRelicDiscoverySchema.storyFragmentEventId(relicId),
        )
        assertEquals(
            "hidden_relic_discovery:achievement:$relicId",
            HiddenRelicDiscoverySchema.achievementEventId(relicId),
        )
    }

    @Test
    fun hiddenCategory_hasStoryAndAchievementHooks() {
        val relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN)
        assertEquals(
            RelicSchema.StoryFragmentKeys.revealForRelic(relicId),
            HiddenRelicDiscoverySchema.storyFragmentKeyFor(relicId, RelicCategory.HIDDEN),
        )
        assertEquals(
            RelicSchema.AchievementKeys.firstDiscovery(RelicCategory.HIDDEN),
            HiddenRelicDiscoverySchema.achievementKeyFor(RelicCategory.HIDDEN),
        )
    }
}
