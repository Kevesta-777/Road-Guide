package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal data class SeasonalEventRewardBundle(
    val credits: Int,
    val xp: Long,
    val eventType: SeasonalEventType,
    val achievementKey: String,
    val masteryAchievementKey: String,
    val storyFragmentKey: String,
    val legendaryRelicKey: String,
    val achievementProgressIncrement: Int = SeasonalEventRewardSchema.ACHIEVEMENT_PROGRESS_INCREMENT,
    val masteryProgressIncrement: Int = SeasonalEventRewardSchema.ACHIEVEMENT_PROGRESS_INCREMENT,
    val legendaryRelicProgressIncrement: Int = 0,
    val recordStoryFragment: Boolean = false,
    val recordLegendaryRelic: Boolean = false,
    val rewardMultiplier: Double,
) {
    val hasAchievement: Boolean get() = achievementKey.isNotBlank()
    val hasStoryFragment: Boolean get() = recordStoryFragment && storyFragmentKey.isNotBlank()
    val hasLegendaryRelic: Boolean get() = recordLegendaryRelic && legendaryRelicKey.isNotBlank()
}
