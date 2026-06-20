package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec

internal object EventTreasureDetector {
    private val eventTreasureIdPattern =
        Regex("""^tr:event:v\d+:""")

    fun isEventTreasure(spec: TreasureSpec): Boolean =
        spec.placementKind.startsWith("${EventTreasureSchema.PlacementKinds.PREFIX}_") ||
            eventTreasureIdPattern.containsMatchIn(spec.treasureId)

    fun eventTypeFrom(spec: TreasureSpec): SeasonalEventType? {
        if (!isEventTreasure(spec)) return null
        val suffix = spec.placementKind.removePrefix("${EventTreasureSchema.PlacementKinds.PREFIX}_")
        return SeasonalEventType.fromId(suffix)
    }
}
