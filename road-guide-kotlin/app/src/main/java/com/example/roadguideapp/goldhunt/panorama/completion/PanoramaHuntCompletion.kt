package com.example.roadguideapp.goldhunt.panorama.completion

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType

internal data class PanoramaHuntCompletion(
    val huntId: String,
    val huntType: PanoramaHuntType,
    val secretPlaceId: String,
    val panoramaId: String,
    val targetsFound: Int,
    val targetsTotal: Int,
    val creditsGranted: Int,
    val xpGranted: Long,
    val completedAtMs: Long,
    val achievementKey: String?,
    val storyFragmentId: String?,
    val legendaryRelicKey: String?,
    val rewardEventId: String,
)

internal data class PanoramaHuntTargetFindingRecordResult(
    val isNew: Boolean,
    val slotIndex: Int,
) {
    companion object {
        val skipped = PanoramaHuntTargetFindingRecordResult(isNew = false, slotIndex = -1)
    }
}

internal data class PanoramaHuntCompletionRecordResult(
    val completion: PanoramaHuntCompletion?,
    val isNew: Boolean,
    val creditsGranted: Int = 0,
    val xpGranted: Long = 0L,
) {
    companion object {
        val skipped = PanoramaHuntCompletionRecordResult(
            completion = null,
            isNew = false,
        )
    }
}

internal data class PanoramaHuntCompletionOutcome(
    val targetRecorded: Boolean = false,
    val huntCompleted: Boolean = false,
    val creditsGranted: Int = 0,
    val xpGranted: Long = 0L,
    val storyFragmentGranted: Boolean = false,
    val achievementProgressKey: String? = null,
    val achievementCompletionCount: Int? = null,
    val legendaryRelicHookRecorded: Boolean = false,
    val progress: PanoramaHuntProgress? = null,
) {
    companion object {
        val none = PanoramaHuntCompletionOutcome()
    }
}
