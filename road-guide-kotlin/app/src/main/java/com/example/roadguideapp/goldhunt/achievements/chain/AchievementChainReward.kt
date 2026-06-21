package com.example.roadguideapp.goldhunt.achievements.chain

internal data class AchievementChainReward(
    val sourceAchievementKey: String,
    val unlocks: List<AchievementChainUnlock>,
) {
    init {
        require(sourceAchievementKey.isNotBlank()) { "sourceAchievementKey must not be blank" }
        require(unlocks.isNotEmpty()) { "unlocks must not be empty" }
    }
}
