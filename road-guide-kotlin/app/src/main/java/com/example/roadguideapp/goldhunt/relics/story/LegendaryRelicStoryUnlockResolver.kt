package com.example.roadguideapp.goldhunt.relics.story

import com.example.roadguideapp.goldhunt.relics.RelicPiece
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRecord
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookEntity

internal object LegendaryRelicStoryUnlockResolver {
    fun resolve(
        template: LegendaryRelicStoryPreview,
        pieces: List<RelicPiece>,
        masked: Boolean,
        revealed: Boolean,
        visibility: RelicVisibility,
        hiddenRecord: HiddenRelicDiscoveryRecord?,
        grant: LegendaryRelicRewardGrantEntity?,
        rewardHooks: List<LegendaryRelicRewardHookEntity>,
        hiddenStoryHookKeys: Set<String>,
    ): LegendaryRelicStoryPreview {
        if (masked || !template.hasStoryContent) {
            return LegendaryRelicStoryPreview()
        }
        val unlockedKeys = buildUnlockedKeys(
            pieces = pieces,
            revealed = revealed,
            visibility = visibility,
            hiddenRecord = hiddenRecord,
            grant = grant,
            rewardHooks = rewardHooks,
            hiddenStoryHookKeys = hiddenStoryHookKeys,
        )
        val unlockDates = buildUnlockDates(
            pieces = pieces,
            hiddenRecord = hiddenRecord,
            grant = grant,
            rewardHooks = rewardHooks,
        )
        val chapters = template.chapters.map { chapter ->
            val unlocked = chapter.storyFragmentKey in unlockedKeys
            chapter.copy(
                unlocked = unlocked,
                unlockDateMs = unlockDates[chapter.storyFragmentKey],
            )
        }
        val loreEntries = template.loreEntries.map { lore ->
            val unlocked = lore.storyFragmentKey in unlockedKeys
            lore.copy(
                unlocked = unlocked,
                unlockDateMs = unlockDates[lore.storyFragmentKey],
            )
        }
        val completionStory = template.completionStory?.let { story ->
            val unlocked = story.storyFragmentKey in unlockedKeys
            story.copy(
                unlocked = unlocked,
                unlockDateMs = unlockDates[story.storyFragmentKey],
            )
        }
        val unlockedFragmentCount = chapters.count { it.unlocked } +
            loreEntries.count { it.unlocked } +
            if (completionStory?.unlocked == true) 1 else 0
        return LegendaryRelicStoryPreview(
            chapters = chapters,
            loreEntries = loreEntries,
            completionStory = completionStory,
            unlockedFragmentCount = unlockedFragmentCount,
            totalFragmentCount = template.totalFragmentCount,
        )
    }

    private fun buildUnlockedKeys(
        pieces: List<RelicPiece>,
        revealed: Boolean,
        visibility: RelicVisibility,
        hiddenRecord: HiddenRelicDiscoveryRecord?,
        grant: LegendaryRelicRewardGrantEntity?,
        rewardHooks: List<LegendaryRelicRewardHookEntity>,
        hiddenStoryHookKeys: Set<String>,
    ): Set<String> {
        val keys = mutableSetOf<String>()
        rewardHooks
            .filter {
                it.hookType == LegendaryRelicStorySchema.HookTypes.STORY_FRAGMENT ||
                    it.hookType == LegendaryRelicStorySchema.HookTypes.CHAPTER ||
                    it.hookType == LegendaryRelicStorySchema.HookTypes.LORE
            }
            .forEach { keys += it.hookKey }
        hiddenStoryHookKeys.forEach { keys += it }
        pieces.filter { it.discovered }.forEach { piece ->
            keys += RelicSchemaStoryKeys.chapterForPiece(piece.relicId, piece.pieceNumber)
        }
        if (revealed || visibility != RelicVisibility.HIDDEN) {
            pieces.firstOrNull { it.discovered }?.let { piece ->
                keys += RelicSchemaStoryKeys.loreFoundation(piece.relicId)
            }
        }
        if (hiddenRecord?.storyFragmentRecorded == true) {
            hiddenRecord.storyFragmentKey?.let { keys += it }
            hiddenRecord.relicId.let { keys += RelicSchemaStoryKeys.revealForRelic(it) }
        }
        if (grant != null) {
            grant.storyFragmentKey?.let { keys += it }
            val completionLoreIndex = if (visibility == RelicVisibility.HIDDEN) 2 else 1
            keys += RelicSchemaStoryKeys.loreForRelic(grant.relicId, completionLoreIndex)
        }
        return keys
    }

    private fun buildUnlockDates(
        pieces: List<RelicPiece>,
        hiddenRecord: HiddenRelicDiscoveryRecord?,
        grant: LegendaryRelicRewardGrantEntity?,
        rewardHooks: List<LegendaryRelicRewardHookEntity>,
    ): Map<String, Long> {
        val dates = mutableMapOf<String, Long>()
        rewardHooks.forEach { hook ->
            dates.putIfAbsent(hook.hookKey, hook.grantedAtMs)
        }
        pieces.filter { it.discovered }.forEach { piece ->
            piece.discoveryDate?.let { date ->
                dates.putIfAbsent(
                    RelicSchemaStoryKeys.chapterForPiece(piece.relicId, piece.pieceNumber),
                    date,
                )
            }
        }
        hiddenRecord?.revealedAtMs?.let { revealedAt ->
            hiddenRecord.storyFragmentKey?.let { dates.putIfAbsent(it, revealedAt) }
            dates.putIfAbsent(RelicSchemaStoryKeys.revealForRelic(hiddenRecord.relicId), revealedAt)
        }
        grant?.grantedAtMs?.let { grantedAt ->
            grant.storyFragmentKey?.let { dates.putIfAbsent(it, grantedAt) }
        }
        return dates
    }

    private object RelicSchemaStoryKeys {
        fun chapterForPiece(relicId: String, pieceNumber: Int): String =
            com.example.roadguideapp.goldhunt.relics.RelicSchema.StoryFragmentKeys
                .chapterForRelic(relicId, pieceNumber)

        fun loreFoundation(relicId: String): String =
            com.example.roadguideapp.goldhunt.relics.RelicSchema.StoryFragmentKeys
                .loreForRelic(relicId, 0)

        fun revealForRelic(relicId: String): String =
            com.example.roadguideapp.goldhunt.relics.RelicSchema.StoryFragmentKeys
                .revealForRelic(relicId)

        fun loreForRelic(relicId: String, loreIndex: Int): String =
            com.example.roadguideapp.goldhunt.relics.RelicSchema.StoryFragmentKeys
                .loreForRelic(relicId, loreIndex)
    }
}
