package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

internal data class SeasonalEventRewardContext(
    val spec: TreasureSpec,
    val event: SeasonalEvent,
    val explorerLevel: Int,
    val rarity: TreasureRarity,
)
