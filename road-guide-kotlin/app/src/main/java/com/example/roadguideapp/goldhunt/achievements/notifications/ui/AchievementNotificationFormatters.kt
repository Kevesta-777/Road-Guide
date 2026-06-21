package com.example.roadguideapp.goldhunt.achievements.notifications.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.achievements.notifications.AchievementNotificationReward
import java.util.Locale

internal object AchievementNotificationFormatters {
    fun formatRewardLine(
        context: Context,
        reward: AchievementNotificationReward,
    ): String = when (reward) {
        is AchievementNotificationReward.Credits ->
            context.getString(R.string.achievement_notification_credits, formatCount(reward.amount))
        is AchievementNotificationReward.Xp ->
            context.getString(R.string.achievement_notification_xp, formatCount(reward.amount))
        is AchievementNotificationReward.Title ->
            context.getString(R.string.achievement_notification_title_unlocked, formatHookKey(reward.titleKey))
        is AchievementNotificationReward.RelicProgress ->
            context.getString(R.string.achievement_notification_relic_progress, formatHookKey(reward.relicKey))
        is AchievementNotificationReward.Cosmetic ->
            when (reward.cosmeticType) {
                "storyFragment" -> context.getString(
                    R.string.achievement_notification_story_fragment,
                    formatHookKey(reward.cosmeticKey),
                )
                else -> context.getString(
                    R.string.achievement_notification_cosmetic_unlocked,
                    formatHookKey(reward.cosmeticKey),
                )
            }
    }

    fun formatHookKey(raw: String): String =
        raw.substringAfterLast('_')
            .replace('_', ' ')
            .replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }

    private fun formatCount(value: Long): String =
        String.format(Locale.getDefault(), "%,d", value)

    private fun formatCount(value: Int): String =
        String.format(Locale.getDefault(), "%,d", value)
}
