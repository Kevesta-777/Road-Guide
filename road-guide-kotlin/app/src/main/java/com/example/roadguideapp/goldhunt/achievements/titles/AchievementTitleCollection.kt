package com.example.roadguideapp.goldhunt.achievements.titles

internal data class AchievementTitleCollection(
    val entries: List<AchievementTitleEntry>,
    val progress: AchievementTitleProgress,
    val activeTitleKey: String?,
) {
    val unlockedEntries: List<AchievementTitleEntry> get() = entries.filter { it.isUnlocked }
    val lockedEntries: List<AchievementTitleEntry> get() = entries.filter { it.isLocked }
    val activeEntry: AchievementTitleEntry? get() = entries.firstOrNull { it.isActive }
}
