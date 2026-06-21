package com.example.roadguideapp.goldhunt.relics.showcase

import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity

internal data class LegendaryRelicShowcaseTitleHighlight(
    val titleKey: String,
    val displayTitle: String,
    val rarity: AchievementTitleRarity,
    val unlockDateMs: Long?,
    val earned: Boolean,
    val isActive: Boolean = false,
)
