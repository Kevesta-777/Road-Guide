package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaHuntRewardScalingTest {
    @Test
    fun scaledRewards_increaseWithDifficultyAndHuntType() {
        val easy = context(PanoramaHuntType.HIDDEN_SYMBOL, explorerLevel = 1)
        val hard = context(PanoramaHuntType.RELIC_HUNT, explorerLevel = 1)
        assertTrue(PanoramaHuntRewardScaling.scaledCredits(hard) > PanoramaHuntRewardScaling.scaledCredits(easy))
        assertTrue(PanoramaHuntRewardScaling.scaledXp(hard) > PanoramaHuntRewardScaling.scaledXp(easy))
    }

    @Test
    fun scaledRewards_increaseWithExplorerLevel() {
        val low = context(PanoramaHuntType.OBJECT_HUNT, explorerLevel = 1)
        val high = context(PanoramaHuntType.OBJECT_HUNT, explorerLevel = 10)
        assertTrue(PanoramaHuntRewardScaling.scaledCredits(high) > PanoramaHuntRewardScaling.scaledCredits(low))
        assertTrue(PanoramaHuntRewardScaling.scaledXp(high) > PanoramaHuntRewardScaling.scaledXp(low))
    }

    @Test
    fun completionBundle_includesHooksForRelicHunt() {
        val bundle = PanoramaHuntRewards.completionBundle(
            context(PanoramaHuntType.RELIC_HUNT, explorerLevel = 5),
        )
        assertTrue(bundle.hasAchievement)
        assertTrue(bundle.hasStoryFragment)
        assertTrue(bundle.hasLegendaryRelic)
    }

    @Test
    fun rewardOutcome_exposesStableHookEventIds() {
        val hunt = PanoramaHunt.forSecretPlace(
            secretPlaceId = "sp:v1:test:SECRET_CODE:seed1",
            huntType = PanoramaHuntType.SECRET_CODE,
            panoramaId = "pano-1",
            rewardSeed = 3L,
        )
        val outcome = PanoramaHuntRewardDispatcher.dispatchCompletion(hunt, explorerLevel = 2)
        assertTrue(outcome.creditEventId().contains(hunt.huntId))
        assertTrue(outcome.storyFragmentEventId()!!.contains("story_fragment_secret_code"))
        assertTrue(outcome.achievementEventId()!!.contains("panorama_hunt_secret_code"))
    }

    private fun context(
        type: PanoramaHuntType,
        explorerLevel: Int,
    ): PanoramaHuntRewardContext = PanoramaHuntRewardContext(
        hunt = PanoramaHunt.forSecretPlace(
            secretPlaceId = "sp:v1:test:${type.id}:seed1",
            huntType = type,
            panoramaId = "pano-test",
            rewardSeed = 1L,
        ),
        explorerLevel = explorerLevel,
    )
}
