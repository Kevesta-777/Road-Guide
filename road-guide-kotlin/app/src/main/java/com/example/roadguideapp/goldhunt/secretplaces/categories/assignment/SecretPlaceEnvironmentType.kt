package com.example.roadguideapp.goldhunt.secretplaces.categories.assignment

/**
 * Offline environment classification for secret-place category biasing.
 * Derived from POI/map tags or supplied explicitly by callers.
 */
internal enum class SecretPlaceEnvironmentType {
    URBAN,
    SUBURBAN,
    NATURAL,
    WATERFRONT,
    HISTORIC,
    WILDERNESS,
    MYSTICAL,
    UNKNOWN,
    ;

    val id: String get() = name

    companion object {
        fun fromId(id: String): SecretPlaceEnvironmentType? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
