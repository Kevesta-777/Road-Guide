package com.example.roadguideapp.goldhunt.achievements.chain

internal data class AchievementChainGate(
    val achievementKey: String,
    val dependencies: List<AchievementChainDependency>,
) {
    init {
        require(achievementKey.isNotBlank()) { "achievementKey must not be blank" }
        require(dependencies.isNotEmpty()) { "dependencies must not be empty" }
    }
}
