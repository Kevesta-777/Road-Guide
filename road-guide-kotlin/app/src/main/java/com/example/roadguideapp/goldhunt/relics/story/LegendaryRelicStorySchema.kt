package com.example.roadguideapp.goldhunt.relics.story

internal object LegendaryRelicStorySchema {
    const val VERSION = 1

    object HookTypes {
        const val STORY_FRAGMENT = "storyFragment"
        const val CHAPTER = "storyChapter"
        const val LORE = "storyLore"
    }

    object EventPrefixes {
        const val CHAPTER = "legendary_relic_story:chapter"
        const val LORE = "legendary_relic_story:lore"
    }

    fun chapterEventId(relicId: String, chapterNumber: Int): String =
        "${EventPrefixes.CHAPTER}:$relicId:$chapterNumber"

    fun loreEventId(relicId: String, loreIndex: Int): String =
        "${EventPrefixes.LORE}:$relicId:$loreIndex"
}
