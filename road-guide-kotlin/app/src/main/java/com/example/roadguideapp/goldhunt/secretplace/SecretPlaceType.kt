package com.example.roadguideapp.goldhunt.secretplace

internal enum class SecretPlaceType(val id: String) {
    HIDDEN_SHRINE("HIDDEN_SHRINE"),
    HIDDEN_PARK("HIDDEN_PARK"),
    VIEWPOINT("VIEWPOINT"),
    HISTORIC_LANDMARK("HISTORIC_LANDMARK"),
    SCENIC_ROUTE("SCENIC_ROUTE"),
    MYSTERY_LOCATION("MYSTERY_LOCATION"),
    ABANDONED_SITE("ABANDONED_SITE"),
    TREASURE_NEST("TREASURE_NEST"),
    ;

    companion object {
        fun fromId(id: String): SecretPlaceType? = entries.firstOrNull { it.id == id }
    }
}
