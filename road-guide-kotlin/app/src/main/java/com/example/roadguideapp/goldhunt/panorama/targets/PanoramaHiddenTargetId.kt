package com.example.roadguideapp.goldhunt.panorama.targets

/**
 * Stable identifiers for hidden panorama targets.
 */
internal object PanoramaHiddenTargetId {
    private val SLOT_SUFFIX = Regex(":s(\\d+)$")

    fun forHuntSlot(huntId: String, slotIndex: Int): String =
        "pht:v${PanoramaHiddenTargetSchema.VERSION}:$huntId:s$slotIndex"

    fun parseSlotIndex(targetId: String): Int? =
        SLOT_SUFFIX.find(targetId)?.groupValues?.get(1)?.toIntOrNull()
}
