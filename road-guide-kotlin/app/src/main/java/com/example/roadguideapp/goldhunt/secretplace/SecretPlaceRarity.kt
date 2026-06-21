package com.example.roadguideapp.goldhunt.secretplace

internal enum class SecretPlaceRarity(val id: String) {
    COMMON("COMMON"),
    RARE("RARE"),
    LEGENDARY("LEGENDARY"),
    ;

    companion object {
        fun fromId(id: String): SecretPlaceRarity? = entries.firstOrNull { it.id == id }

        fun maxOf(a: SecretPlaceRarity, b: SecretPlaceRarity): SecretPlaceRarity =
            if (a.ordinal >= b.ordinal) a else b
    }
}
