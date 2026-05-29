package com.example.roadguideapp.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.roadguideapp.R
import com.google.gson.JsonObject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.geojson.Feature

internal data class AppleNearbyShortcut(
    val labelRes: Int,
    val tint: Color,
    val emoji: String,
    /** Pelias `/nearby` categories filter (fallback when map tiles have few hits). */
    val peliasCategories: String,
) {
    /** Label for Nearby sheet cells (may include emoji). */
    fun markerLabel(): String = emoji

    /** White glyph for map bitmap sprites. */
    fun mapGlyph(): String = when (labelRes) {
        R.string.apple_cat_parking -> "P"
        else -> emoji
    }

    fun markerColorHex(): String = String.format("#%06X", 0xFFFFFF and tint.toArgb())
}

internal object NearbyCategorySearch {

    const val RESULT_LIMIT = 40
    const val POI_LAYERS = "venue"

    val shortcuts: List<AppleNearbyShortcut> = listOf(
        AppleNearbyShortcut(R.string.apple_cat_lunch, Color(0xFFFF9F0A), "🍴", "food"),
        AppleNearbyShortcut(R.string.apple_cat_fast_food, Color(0xFFFF9F0A), "🍔", "food:fast_food"),
        AppleNearbyShortcut(R.string.apple_cat_gas, Color(0xFF0A84FF), "⛽", "retail:fuel"),
        AppleNearbyShortcut(R.string.apple_cat_coffee, Color(0xFFFF9F0A), "☕", "food:coffee"),
        AppleNearbyShortcut(R.string.apple_cat_groceries, Color(0xFFFFCC00), "🛒", "retail"),
        AppleNearbyShortcut(R.string.apple_cat_hotels, Color(0xFFAF52DE), "🛏", "accommodation"),
        AppleNearbyShortcut(R.string.apple_cat_parking, Color(0xFF0A84FF), "P", "transport:parking"),
        AppleNearbyShortcut(R.string.apple_cat_convenience, Color(0xFFFFCC00), "🏪", "retail:convenience"),
        AppleNearbyShortcut(R.string.apple_cat_bars, Color(0xFFFF9F0A), "🍸", "food:bar"),
    )

    fun boundsForResults(results: List<PeliasSearchResult>): LatLngBounds? {
        if (results.isEmpty()) return null
        val builder = LatLngBounds.Builder()
        results.forEach { builder.include(it.latLng) }
        return builder.build()
    }

    /**
     * Matches OpenMapTiles POI tags on features currently drawn on the map.
     */
    fun matchesMapFeature(category: AppleNearbyShortcut, feature: Feature): Boolean {
        val props = feature.properties() ?: return false
        return matchesTags(category, props)
    }

    fun matchesTags(category: AppleNearbyShortcut, props: JsonObject): Boolean {
        val tags = buildSet {
            for (key in TAG_KEYS) {
                props.readTag(key)?.let { add(it) }
            }
        }
        if (tags.isEmpty()) return false

        val matchers = TAG_MATCHERS[category.labelRes].orEmpty()
        return matchers.any { matcher -> tags.any { matcher.matches(it) } }
    }

    fun mergeResults(
        primary: List<PeliasSearchResult>,
        secondary: List<PeliasSearchResult>,
        limit: Int = RESULT_LIMIT,
    ): List<PeliasSearchResult> {
        val seen = HashSet<String>()
        val merged = ArrayList<PeliasSearchResult>(limit)
        for (result in primary + secondary) {
            if (seen.add(result.gid)) {
                merged.add(result)
                if (merged.size >= limit) break
            }
        }
        return merged
    }

    private val TAG_KEYS = listOf(
        "class",
        "subclass",
        "type",
        "amenity",
        "shop",
        "tourism",
        "leisure",
        "transport",
    )

    private data class TagMatcher(val values: Set<String>) {
        fun matches(tag: String): Boolean {
            val normalized = tag.lowercase()
            return values.any { normalized == it || normalized.contains(it) }
        }
    }

    private val TAG_MATCHERS: Map<Int, List<TagMatcher>> = mapOf(
        R.string.apple_cat_lunch to listOf(
            TagMatcher(setOf("restaurant", "food_court", "bistro")),
        ),
        R.string.apple_cat_fast_food to listOf(
            TagMatcher(setOf("fast_food")),
        ),
        R.string.apple_cat_gas to listOf(
            TagMatcher(setOf("fuel", "gas_station", "petrol")),
        ),
        R.string.apple_cat_coffee to listOf(
            TagMatcher(setOf("cafe", "coffee", "coffee_shop")),
        ),
        R.string.apple_cat_groceries to listOf(
            TagMatcher(setOf("supermarket", "grocery", "greengrocer", "mall", "marketplace")),
        ),
        R.string.apple_cat_hotels to listOf(
            TagMatcher(setOf("hotel", "motel", "hostel", "guest_house", "chalet")),
        ),
        R.string.apple_cat_parking to listOf(
            TagMatcher(setOf("parking", "parking_space", "parking_entrance")),
        ),
        R.string.apple_cat_convenience to listOf(
            TagMatcher(setOf("convenience", "kiosk", "variety_store")),
        ),
        R.string.apple_cat_bars to listOf(
            TagMatcher(setOf("bar", "pub", "biergarten", "nightclub", "lounge")),
        ),
    )

    private fun JsonObject.readTag(key: String): String? {
        val value = get(key) ?: return null
        if (value.isJsonNull) return null
        return when {
            value.isJsonPrimitive && value.asJsonPrimitive.isString ->
                value.asString.trim().takeIf { it.isNotEmpty() }
            value.isJsonPrimitive && value.asJsonPrimitive.isNumber ->
                value.asString.trim().takeIf { it.isNotEmpty() }
            else -> null
        }
    }
}
