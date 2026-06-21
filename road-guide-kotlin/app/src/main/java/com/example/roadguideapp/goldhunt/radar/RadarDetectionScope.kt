package com.example.roadguideapp.goldhunt.radar

/**
 * Maps equipped radar types to detectable target families and priority weights.
 */
internal object RadarDetectionScope {
    fun categoriesDetectableBy(radarType: RadarType): Set<RadarTargetCategory> = when (radarType) {
        RadarType.BASIC_RADAR -> setOf(RadarTargetCategory.TREASURE)
        RadarType.TREASURE_RADAR -> setOf(RadarTargetCategory.TREASURE)
        RadarType.SECRET_PLACE_RADAR -> setOf(
            RadarTargetCategory.SECRET_PLACE,
            RadarTargetCategory.TREASURE,
        )
        RadarType.CLUSTER_RADAR -> setOf(
            RadarTargetCategory.CLUSTER,
            RadarTargetCategory.TREASURE,
        )
        RadarType.STORY_RADAR -> setOf(
            RadarTargetCategory.STORY_FRAGMENT,
            RadarTargetCategory.SECRET_PLACE,
        )
        RadarType.LEGENDARY_RADAR -> setOf(
            RadarTargetCategory.LEGENDARY_RELIC,
            RadarTargetCategory.STORY_FRAGMENT,
            RadarTargetCategory.CLUSTER,
        )
    }

    fun primaryRadarFor(category: RadarTargetCategory): RadarType =
        category.preferredRadarType

    fun isDetectable(
        category: RadarTargetCategory,
        radarType: RadarType,
    ): Boolean = category in categoriesDetectableBy(radarType)

    fun priorityWeight(
        category: RadarTargetCategory,
        radarType: RadarType,
    ): Int = when {
        category.preferredRadarType == radarType -> 100
        radarType == RadarType.BASIC_RADAR && category == RadarTargetCategory.TREASURE -> 80
        category == RadarTargetCategory.TREASURE -> 40
        radarType == RadarType.LEGENDARY_RADAR &&
            category == RadarTargetCategory.STORY_FRAGMENT -> 60
        radarType == RadarType.STORY_RADAR &&
            category == RadarTargetCategory.SECRET_PLACE -> 50
        else -> 20
    }
}
