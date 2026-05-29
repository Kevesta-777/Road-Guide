package com.example.roadguideapp.map

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng

internal data class PeliasSearchResult(
    val gid: String,
    val label: String,
    val name: String,
    val layer: String?,
    val latitude: Double,
    val longitude: Double,
) {
    val latLng: LatLng get() = LatLng(latitude, longitude)

    fun toMapPlaceDetail(context: Context): MapPlaceDetail {
        val category = layer?.replaceFirstChar { c ->
            if (c.isLowerCase()) c.titlecase() else c.toString()
        } ?: context.getString(com.example.roadguideapp.R.string.apple_place_unknown_category)
        val locality = PlaceMetadataResolver.extractLocalityFromLabel(label, name)
        return MapPlaceDetail(
            id = gid,
            name = name,
            category = category,
            locality = locality,
            hoursSummary = PlaceMetadataResolver.unknownHours(context),
            isOpenNow = false,
            website = null,
            phone = null,
            address = label,
            latLng = latLng,
        )
    }

    companion object {
        fun parseCollection(body: String): List<PeliasSearchResult> {
            val root = runCatching { JSONObject(body) }.getOrNull() ?: return emptyList()
            val features = root.optJSONArray("features") ?: return emptyList()
            return buildList {
                for (i in 0 until features.length()) {
                    val feature = features.optJSONObject(i) ?: continue
                    fromFeature(feature)?.let { add(it) }
                }
            }
        }

        fun fromFeature(feature: JSONObject): PeliasSearchResult? {
            val geometry = feature.optJSONObject("geometry") ?: return null
            if (geometry.optString("type") != "Point") return null
            val coords = geometry.optJSONArray("coordinates") ?: return null
            if (coords.length() < 2) return null
            val lon = coords.optDouble(0)
            val lat = coords.optDouble(1)
            if (!lon.isFinite() || !lat.isFinite()) return null

            val props = feature.optJSONObject("properties") ?: return null
            val gid = props.optString("gid").takeIf { it.isNotEmpty() }
                ?: props.optString("id").takeIf { it.isNotEmpty() }
                ?: buildFallbackGid(props)
                ?: return null
            val name = props.optString("name").takeIf { it.isNotEmpty() }
                ?: props.optString("label").takeIf { it.isNotEmpty() }
                ?: return null
            val label = props.optString("label").takeIf { it.isNotEmpty() } ?: name
            val layer = props.optString("layer").takeIf { it.isNotEmpty() }

            return PeliasSearchResult(
                gid = gid,
                label = label,
                name = name,
                layer = layer,
                latitude = lat,
                longitude = lon,
            )
        }

        private fun buildFallbackGid(props: JSONObject): String? {
            val source = props.optString("source").takeIf { it.isNotEmpty() } ?: return null
            val layer = props.optString("layer").takeIf { it.isNotEmpty() } ?: return null
            val id = props.optString("osm_id").takeIf { it.isNotEmpty() }
                ?: props.optString("id").takeIf { it.isNotEmpty() }
                ?: return null
            return "$source:$layer:$id"
        }
    }
}
