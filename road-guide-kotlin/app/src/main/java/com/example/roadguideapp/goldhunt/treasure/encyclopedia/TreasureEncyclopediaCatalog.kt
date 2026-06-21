package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionCatalog
import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureTypeTemplate

/**
 * Display order and seed templates for the treasure encyclopedia.
 */
internal object TreasureEncyclopediaCatalog {
    val displayTemplates: List<TreasureTypeTemplate> =
        TreasureDefinitionCatalog.standardCollectibles + TreasureDefinitionCatalog.futureTemplates

    val trackableTemplates: List<TreasureTypeTemplate> =
        displayTemplates.filter { it.enabled }

    fun initialStatus(template: TreasureTypeTemplate): TreasureEncyclopediaStatus = when {
        !template.enabled -> TreasureEncyclopediaStatus.LOCKED
        else -> TreasureEncyclopediaStatus.MISSING
    }
}
