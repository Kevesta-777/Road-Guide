package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardSchema
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicHuntChainResolverTest {
    private val explorerId = RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER)
    private val storyId = RelicSchema.RelicKeys.forCategory(RelicCategory.STORY)
    private val hiddenId = RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN)

    @Test
    fun isProgressAllowed_blocksStoryUntilExplorerCompleted() {
        val context = RelicHuntChainResolver.buildContext(
            relicsById = mapOf(
                explorerId to relic(explorerId, RelicCategory.EXPLORER, completed = false),
                storyId to relic(storyId, RelicCategory.STORY, completed = false),
            ),
        )
        assertFalse(RelicHuntChainResolver.isProgressAllowed(storyId, context))
    }

    @Test
    fun isProgressAllowed_allowsStoryAfterExplorerCompleted() {
        val context = RelicHuntChainResolver.buildContext(
            relicsById = mapOf(
                explorerId to relic(explorerId, RelicCategory.EXPLORER, completed = true, completionDate = 1_000L),
                storyId to relic(storyId, RelicCategory.STORY, completed = false),
            ),
        )
        assertTrue(RelicHuntChainResolver.isProgressAllowed(storyId, context))
    }

    @Test
    fun isProgressAllowed_requiresChainUnlockForHiddenRelic() {
        val context = RelicHuntChainResolver.buildContext(
            relicsById = mapOf(
                hiddenId to relic(hiddenId, RelicCategory.HIDDEN, completed = false),
            ),
        )
        assertFalse(RelicHuntChainResolver.isProgressAllowed(hiddenId, context))
    }

    @Test
    fun isProgressAllowed_allowsHiddenAfterChainHook() {
        val royalId = RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL)
        val context = RelicHuntChainResolver.buildContext(
            relicsById = mapOf(
                royalId to relic(royalId, RelicCategory.ROYAL, completed = true, completionDate = 1_000L),
                hiddenId to relic(hiddenId, RelicCategory.HIDDEN, completed = false),
            ),
            chainHooks = listOf(
                LegendaryRelicRewardHookEntity(
                    eventId = "relic_hunt_chain:unlock:royal:legendaryRelic:$hiddenId",
                    relicId = RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
                    hookType = LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK,
                    hookKey = hiddenId,
                    grantedAtMs = 1_000L,
                ),
            ),
        )
        assertTrue(RelicHuntChainResolver.isProgressAllowed(hiddenId, context))
    }

    private fun relic(
        relicId: String,
        category: RelicCategory,
        completed: Boolean,
        completionDate: Long? = null,
    ): LegendaryRelic = LegendaryRelic(
        relicId = relicId,
        name = "${category.displayName} Relic",
        description = "Description",
        category = category,
        rarity = RelicRarity.forCategory(category),
        pieceCount = 1,
        piecesCollected = if (completed) 1 else 0,
        completed = completed,
        completionDate = completionDate,
        rewardCredits = 50,
        rewardXp = 80L,
    )
}
