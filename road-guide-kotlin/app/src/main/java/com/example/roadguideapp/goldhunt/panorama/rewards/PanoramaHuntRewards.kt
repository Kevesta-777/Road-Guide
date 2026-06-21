package com.example.roadguideapp.goldhunt.panorama.rewards

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntTypeSchema

internal object PanoramaHuntRewards {
    fun completionBundle(context: PanoramaHuntRewardContext): PanoramaHuntRewardBundle {
        val hunt = context.hunt
        val scaling = PanoramaHuntRewardScaling.breakdown(context)
        return PanoramaHuntRewardBundle(
            credits = PanoramaHuntRewardScaling.scaledCredits(context),
            xp = PanoramaHuntRewardScaling.scaledXp(context),
            rewardMultiplier = scaling.combinedMultiplier,
            huntType = hunt.huntType,
            storyFragmentId = resolveStoryFragmentId(hunt),
            achievementKey = resolveAchievementKey(hunt),
            legendaryRelicKey = resolveLegendaryRelicKey(hunt),
        )
    }

    fun completionBundle(
        hunt: PanoramaHunt,
        explorerLevel: Int,
    ): PanoramaHuntRewardBundle = completionBundle(
        PanoramaHuntRewardContext(hunt = hunt, explorerLevel = explorerLevel),
    )

    private fun resolveStoryFragmentId(hunt: PanoramaHunt): String? =
        hunt.resolvedStoryFragmentId
            ?: PanoramaHuntTypeSchema.StoryFragmentKeys.forType(hunt.huntType)

    private fun resolveAchievementKey(hunt: PanoramaHunt): String =
        hunt.resolvedAchievementKey
            ?: PanoramaHuntTypeSchema.AchievementKeys.firstCompletion(hunt.huntType)

    private fun resolveLegendaryRelicKey(hunt: PanoramaHunt): String? =
        hunt.resolvedLegendaryRelicKey
            ?: PanoramaHuntTypeSchema.LegendaryRelicKeys.forType(hunt.huntType)
}
