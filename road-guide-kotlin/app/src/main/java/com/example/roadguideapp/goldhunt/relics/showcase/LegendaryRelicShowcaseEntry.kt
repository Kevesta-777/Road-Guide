package com.example.roadguideapp.goldhunt.relics.showcase

import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity

internal data class LegendaryRelicShowcaseEntry(
    val relicId: String,
    val displayName: String,
    val description: String,
    val category: RelicCategory,
    val categoryLabel: String,
    val rarity: RelicRarity,
    val completionDateMs: Long?,
    val creditsEarned: Int,
    val xpEarned: Long,
    val badge: LegendaryRelicShowcaseBadgeHighlight?,
    val title: LegendaryRelicShowcaseTitleHighlight?,
    val achievementLinks: List<LegendaryRelicShowcaseAchievementLink>,
    val story: LegendaryRelicShowcaseStoryHighlight?,
    val rewardsGranted: Boolean,
)
