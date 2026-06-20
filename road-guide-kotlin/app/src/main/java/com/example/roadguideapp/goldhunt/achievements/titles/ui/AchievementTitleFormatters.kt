package com.example.roadguideapp.goldhunt.achievements.titles.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntry
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleProgress
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class AchievementTitleFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatUnlockedProgress(progress: AchievementTitleProgress): String =
        context.getString(
            R.string.achievement_title_unlocked_progress,
            progress.unlockedCount,
            progress.totalCount,
        )

    fun formatLockedProgress(progress: AchievementTitleProgress): String =
        context.getString(
            R.string.achievement_title_locked_progress,
            progress.lockedCount,
            progress.totalCount,
        )

    fun formatCompletionPercentage(progress: AchievementTitleProgress): String =
        ExplorerProfileFormatters.formatCompletionPercentage(progress.completionFraction)

    fun formatActiveTitleLabel(activeTitle: String?): String? {
        if (activeTitle.isNullOrBlank()) return null
        return context.getString(R.string.achievement_title_active_label, activeTitle)
    }

    fun toEntryUiState(entry: AchievementTitleEntry): AchievementTitleEntryUiState =
        AchievementTitleEntryUiState(
            titleKey = entry.titleKey,
            displayTitle = entry.displayTitle,
            rarityLabel = formatRarity(entry.rarity),
            unlockDateLabel = formatUnlockDate(entry.unlockDateMs, entry.isLocked),
            actionLabel = formatActionLabel(entry),
            isUnlocked = entry.isUnlocked,
            isLocked = entry.isLocked,
            isActive = entry.isActive,
            canActivate = entry.isUnlocked && !entry.isActive,
        )

    private fun formatRarity(rarity: AchievementTitleRarity): String = when (rarity) {
        AchievementTitleRarity.COMMON ->
            context.getString(R.string.achievement_title_rarity_common)
        AchievementTitleRarity.RARE ->
            context.getString(R.string.achievement_title_rarity_rare)
        AchievementTitleRarity.LEGENDARY ->
            context.getString(R.string.achievement_title_rarity_legendary)
    }

    private fun formatUnlockDate(unlockDateMs: Long?, locked: Boolean): String {
        if (locked || unlockDateMs == null || unlockDateMs <= 0L) {
            return context.getString(R.string.achievement_title_not_unlocked)
        }
        return dateFormat.format(Date(unlockDateMs))
    }

    private fun formatActionLabel(entry: AchievementTitleEntry): String? = when {
        entry.isActive -> context.getString(R.string.achievement_title_action_active)
        entry.isUnlocked -> context.getString(R.string.achievement_title_action_set_active)
        else -> null
    }
}
