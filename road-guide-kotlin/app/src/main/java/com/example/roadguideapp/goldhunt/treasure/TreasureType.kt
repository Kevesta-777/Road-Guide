package com.example.roadguideapp.goldhunt.treasure

internal enum class TreasureType(val id: String) {
    STAR("STAR"),
    FLOWER("FLOWER"),
    CRYSTAL("CRYSTAL"),
    GIFT("GIFT"),
    HINT("HINT"),
    ;

    companion object {
        fun fromId(id: String): TreasureType? = entries.firstOrNull { it.id == id }
    }
}
