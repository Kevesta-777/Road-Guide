package com.example.roadguideapp.map

import com.google.gson.JsonObject

/** Whether a map place can show the claim / business-edit action. */
internal object PlaceClaimEligibility {

    private val geographicSubtypes = setOf(
        "country", "state", "region", "district", "county", "city", "town", "village",
        "suburb", "neighbourhood", "neighborhood", "quarter", "postcode", "island",
        "locality", "borough", "municipality", "continent", "province", "hamlet",
        "farm", "allotments", "isolated_dwelling",
    )

    private val geographicClasses = setOf(
        "place", "boundary", "waterway", "water", "landuse", "landcover",
        "transportation", "transportation_name", "highway", "road", "street",
        "aeroway", "mountain_peak", "natural", "water_name",
    )

    private val geographicLayers = setOf(
        "water", "waterway", "transportation", "transportation_name", "boundary",
        "place", "place_label", "housenumber", "water_name",
    )

    private val waterwaySubtypes = setOf(
        "river", "stream", "canal", "drain", "ditch", "brook",
    )

    private val ineligiblePeliasLayers = setOf(
        "continent", "country", "region", "macroregion", "macrocounty", "county",
        "localadmin", "locality", "borough", "neighbourhood", "neighborhood",
        "street", "address", "postalcode", "marine", "ocean", "empire",
    )

    /**
     * Business map POIs render both a sprite icon and a name label on their symbol layer.
     * Icon-only or label-only presentations are not claim targets.
     */
    fun hasBusinessPoiPresentation(hasSpriteIcon: Boolean, hasLabelText: Boolean): Boolean {
        return hasSpriteIcon && hasLabelText
    }

    fun forMapSymbolPresentation(
        hasSpriteIcon: Boolean,
        hasLabelText: Boolean,
        properties: JsonObject,
        category: String,
    ): Boolean {
        if (!hasBusinessPoiPresentation(hasSpriteIcon, hasLabelText)) return false
        return fromOsmProperties(properties, category)
    }

    /**
     * Map POI taps: allow claim by default unless the feature is clearly geographic
     * (city, river, street, etc.). Headway / OpenMapTiles often store the specific
     * business type in [class] (e.g. restaurant) rather than amenity/shop keys.
     */
    fun fromOsmProperties(properties: JsonObject, category: String): Boolean {
        return !isExplicitlyIneligible(properties, category)
    }

    fun fromPeliasLayer(layer: String?): Boolean {
        val normalized = layer?.trim()?.lowercase().orEmpty()
        if (normalized.isBlank()) return false
        if (normalized in ineligiblePeliasLayers) return false
        return normalized in setOf("venue", "poi")
    }

    private fun isExplicitlyIneligible(properties: JsonObject, category: String): Boolean {
        val placeClass = readProp(properties, "class")?.lowercase().orEmpty()
        val subclass = readProp(properties, "subclass")?.lowercase().orEmpty()
        val type = readProp(properties, "type")?.lowercase().orEmpty()
        val layer = readProp(properties, "layer")?.lowercase()
        val categoryLower = category.lowercase()

        if (layer != null && layer in geographicLayers) return true
        if (placeClass in geographicClasses) return true
        if (subclass in geographicSubtypes || type in geographicSubtypes) return true
        if (categoryLower in geographicSubtypes || categoryLower in geographicClasses) return true
        if (subclass == "bridge" || type == "bridge" || categoryLower == "bridge") return true
        if (placeClass == "waterway" || subclass in waterwaySubtypes || type in waterwaySubtypes) return true
        // Standalone buildings without a business subtype are not claim targets.
        if (placeClass == "building" && subclass.isBlank() && type.isBlank()) return true
        return false
    }

    private fun readProp(properties: JsonObject, key: String): String? {
        return properties.get(key)?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
    }
}
