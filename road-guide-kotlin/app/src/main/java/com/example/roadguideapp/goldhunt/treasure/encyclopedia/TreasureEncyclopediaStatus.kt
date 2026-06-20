package com.example.roadguideapp.goldhunt.treasure.encyclopedia

/** Discovery state for a catalog encyclopedia entry. */
internal enum class TreasureEncyclopediaStatus {
    FOUND,
    MISSING,
    LOCKED,
    ;

    val id: String get() = name

    companion object {
        fun fromId(id: String): TreasureEncyclopediaStatus? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
