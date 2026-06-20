package com.example.roadguideapp.goldhunt.achievements.badges

internal data class AchievementBadgeCollection(
    val entries: List<AchievementBadgeEntry>,
    val progress: AchievementBadgeProgress,
) {
    val unlockedEntries: List<AchievementBadgeEntry> get() = entries.filter { it.isUnlocked }
    val lockedEntries: List<AchievementBadgeEntry> get() = entries.filter { it.isLocked }
}
