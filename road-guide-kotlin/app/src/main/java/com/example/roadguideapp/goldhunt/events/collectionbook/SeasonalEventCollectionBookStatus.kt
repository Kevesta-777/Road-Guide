package com.example.roadguideapp.goldhunt.events.collectionbook

internal enum class SeasonalEventCollectionBookStatus(val id: String) {
    MISSING("missing"),
    PARTICIPATED("participated"),
    COMPLETED("completed"),
    ;

    companion object {
        fun fromId(id: String): SeasonalEventCollectionBookStatus? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
