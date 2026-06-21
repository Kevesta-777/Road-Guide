package com.example.roadguideapp.goldhunt.relics.rewards

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LegendaryRelicRewardDispatcherTest {
    private val storyRelic = LegendaryRelic(
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.STORY),
        name = "Story Relic",
        description = "A narrative relic.",
        category = RelicCategory.STORY,
        rarity = RelicRarity.COMMON,
        pieceCount = 1,
        piecesCollected = 1,
        completed = true,
        completionDate = 1_000L,
        rewardCredits = 80,
        rewardXp = 120L,
        badgeKey = RelicSchema.BadgeKeys.forRelic(
            RelicSchema.RelicKeys.forCategory(RelicCategory.STORY),
        ),
        titleKey = RelicSchema.TitleKeys.forRelic(
            RelicSchema.RelicKeys.forCategory(RelicCategory.STORY),
        ),
    )

    private val warriorRelic = storyRelic.copy(
        relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR),
        name = "Warrior Relic",
        description = "A warrior relic.",
        category = RelicCategory.WARRIOR,
        rarity = RelicRarity.RARE,
        badgeKey = RelicSchema.BadgeKeys.forRelic(
            RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR),
        ),
        titleKey = RelicSchema.TitleKeys.forRelic(
            RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR),
        ),
    )

    @Test
    fun dispatch_storyRelic_includesCreditsXpStoryTitleAndBadge() {
        val outcome = LegendaryRelicRewardDispatcher.dispatch(storyRelic)
        assertEquals(RewardRuleType.LEGENDARY_RELIC_COMPLETION, outcome.creditGrant.ruleType)
        assertEquals(80, outcome.creditGrant.amount)
        assertEquals(120L, outcome.xp)
        assertEquals(
            RelicSchema.StoryFragmentKeys.completionForRelic(storyRelic.relicId),
            outcome.storyFragmentKey,
        )
        assertNotNull(outcome.titleKey)
        assertNotNull(outcome.badgeKey)
    }

    @Test
    fun dispatch_warriorRelic_omitsStoryFragment() {
        val outcome = LegendaryRelicRewardDispatcher.dispatch(warriorRelic)
        assertNull(outcome.storyFragmentKey)
        assertNotNull(outcome.badgeKey)
        assertNotNull(outcome.titleKey)
    }

    @Test
    fun outcome_buildsStableHookEventIds() {
        val outcome = LegendaryRelicRewardDispatcher.dispatch(storyRelic)
        assertEquals(
            LegendaryRelicRewardSchema.storyFragmentEventId(storyRelic.relicId),
            outcome.storyFragmentEventId(),
        )
        assertEquals(
            LegendaryRelicRewardSchema.badgeEventId(storyRelic.relicId),
            outcome.badgeEventId(),
        )
        assertEquals(
            LegendaryRelicRewardSchema.titleEventId(storyRelic.relicId),
            outcome.titleEventId(),
        )
    }
}
