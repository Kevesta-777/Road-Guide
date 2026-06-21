package com.example.roadguideapp.goldhunt.achievements.titles

import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainCatalog
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainCatalog
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry

internal object AchievementTitleCatalog {
    val definitions: List<AchievementTitleDefinition> by lazy {
        val fromAchievements = AchievementRegistry.allAchievements()
            .filter { it.hasTitleReward && !it.titleKey.isNullOrBlank() }
            .map { achievement ->
                AchievementTitleDefinition(
                    titleKey = achievement.titleKey!!,
                    achievementId = achievement.achievementId,
                    displayTitle = achievement.title,
                    rarity = AchievementTitleRarity.fromAchievement(achievement),
                )
            }
        val fromChains = AchievementChainCatalog.chainTitleDefinitions()
        val fromRelicChains = RelicHuntChainCatalog.chainTitleDefinitions()
        (fromAchievements + fromChains + fromRelicChains)
            .distinctBy { it.titleKey }
            .sortedWith(
                compareBy<AchievementTitleDefinition> { it.rarity.ordinal }
                    .thenBy { it.displayTitle },
            )
    }

    fun definitionForKey(titleKey: String): AchievementTitleDefinition? =
        definitions.firstOrNull { it.titleKey == titleKey }

    fun definitionForAchievement(achievementId: String): AchievementTitleDefinition? =
        definitions.firstOrNull { it.achievementId == achievementId }
}
