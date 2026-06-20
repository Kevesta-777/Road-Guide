package com.example.roadguideapp.goldhunt.relics.showcase

internal data class LegendaryRelicShowcaseStoryHighlight(
    val storyFragmentKey: String,
    val title: String,
    val body: String,
    val chaptersUnlocked: Int,
    val chaptersTotal: Int,
    val loreUnlocked: Int,
    val loreTotal: Int,
    val completionUnlocked: Boolean,
    val unlockDateMs: Long?,
)
