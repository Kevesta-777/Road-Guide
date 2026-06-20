package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.Achievement

internal data class AchievementChainContext(
    val achievementsByKey: Map<String, Achievement>,
    val chainUnlockedAchievementKeys: Set<String> = emptySet(),
) {
    fun isAchievementCompleted(achievementKey: String): Boolean =
        achievementsByKey[achievementKey]?.completed == true

    fun isAchievementAvailable(achievementKey: String): Boolean =
        isAchievementCompleted(achievementKey) || achievementKey in chainUnlockedAchievementKeys
}
