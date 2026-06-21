package com.example.roadguideapp.goldhunt.achievements.journal.ui

import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournal
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalEntry
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalProgress

internal data class AchievementJournalEntryUiState(
    val achievementId: String,
    val displayName: String,
    val categoryLabel: String,
    val statusLabel: String,
    val progressLabel: String,
    val progressFraction: Float,
    val completionDateLabel: String,
    val minimumLevelLabel: String?,
    val rewardPreviewLabel: String,
    val description: String,
    val isCompleted: Boolean,
    val isIncomplete: Boolean,
    val isLocked: Boolean,
    val isHidden: Boolean,
)

internal data class AchievementJournalUiState(
    val completedProgressLabel: String,
    val incompleteProgressLabel: String,
    val lockedProgressLabel: String,
    val hiddenProgressLabel: String,
    val secretProgressLabel: String?,
    val completionPercentageLabel: String,
    val completionProgressFraction: Float,
    val completedEntries: List<AchievementJournalEntryUiState>,
    val incompleteEntries: List<AchievementJournalEntryUiState>,
    val lockedEntries: List<AchievementJournalEntryUiState>,
    val hiddenEntries: List<AchievementJournalEntryUiState>,
) {
    companion object {
        val Empty = AchievementJournalUiState(
            completedProgressLabel = "",
            incompleteProgressLabel = "",
            lockedProgressLabel = "",
            hiddenProgressLabel = "",
            secretProgressLabel = null,
            completionPercentageLabel = "",
            completionProgressFraction = 0f,
            completedEntries = emptyList(),
            incompleteEntries = emptyList(),
            lockedEntries = emptyList(),
            hiddenEntries = emptyList(),
        )

        fun from(journal: AchievementJournal, formatters: AchievementJournalFormatters): AchievementJournalUiState =
            from(journal.entries, journal.progress, formatters)

        fun from(
            entries: List<AchievementJournalEntry>,
            progress: AchievementJournalProgress,
            formatters: AchievementJournalFormatters,
        ): AchievementJournalUiState {
            val uiEntries = entries.map { formatters.toEntryUiState(it) }
            return AchievementJournalUiState(
                completedProgressLabel = formatters.formatCompletedProgress(progress),
                incompleteProgressLabel = formatters.formatIncompleteProgress(progress),
                lockedProgressLabel = formatters.formatLockedProgress(progress),
                hiddenProgressLabel = formatters.formatHiddenProgress(progress),
                secretProgressLabel = formatters.formatSecretProgress(progress),
                completionPercentageLabel = formatters.formatCompletionPercentage(progress),
                completionProgressFraction = progress.completionFraction,
                completedEntries = uiEntries.filter { it.isCompleted },
                incompleteEntries = uiEntries.filter { it.isIncomplete },
                lockedEntries = uiEntries.filter { it.isLocked },
                hiddenEntries = uiEntries.filter { it.isHidden },
            )
        }
    }
}
