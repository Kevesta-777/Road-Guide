package com.example.roadguideapp.goldhunt.relics.story

internal enum class LegendaryRelicStoryLoreTrigger(val id: String) {
    FOUNDATION("foundation"),
    REVEAL("reveal"),
    COMPLETION("completion"),
    ;

    companion object {
        fun fromId(id: String): LegendaryRelicStoryLoreTrigger? =
            entries.firstOrNull { it.id == id }
    }
}
