package com.example.roadguideapp.goldhunt.achievements.journal

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibility

internal data class AchievementJournalEntry(
    val achievementId: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val status: AchievementJournalStatus,
    val visibility: AchievementVisibility = AchievementVisibility.VISIBLE,
    val currentValue: Int,
    val targetValue: Int,
    val progressFraction: Float,
    val completionDateMs: Long?,
    val minimumExplorerLevel: Int,
    val rewardPreview: AchievementJournalRewardPreview,
    val schemaVersion: Int = AchievementJournalSchema.VERSION,
    val extensionJson: String = AchievementJournalSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isCompleted: Boolean get() = status == AchievementJournalStatus.COMPLETED
    val isIncomplete: Boolean get() = status == AchievementJournalStatus.INCOMPLETE
    val isLocked: Boolean get() = status == AchievementJournalStatus.LOCKED
    val isHidden: Boolean get() = status == AchievementJournalStatus.HIDDEN
    val isSecret: Boolean get() = visibility == AchievementVisibility.SECRET
    val isRevealed: Boolean get() = visibility == AchievementVisibility.VISIBLE || isCompleted
    val isVisible: Boolean get() = isRevealed
}
