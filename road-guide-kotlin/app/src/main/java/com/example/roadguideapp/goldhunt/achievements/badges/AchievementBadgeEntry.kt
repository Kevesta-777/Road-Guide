package com.example.roadguideapp.goldhunt.achievements.badges

internal data class AchievementBadgeEntry(
    val badgeKey: String,
    val achievementId: String,
    val title: String,
    val iconKey: String,
    val rarity: AchievementBadgeRarity,
    val unlockDateMs: Long?,
    val schemaVersion: Int = AchievementBadgeSchema.VERSION,
    val extensionJson: String = AchievementBadgeSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isUnlocked: Boolean get() = unlockDateMs != null && unlockDateMs > 0L
    val isLocked: Boolean get() = !isUnlocked
}
