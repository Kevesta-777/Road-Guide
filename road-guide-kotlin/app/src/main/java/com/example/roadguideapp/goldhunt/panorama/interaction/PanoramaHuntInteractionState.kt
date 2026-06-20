package com.example.roadguideapp.goldhunt.panorama.interaction

import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTarget
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetSet

/**
 * Tracks collected hidden targets for a single panorama hunt session.
 */
internal class PanoramaHuntInteractionState(
    private val targetSet: PanoramaHiddenTargetSet,
    preCollectedSlots: Set<Int> = emptySet(),
) {
    private val collectedSlots = linkedSetOf<Int>().apply { addAll(preCollectedSlots) }

    val totalCount: Int
        get() = targetSet.targets.size

    val collectedCount: Int
        get() = collectedSlots.size

    val remainingCount: Int
        get() = totalCount - collectedCount

    val isComplete: Boolean
        get() = collectedCount >= totalCount

    val collectedTargets: List<PanoramaHiddenTarget>
        get() = targetSet.targets
            .filter { it.slotIndex in collectedSlots }
            .sortedBy { it.slotIndex }

    val remainingTargets: List<PanoramaHiddenTarget>
        get() = targetSet.targets
            .filter { it.slotIndex !in collectedSlots }
            .sortedBy { it.slotIndex }

    fun isCollected(target: PanoramaHiddenTarget): Boolean =
        target.slotIndex in collectedSlots

    fun markCollected(target: PanoramaHiddenTarget): Boolean {
        if (target.slotIndex in collectedSlots) return false
        collectedSlots += target.slotIndex
        return true
    }
}
