package com.example.roadguideapp.goldhunt.relics.journal

internal enum class LegendaryRelicJournalStatus(val id: String) {
    COMPLETED("completed"),
    IN_PROGRESS("inProgress"),
    MISSING("missing"),
    LOCKED("locked"),
    HIDDEN_MASKED("hiddenMasked"),
    ;

    companion object {
        fun fromId(id: String): LegendaryRelicJournalStatus? =
            entries.firstOrNull { it.id == id }
    }
}
