package com.example.roadguideapp.goldhunt.achievements.journal

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainContext
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainResolver
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibility
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibilityResolver

internal object AchievementJournalStatusResolver {
    fun resolve(
        definition: AchievementDefinition,
        achievement: Achievement,
        explorerLevel: Int,
        achievementsByKey: Map<String, Achievement> = emptyMap(),
        chainContext: AchievementChainContext? = null,
    ): AchievementJournalStatus {
        val visibility = AchievementVisibilityResolver.forDefinition(definition)
        val context = chainContext ?: AchievementChainResolver.buildContext(achievementsByKey)
        return resolve(
            definition = definition,
            achievement = achievement,
            explorerLevel = explorerLevel,
            visibility = visibility,
            chainContext = context,
        )
    }

    fun resolve(
        definition: AchievementDefinition,
        achievement: Achievement,
        explorerLevel: Int,
        visibility: AchievementVisibility,
        chainContext: AchievementChainContext = AchievementChainContext(emptyMap()),
    ): AchievementJournalStatus {
        if (achievement.completed) return AchievementJournalStatus.COMPLETED
        if (explorerLevel < definition.minExplorerLevel) return AchievementJournalStatus.LOCKED
        if (visibility != AchievementVisibility.VISIBLE) return AchievementJournalStatus.HIDDEN
        if (!AchievementChainResolver.isProgressAllowed(definition.key, chainContext)) {
            return AchievementJournalStatus.LOCKED
        }
        return AchievementJournalStatus.INCOMPLETE
    }
}
