package com.example.roadguideapp.goldhunt.panorama.completion

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTarget

internal object PanoramaHuntCompletionMapper {
    fun toTargetFindingEntity(
        target: PanoramaHiddenTarget,
        foundAtMs: Long,
    ): PanoramaHuntTargetFindingEntity = PanoramaHuntTargetFindingEntity(
        targetId = target.targetId,
        huntId = target.huntId,
        slotIndex = target.slotIndex,
        foundAtMs = foundAtMs,
    )

    fun toCompletionEntity(
        hunt: PanoramaHunt,
        targetsFound: Int,
        targetsTotal: Int,
        creditsGranted: Int,
        xpGranted: Long,
        completedAtMs: Long,
    ): PanoramaHuntCompletionEntity = PanoramaHuntCompletionEntity(
        huntId = hunt.huntId,
        huntType = hunt.huntType.id,
        secretPlaceId = hunt.secretPlaceId,
        panoramaId = hunt.panoramaId,
        targetsFound = targetsFound,
        targetsTotal = targetsTotal,
        creditsGranted = creditsGranted,
        xpGranted = xpGranted,
        completedAtMs = completedAtMs,
        achievementKey = hunt.resolvedAchievementKey,
        storyFragmentId = hunt.resolvedStoryFragmentId,
        legendaryRelicKey = hunt.resolvedLegendaryRelicKey,
        rewardEventId = PanoramaHuntCompletionRewardDispatcher.grantFor(hunt).eventId,
    )

    fun toDomain(entity: PanoramaHuntCompletionEntity): PanoramaHuntCompletion? {
        val huntType = PanoramaHuntType.fromId(entity.huntType) ?: return null
        return PanoramaHuntCompletion(
            huntId = entity.huntId,
            huntType = huntType,
            secretPlaceId = entity.secretPlaceId,
            panoramaId = entity.panoramaId,
            targetsFound = entity.targetsFound,
            targetsTotal = entity.targetsTotal,
            creditsGranted = entity.creditsGranted,
            xpGranted = entity.xpGranted,
            completedAtMs = entity.completedAtMs,
            achievementKey = entity.achievementKey,
            storyFragmentId = entity.storyFragmentId,
            legendaryRelicKey = entity.legendaryRelicKey,
            rewardEventId = entity.rewardEventId,
        )
    }

    fun toProgress(
        huntId: String,
        collectedSlotIndices: Set<Int>,
        targetsTotal: Int,
        completedAtMs: Long? = null,
    ): PanoramaHuntProgress = PanoramaHuntProgress(
        huntId = huntId,
        targetsFound = collectedSlotIndices.size,
        targetsTotal = targetsTotal,
        collectedSlotIndices = collectedSlotIndices,
        isComplete = collectedSlotIndices.size >= targetsTotal && targetsTotal > 0,
        completedAtMs = completedAtMs,
    )
}
