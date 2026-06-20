package com.example.roadguideapp.goldhunt.panorama.journal

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntTypeCatalog

internal data class PanoramaHuntJournalTemplate(
    val huntType: PanoramaHuntType,
    val displayName: String,
    val achievementKey: String?,
    val storyFragmentKey: String?,
    val legendaryRelicKey: String?,
)

internal object PanoramaHuntJournalCatalog {
    val displayTemplates: List<PanoramaHuntJournalTemplate> =
        PanoramaHuntTypeCatalog.displayOrder.map { type ->
            PanoramaHuntJournalTemplate(
                huntType = type,
                displayName = type.displayName,
                achievementKey = type.achievementKey,
                storyFragmentKey = type.storyFragmentKey,
                legendaryRelicKey = type.legendaryRelicKey,
            )
        }

    fun initialStatus(): PanoramaHuntJournalStatus = PanoramaHuntJournalStatus.MISSING

    fun templateForType(type: PanoramaHuntType): PanoramaHuntJournalTemplate? =
        displayTemplates.firstOrNull { it.huntType == type }

    fun templateForId(huntTypeId: String): PanoramaHuntJournalTemplate? =
        PanoramaHuntType.fromId(huntTypeId)?.let { templateForType(it) }
}
