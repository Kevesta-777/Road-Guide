package com.example.roadguideapp.goldhunt.panorama.journal

internal enum class PanoramaHuntJournalStatus(val id: String) {
    MISSING("MISSING"),
    DISCOVERED("DISCOVERED"),
    COMPLETED("COMPLETED"),
    ;

    companion object {
        fun fromId(id: String): PanoramaHuntJournalStatus? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
