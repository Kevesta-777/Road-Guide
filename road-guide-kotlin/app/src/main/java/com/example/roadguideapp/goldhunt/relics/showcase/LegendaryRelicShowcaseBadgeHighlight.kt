package com.example.roadguideapp.goldhunt.relics.showcase

import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRarity

internal data class LegendaryRelicShowcaseBadgeHighlight(
    val badgeKey: String,
    val title: String,
    val iconKey: String,
    val rarity: AchievementBadgeRarity,
    val unlockDateMs: Long?,
    val earned: Boolean,
)
