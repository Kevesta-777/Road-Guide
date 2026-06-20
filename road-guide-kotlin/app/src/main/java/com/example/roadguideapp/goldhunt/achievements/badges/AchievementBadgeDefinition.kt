package com.example.roadguideapp.goldhunt.achievements.badges

internal data class AchievementBadgeDefinition(
    val badgeKey: String,
    val achievementId: String,
    val title: String,
    val iconKey: String,
    val rarity: AchievementBadgeRarity,
) {
    init {
        require(badgeKey.isNotBlank()) { "badgeKey must not be blank" }
        require(achievementId.isNotBlank()) { "achievementId must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(iconKey.isNotBlank()) { "iconKey must not be blank" }
    }
}
