package com.example.roadguideapp.goldhunt.relics.rewards

import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDefinition
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicRarity

internal object LegendaryRelicRewardTitleCatalog {
    fun definitionForRelic(relic: LegendaryRelic): AchievementTitleDefinition? {
        val titleKey = relic.titleKey ?: return null
        return AchievementTitleDefinition(
            titleKey = titleKey,
            achievementId = relic.relicId,
            displayTitle = relic.name,
            rarity = rarityFor(relic.rarity),
        )
    }

    private fun rarityFor(rarity: RelicRarity): AchievementTitleRarity = when (rarity) {
        RelicRarity.LEGENDARY -> AchievementTitleRarity.LEGENDARY
        RelicRarity.EPIC,
        RelicRarity.RARE,
        -> AchievementTitleRarity.RARE
        RelicRarity.COMMON -> AchievementTitleRarity.RARE
    }
}
