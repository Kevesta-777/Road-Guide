package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardSchema

internal object RelicHuntChainUnlockDispatcher {
    fun dispatch(
        sourceRelic: LegendaryRelic,
        unlocks: List<RelicHuntChainUnlock>,
        timestampMs: Long,
    ): List<RelicHuntChainUnlockOperation> =
        unlocks.mapNotNull { unlock ->
            when (unlock) {
                is RelicHuntChainUnlock.Achievement ->
                    RelicHuntChainUnlockOperation.AchievementUnlock(
                        achievementKey = unlock.achievementKey,
                        relicHook = relicHookEntity(
                            sourceRelicId = sourceRelic.relicId,
                            subtype = RelicHuntChainSchema.HookSubtypes.ACHIEVEMENT,
                            hookKey = "${RelicHuntChainSchema.HookSubtypes.ACHIEVEMENT}:${unlock.achievementKey}",
                            timestampMs = timestampMs,
                        ),
                        achievementHook = achievementHookEntity(
                            sourceRelicId = sourceRelic.relicId,
                            subtype = RelicHuntChainSchema.HookSubtypes.ACHIEVEMENT,
                            hookKey = "${RelicHuntChainSchema.HookSubtypes.ACHIEVEMENT}:${unlock.achievementKey}",
                            timestampMs = timestampMs,
                        ),
                    )
                is RelicHuntChainUnlock.Badge ->
                    RelicHuntChainUnlockOperation.BadgeUnlock(
                        badgeKey = unlock.badgeKey,
                        hook = relicHookEntity(
                            sourceRelicId = sourceRelic.relicId,
                            subtype = RelicHuntChainSchema.HookSubtypes.BADGE,
                            hookKey = unlock.badgeKey,
                            timestampMs = timestampMs,
                        ),
                    )
                is RelicHuntChainUnlock.Title ->
                    RelicHuntChainUnlockOperation.TitleUnlock(
                        titleKey = unlock.titleKey,
                        hook = relicHookEntity(
                            sourceRelicId = sourceRelic.relicId,
                            subtype = RelicHuntChainSchema.HookSubtypes.TITLE,
                            hookKey = unlock.titleKey,
                            timestampMs = timestampMs,
                        ),
                    )
                is RelicHuntChainUnlock.LegendaryRelic ->
                    RelicHuntChainUnlockOperation.LegendaryRelicUnlock(
                        relicKey = unlock.relicKey,
                        hook = relicHookEntity(
                            sourceRelicId = sourceRelic.relicId,
                            subtype = RelicHuntChainSchema.HookSubtypes.LEGENDARY_RELIC,
                            hookKey = unlock.relicKey,
                            timestampMs = timestampMs,
                        ),
                    )
                is RelicHuntChainUnlock.StoryFragment ->
                    RelicHuntChainUnlockOperation.StoryFragmentUnlock(
                        storyFragmentKey = unlock.storyFragmentKey,
                        hook = relicHookEntity(
                            sourceRelicId = sourceRelic.relicId,
                            subtype = RelicHuntChainSchema.HookSubtypes.STORY_FRAGMENT,
                            hookKey = unlock.storyFragmentKey,
                            timestampMs = timestampMs,
                        ),
                    )
            }
        }

    private fun relicHookEntity(
        sourceRelicId: String,
        subtype: String,
        hookKey: String,
        timestampMs: Long,
    ): LegendaryRelicRewardHookEntity = LegendaryRelicRewardHookEntity(
        eventId = RelicHuntChainSchema.chainUnlockEventId(
            sourceRelicId = sourceRelicId,
            unlockSubtype = subtype,
            hookKey = hookKey,
        ),
        relicId = sourceRelicId,
        hookType = LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK,
        hookKey = hookKey,
        grantedAtMs = timestampMs,
    )

    private fun achievementHookEntity(
        sourceRelicId: String,
        subtype: String,
        hookKey: String,
        timestampMs: Long,
    ): AchievementRewardHookEntity = AchievementRewardHookEntity(
        eventId = "relic_hunt_chain:achievement_unlock:$sourceRelicId:$hookKey",
        achievementId = sourceRelicId,
        hookType = AchievementRewardSchema.HookTypes.CHAIN_UNLOCK,
        hookKey = hookKey,
        grantedAtMs = timestampMs,
    )
}

internal sealed class RelicHuntChainUnlockOperation {
    data class AchievementUnlock(
        val achievementKey: String,
        val relicHook: LegendaryRelicRewardHookEntity,
        val achievementHook: AchievementRewardHookEntity,
    ) : RelicHuntChainUnlockOperation()

    data class BadgeUnlock(
        val badgeKey: String,
        val hook: LegendaryRelicRewardHookEntity,
    ) : RelicHuntChainUnlockOperation()

    data class TitleUnlock(
        val titleKey: String,
        val hook: LegendaryRelicRewardHookEntity,
    ) : RelicHuntChainUnlockOperation()

    data class LegendaryRelicUnlock(
        val relicKey: String,
        val hook: LegendaryRelicRewardHookEntity,
    ) : RelicHuntChainUnlockOperation()

    data class StoryFragmentUnlock(
        val storyFragmentKey: String,
        val hook: LegendaryRelicRewardHookEntity,
    ) : RelicHuntChainUnlockOperation()
}
