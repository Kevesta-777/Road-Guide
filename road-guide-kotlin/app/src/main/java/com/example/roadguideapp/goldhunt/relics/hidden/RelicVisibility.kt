package com.example.roadguideapp.goldhunt.relics.hidden

/**
 * Catalog visibility tier for legendary relics.
 *
 * [HIDDEN] relics appear as unknown placeholders until the first piece is discovered.
 */
internal enum class RelicVisibility(val id: String) {
    VISIBLE("visible"),
    HIDDEN("hidden"),
    ;

    companion object {
        fun fromId(id: String): RelicVisibility? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
