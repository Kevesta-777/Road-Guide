package com.example.roadguideapp.goldhunt.relics.story

internal data class LegendaryRelicStoryChapter(
    val chapterNumber: Int,
    val storyFragmentKey: String,
    val title: String,
    val body: String,
    val pieceNumber: Int,
    val unlocked: Boolean = false,
    val unlockDateMs: Long? = null,
)

internal data class LegendaryRelicStoryLoreEntry(
    val loreIndex: Int,
    val storyFragmentKey: String,
    val title: String,
    val body: String,
    val trigger: LegendaryRelicStoryLoreTrigger,
    val unlocked: Boolean = false,
    val unlockDateMs: Long? = null,
)

internal data class LegendaryRelicStoryCompletionStory(
    val storyFragmentKey: String,
    val title: String,
    val body: String,
    val unlocked: Boolean = false,
    val unlockDateMs: Long? = null,
)

internal data class LegendaryRelicStoryPreview(
    val chapters: List<LegendaryRelicStoryChapter> = emptyList(),
    val loreEntries: List<LegendaryRelicStoryLoreEntry> = emptyList(),
    val completionStory: LegendaryRelicStoryCompletionStory? = null,
    val unlockedFragmentCount: Int = 0,
    val totalFragmentCount: Int = 0,
) {
    val hasStoryContent: Boolean
        get() = chapters.isNotEmpty() || loreEntries.isNotEmpty() || completionStory != null

    val completionFraction: Float
        get() = if (totalFragmentCount <= 0) {
            0f
        } else {
            (unlockedFragmentCount.toFloat() / totalFragmentCount.toFloat()).coerceIn(0f, 1f)
        }
}
