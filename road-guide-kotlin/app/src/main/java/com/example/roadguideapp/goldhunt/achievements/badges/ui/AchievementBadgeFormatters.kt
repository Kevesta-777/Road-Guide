package com.example.roadguideapp.goldhunt.achievements.badges.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntry
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeProgress
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRarity
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class AchievementBadgeFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatUnlockedProgress(progress: AchievementBadgeProgress): String =
        context.getString(
            R.string.achievement_badge_unlocked_progress,
            progress.unlockedCount,
            progress.totalCount,
        )

    fun formatLockedProgress(progress: AchievementBadgeProgress): String =
        context.getString(
            R.string.achievement_badge_locked_progress,
            progress.lockedCount,
            progress.totalCount,
        )

    fun formatCompletionPercentage(progress: AchievementBadgeProgress): String =
        ExplorerProfileFormatters.formatCompletionPercentage(progress.completionFraction)

    fun toEntryUiState(entry: AchievementBadgeEntry): AchievementBadgeEntryUiState =
        AchievementBadgeEntryUiState(
            badgeKey = entry.badgeKey,
            title = entry.title,
            iconLabel = formatIconLabel(entry.iconKey),
            rarityLabel = formatRarity(entry.rarity),
            unlockDateLabel = formatUnlockDate(entry.unlockDateMs, entry.isLocked),
            isUnlocked = entry.isUnlocked,
            isLocked = entry.isLocked,
        )

    private fun formatIconLabel(iconKey: String): String =
        context.getString(R.string.achievement_badge_icon_label, iconKey)

    private fun formatRarity(rarity: AchievementBadgeRarity): String = when (rarity) {
        AchievementBadgeRarity.COMMON ->
            context.getString(R.string.achievement_badge_rarity_common)
        AchievementBadgeRarity.RARE ->
            context.getString(R.string.achievement_badge_rarity_rare)
        AchievementBadgeRarity.LEGENDARY ->
            context.getString(R.string.achievement_badge_rarity_legendary)
    }

    private fun formatUnlockDate(unlockDateMs: Long?, locked: Boolean): String {
        if (locked || unlockDateMs == null || unlockDateMs <= 0L) {
            return context.getString(R.string.achievement_badge_not_unlocked)
        }
        return dateFormat.format(Date(unlockDateMs))
    }
}
