package com.example.roadguideapp.goldhunt.relics.rewards

import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeDefinition
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRarity
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry

internal object LegendaryRelicRewardBadgeCatalog {
    fun definitionForRelic(relic: LegendaryRelic): AchievementBadgeDefinition? {
        val badgeKey = relic.badgeKey ?: return null
        return AchievementBadgeDefinition(
            badgeKey = badgeKey,
            achievementId = relic.relicId,
            title = "${relic.name} Badge",
            iconKey = relic.iconKey,
            rarity = rarityFor(relic.rarity),
        )
    }

    fun seedDefinitions(): List<AchievementBadgeDefinition> =
        LegendaryRelicRegistry.allDefinitions()
            .mapNotNull { definition ->
                val relic = LegendaryRelicCatalog.relicFromDefinition(definition)
                definitionForRelic(relic)
            }
            .distinctBy { it.badgeKey }

    private fun rarityFor(rarity: RelicRarity): AchievementBadgeRarity = when (rarity) {
        RelicRarity.LEGENDARY -> AchievementBadgeRarity.LEGENDARY
        RelicRarity.EPIC,
        RelicRarity.RARE,
        -> AchievementBadgeRarity.RARE
        RelicRarity.COMMON -> AchievementBadgeRarity.COMMON
    }
}
