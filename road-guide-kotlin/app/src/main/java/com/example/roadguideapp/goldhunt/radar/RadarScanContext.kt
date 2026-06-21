package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardRegion

/**
 * Offline dependencies for one radar pulse (deterministic generators + cached persistence flags).
 */
internal data class RadarScanContext(
    val playRegion: PlayRegion,
    val playRegionId: String,
    val detectionRangeMeters: Int,
    val radarType: RadarType,
    val isTreasureCollected: (String) -> Boolean,
    val isTreasurePlacementExplored: (Double, Double) -> Boolean,
    val isSecretPlaceDiscovered: (String) -> Boolean,
    val areSecretPlacesUnlocked: Boolean,
    val isClusterCompleted: (String) -> Boolean,
    val treasureGenerationTier: TreasureGenerationTier = TreasureGenerationTier.STAR_ONLY,
    val activeHoard: TreasureHoardRegion? = null,
)
