package com.example.roadguideapp.map

import android.content.Context
import com.google.gson.JsonObject
import org.maplibre.android.geometry.LatLng
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves human-readable street addresses for map places.
 * Vector tiles often omit OSM addr tags, so Pelias reverse geocoding fills the gap.
 */
internal object PlaceAddressResolver {

    private val reverseCache = ConcurrentHashMap<String, String>()

    private val coordinateAddressPattern = Regex(
        """^\s*-?\d+(?:\.\d+)?\s*,\s*-?\d+(?:\.\d+)?\s*$""",
    )

    fun needsReverseGeocode(address: String): Boolean {
        val trimmed = address.trim()
        return trimmed.isEmpty() || coordinateAddressPattern.matches(trimmed)
    }

    fun formatFromOsmProperties(properties: JsonObject, latLng: LatLng): String {
        readProp(properties, "addr:full", "address")?.let { return it }

        val streetLine = listOfNotNull(
            readProp(properties, "addr:housenumber"),
            readProp(properties, "addr:street"),
        ).joinToString(" ").trim().takeIf { it.isNotEmpty() }

        val localityLine = listOfNotNull(
            readProp(properties, "addr:city", "addr:town", "addr:village", "addr:suburb"),
            readProp(properties, "addr:state", "addr:province"),
            readProp(properties, "addr:postcode"),
            readProp(properties, "addr:country"),
        ).joinToString(", ").trim().takeIf { it.isNotEmpty() }

        val combined = listOfNotNull(streetLine, localityLine)
            .joinToString(", ")
            .trim()

        return combined.ifBlank { formatCoordinateAddress(latLng) }
    }

    suspend fun enrichPlace(context: Context, place: MapPlaceDetail): MapPlaceDetail {
        if (!needsReverseGeocode(place.address)) return place

        val cacheKey = cacheKey(place.latLng)
        val cached = reverseCache[cacheKey]
        if (cached != null) {
            return place.withResolvedAddress(cached)
        }

        when (val response = PeliasSearchClient.reverse(place.latLng)) {
            is PeliasSearchResponse.Success -> {
                val label = response.results.firstOrNull()?.label?.trim().orEmpty()
                if (label.isNotEmpty()) {
                    reverseCache[cacheKey] = label
                    return place.withResolvedAddress(label)
                }
            }
            is PeliasSearchResponse.Failure -> Unit
        }
        return place
    }

    private fun MapPlaceDetail.withResolvedAddress(label: String): MapPlaceDetail {
        val resolvedLocality = locality.ifBlank {
            PlaceMetadataResolver.extractLocalityFromLabel(label, name)
        }
        return copy(address = label, locality = resolvedLocality)
    }

    private fun readProp(properties: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            properties.get(key)?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }?.let {
                return it
            }
        }
        return null
    }

    private fun formatCoordinateAddress(latLng: LatLng): String {
        return "${latLng.latitude}, ${latLng.longitude}"
    }

    private fun cacheKey(latLng: LatLng): String {
        return "%.5f,%.5f".format(latLng.latitude, latLng.longitude)
    }
}
