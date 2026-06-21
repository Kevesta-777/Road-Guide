package com.example.roadguideapp.goldhunt.achievements.titles

internal data class AchievementTitleDefinition(
    val titleKey: String,
    val achievementId: String,
    val displayTitle: String,
    val rarity: AchievementTitleRarity,
) {
    init {
        require(titleKey.isNotBlank()) { "titleKey must not be blank" }
        require(achievementId.isNotBlank()) { "achievementId must not be blank" }
        require(displayTitle.isNotBlank()) { "displayTitle must not be blank" }
    }
}
