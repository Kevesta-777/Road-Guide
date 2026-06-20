package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

/** Catalog state for a secret-place category in the collection book. */
internal enum class SecretPlaceCollectionBookStatus {
    MISSING,
    DISCOVERED,
    COMPLETED,
    ;

    val id: String get() = name

    companion object {
        fun fromId(id: String): SecretPlaceCollectionBookStatus? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
