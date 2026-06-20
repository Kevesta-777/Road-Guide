package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema

internal object AchievementChainUnlockDispatcher {
    fun dispatch(
        sourceAchievement: Achievement,
        unlocks: List<AchievementChainUnlock>,
        timestampMs: Long,
    ): List<AchievementChainUnlockOperation> =
        unlocks.mapNotNull { unlock ->
            when (unlock) {
                is AchievementChainUnlock.Achievement ->
                    AchievementChainUnlockOperation.AchievementUnlock(
                        achievementKey = unlock.achievementKey,
                        hook = hookEntity(
                            sourceAchievementId = sourceAchievement.achievementId,
                            subtype = AchievementChainSchema.HookSubtypes.ACHIEVEMENT,
                            hookKey = "${AchievementChainSchema.HookSubtypes.ACHIEVEMENT}:${unlock.achievementKey}",
                            timestampMs = timestampMs,
                        ),
                    )
                is AchievementChainUnlock.Badge ->
                    AchievementChainUnlockOperation.BadgeUnlock(
                        badgeKey = unlock.badgeKey,
                        hook = hookEntity(
                            sourceAchievementId = sourceAchievement.achievementId,
                            subtype = AchievementChainSchema.HookSubtypes.BADGE,
                            hookKey = unlock.badgeKey,
                            timestampMs = timestampMs,
                        ),
                    )
                is AchievementChainUnlock.Title ->
                    AchievementChainUnlockOperation.TitleUnlock(
                        titleKey = unlock.titleKey,
                        hook = hookEntity(
                            sourceAchievementId = sourceAchievement.achievementId,
                            subtype = AchievementChainSchema.HookSubtypes.TITLE,
                            hookKey = unlock.titleKey,
                            timestampMs = timestampMs,
                        ),
                    )
                is AchievementChainUnlock.LegendaryRelic ->
                    AchievementChainUnlockOperation.LegendaryRelicUnlock(
                        relicKey = unlock.relicKey,
                        hook = hookEntity(
                            sourceAchievementId = sourceAchievement.achievementId,
                            subtype = AchievementChainSchema.HookSubtypes.LEGENDARY_RELIC,
                            hookKey = unlock.relicKey,
                            timestampMs = timestampMs,
                        ),
                    )
                is AchievementChainUnlock.StoryFragment ->
                    AchievementChainUnlockOperation.StoryFragmentUnlock(
                        storyFragmentKey = unlock.storyFragmentKey,
                        hook = hookEntity(
                            sourceAchievementId = sourceAchievement.achievementId,
                            subtype = AchievementChainSchema.HookSubtypes.STORY_FRAGMENT,
                            hookKey = unlock.storyFragmentKey,
                            timestampMs = timestampMs,
                        ),
                    )
            }
        }

    private fun hookEntity(
        sourceAchievementId: String,
        subtype: String,
        hookKey: String,
        timestampMs: Long,
    ): AchievementRewardHookEntity = AchievementRewardHookEntity(
        eventId = AchievementChainSchema.chainUnlockEventId(
            sourceAchievementId = sourceAchievementId,
            unlockSubtype = subtype,
            hookKey = hookKey,
        ),
        achievementId = sourceAchievementId,
        hookType = AchievementRewardSchema.HookTypes.CHAIN_UNLOCK,
        hookKey = hookKey,
        grantedAtMs = timestampMs,
    )
}

internal sealed class AchievementChainUnlockOperation {
    abstract val hook: AchievementRewardHookEntity

    data class AchievementUnlock(
        val achievementKey: String,
        override val hook: AchievementRewardHookEntity,
    ) : AchievementChainUnlockOperation()

    data class BadgeUnlock(
        val badgeKey: String,
        override val hook: AchievementRewardHookEntity,
    ) : AchievementChainUnlockOperation()

    data class TitleUnlock(
        val titleKey: String,
        override val hook: AchievementRewardHookEntity,
    ) : AchievementChainUnlockOperation()

    data class LegendaryRelicUnlock(
        val relicKey: String,
        override val hook: AchievementRewardHookEntity,
    ) : AchievementChainUnlockOperation()

    data class StoryFragmentUnlock(
        val storyFragmentKey: String,
        override val hook: AchievementRewardHookEntity,
    ) : AchievementChainUnlockOperation()
}
