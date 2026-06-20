package com.example.roadguideapp.goldhunt.achievements.badges

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainCatalog
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainCatalog
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry

internal object AchievementBadgeCatalog {
    val definitions: List<AchievementBadgeDefinition> by lazy {
        val fromAchievements = AchievementRegistry.allAchievements()
            .filter { it.hasBadgeReward && !it.badgeKey.isNullOrBlank() }
            .map(::definitionFromAchievement)
        val fromChains = AchievementChainCatalog.chainBadgeDefinitions()
        val fromRelicChains = RelicHuntChainCatalog.chainBadgeDefinitions()
        (fromAchievements + fromChains + fromRelicChains)
            .distinctBy { it.badgeKey }
            .sortedWith(
                compareBy<AchievementBadgeDefinition> { it.rarity.ordinal }
                    .thenBy { it.title },
            )
    }

    fun definitionForKey(badgeKey: String): AchievementBadgeDefinition? =
        definitions.firstOrNull { it.badgeKey == badgeKey }

    fun definitionForAchievement(achievementId: String): AchievementBadgeDefinition? =
        definitions.firstOrNull { it.achievementId == achievementId }

    private fun definitionFromAchievement(achievement: Achievement): AchievementBadgeDefinition =
        AchievementBadgeDefinition(
            badgeKey = achievement.badgeKey!!,
            achievementId = achievement.achievementId,
            title = achievement.title,
            iconKey = iconKeyFor(achievement),
            rarity = AchievementBadgeRarity.fromAchievement(achievement),
        )

    private fun iconKeyFor(achievement: Achievement): String {
        val badgeKey = achievement.badgeKey ?: return achievement.category.iconKey
        val suffix = badgeKey.removePrefix("badge_achievement_")
        return if (suffix != badgeKey) {
            "achievement_$suffix"
        } else {
            achievement.category.iconKey
        }
    }
}
