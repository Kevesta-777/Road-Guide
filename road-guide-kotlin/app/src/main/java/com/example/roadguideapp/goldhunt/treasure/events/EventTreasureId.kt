package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.engine.GridCell
import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal object EventTreasureId {
    fun forL1Slot(
        playRegionId: String,
        eventType: SeasonalEventType,
        cycleYear: Int,
        l1: GridCell,
        slot: Int,
    ): String =
        "tr:event:v${EventTreasureSchema.VERSION}:$playRegionId:" +
            "${eventType.id.lowercase()}:$cycleYear:L1:${l1.ix}:${l1.iy}:s$slot"
}
