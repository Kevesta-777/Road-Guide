package com.example.roadguideapp.map

import java.util.Locale
import org.maplibre.android.geometry.LatLng

/**
 * Stable key for linking a map place to a backend `business_pois.external_ref` row.
 */
internal fun MapPlaceDetail.businessPoiExternalRef(): String {
    storedExternalRef?.takeIf { it.isNotBlank() }?.let { return it }
    val featureId = id.trim()
    if (featureId.isNotBlank()) {
        return "feature:$featureId"
    }
    val lat = String.format(Locale.US, "%.5f", latLng.latitude)
    val lng = String.format(Locale.US, "%.5f", latLng.longitude)
    val normalizedName = name.trim().lowercase(Locale.US).replace(Regex("\\s+"), "-")
    return "geo:$lat,$lng:$normalizedName"
}

internal fun lookAroundExternalRef(latLng: LatLng?, title: String): String? {
    if (latLng == null) return null
    val lat = String.format(Locale.US, "%.5f", latLng.latitude)
    val lng = String.format(Locale.US, "%.5f", latLng.longitude)
    val normalizedName = title.trim().lowercase(Locale.US).replace(Regex("\\s+"), "-")
    return "geo:$lat,$lng:$normalizedName"
}
