package com.example.roadguideapp.goldhunt.achievements.titles

internal data class AchievementTitleEntry(
    val titleKey: String,
    val achievementId: String,
    val displayTitle: String,
    val rarity: AchievementTitleRarity,
    val unlockDateMs: Long?,
    val isActive: Boolean = false,
    val schemaVersion: Int = AchievementTitleSchema.VERSION,
    val extensionJson: String = AchievementTitleSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isUnlocked: Boolean get() = unlockDateMs != null && unlockDateMs > 0L
    val isLocked: Boolean get() = !isUnlocked
}
