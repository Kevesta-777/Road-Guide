package com.example.roadguideapp.goldhunt.relics.story

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistryDescriptions

internal object LegendaryRelicStoryCatalog {
    fun completionStoryFragmentKey(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition? = null,
    ): String? {
        if (!RelicSchema.CategoryGates.supportsStoryFragments(relic.category)) return null
        return resolveCompletionStoryFragmentKey(relic.relicId, relic.category, definition)
    }

    fun revealStoryFragmentKey(relicId: String, category: RelicCategory): String? {
        if (!RelicSchema.CategoryGates.supportsStoryFragments(category)) return null
        return RelicSchema.StoryFragmentKeys.revealForRelic(relicId)
    }

    fun buildTemplate(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition?,
        visibility: RelicVisibility,
    ): LegendaryRelicStoryPreview {
        if (!RelicSchema.CategoryGates.supportsStoryFragments(relic.category)) {
            return LegendaryRelicStoryPreview()
        }
        val chapters = buildChapters(relic)
        val loreEntries = buildLoreEntries(relic, definition, visibility)
        val completionStory = buildCompletionStory(relic, definition)
        val totalFragmentCount = chapters.size + loreEntries.size + if (completionStory != null) 1 else 0
        return LegendaryRelicStoryPreview(
            chapters = chapters,
            loreEntries = loreEntries,
            completionStory = completionStory,
            totalFragmentCount = totalFragmentCount,
        )
    }

    private fun resolveCompletionStoryFragmentKey(
        relicId: String,
        category: RelicCategory,
        definition: LegendaryRelicDefinition?,
    ): String = RelicSchema.StoryFragmentKeys.completionForRelic(relicId)

    private fun buildChapters(relic: LegendaryRelic): List<LegendaryRelicStoryChapter> =
        (1..relic.pieceCount).map { pieceNumber ->
            val chapterNumber = pieceNumber
            LegendaryRelicStoryChapter(
                chapterNumber = chapterNumber,
                storyFragmentKey = RelicSchema.StoryFragmentKeys.chapterForRelic(
                    relic.relicId,
                    chapterNumber,
                ),
                title = chapterTitle(relic, chapterNumber),
                body = chapterBody(relic, chapterNumber),
                pieceNumber = pieceNumber,
            )
        }

    private fun buildLoreEntries(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition?,
        visibility: RelicVisibility,
    ): List<LegendaryRelicStoryLoreEntry> {
        val entries = mutableListOf(
            LegendaryRelicStoryLoreEntry(
                loreIndex = 0,
                storyFragmentKey = RelicSchema.StoryFragmentKeys.loreForRelic(relic.relicId, 0),
                title = "Origins of ${relic.name}",
                body = foundationLore(relic, definition),
                trigger = LegendaryRelicStoryLoreTrigger.FOUNDATION,
            ),
        )
        if (visibility == RelicVisibility.HIDDEN) {
            entries += LegendaryRelicStoryLoreEntry(
                loreIndex = 1,
                storyFragmentKey = RelicSchema.StoryFragmentKeys.revealForRelic(relic.relicId),
                title = "Hidden Truth",
                body = "The first shard lifts the veil on ${relic.name}, exposing a path " +
                    "that cartographers never charted.",
                trigger = LegendaryRelicStoryLoreTrigger.REVEAL,
            )
        }
        entries += LegendaryRelicStoryLoreEntry(
            loreIndex = if (visibility == RelicVisibility.HIDDEN) 2 else 1,
            storyFragmentKey = RelicSchema.StoryFragmentKeys.loreForRelic(
                relic.relicId,
                if (visibility == RelicVisibility.HIDDEN) 2 else 1,
            ),
            title = "Legacy of ${relic.category.displayName}",
            body = "Collectors who restore ${relic.name} preserve a ${relic.category.displayName.lowercase()} " +
                "thread in the Gold Hunt saga.",
            trigger = LegendaryRelicStoryLoreTrigger.COMPLETION,
        )
        return entries
    }

    private fun buildCompletionStory(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition?,
    ): LegendaryRelicStoryCompletionStory? {
        val storyFragmentKey = completionStoryFragmentKey(relic, definition) ?: return null
        return LegendaryRelicStoryCompletionStory(
            storyFragmentKey = storyFragmentKey,
            title = "Completion: ${relic.name}",
            body = completionBody(relic, definition),
        )
    }

    private fun chapterTitle(relic: LegendaryRelic, chapterNumber: Int): String =
        if (relic.pieceCount <= 1) {
            relic.name
        } else {
            "Chapter $chapterNumber — ${relic.name}"
        }

    private fun chapterBody(relic: LegendaryRelic, chapterNumber: Int): String =
        if (relic.pieceCount <= 1) {
            relic.description
        } else {
            "Shard $chapterNumber of ${relic.pieceCount} restores another verse of ${relic.name}. " +
                relic.description
        }

    private fun foundationLore(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition?,
    ): String = definition?.description ?: LegendaryRelicRegistryDescriptions.foundation(relic.category)

    private fun completionBody(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition?,
    ): String = "With every shard united, ${relic.name} completes its saga. " +
        (definition?.description ?: relic.description)
}
