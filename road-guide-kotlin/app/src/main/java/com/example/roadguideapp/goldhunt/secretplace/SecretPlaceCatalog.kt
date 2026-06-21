package com.example.roadguideapp.goldhunt.secretplace

import android.content.Context
import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

internal object SecretPlaceCatalog {
    private const val ASSET_PATH = "goldhunt/secret_places_v1.json"

    fun catalogIdsInBounds(
        context: Context,
        bounds: LatLngBounds,
        region: PlayRegion,
    ): Set<String> {
        return placesInBounds(context, bounds, region).map { it.secretPlaceId }.toSet()
    }

    fun placesInBounds(
        context: Context,
        bounds: LatLngBounds,
        region: PlayRegion,
        additionalSecretGeneration: Boolean = false,
    ): List<SecretPlaceSpec> {
        val entries = loadEntries(context)
        val out = ArrayList<SecretPlaceSpec>(entries.size)
        for (entry in entries) {
            if (!bounds.contains(LatLng(entry.lat, entry.lng))) continue
            val l0 = GridIndex.encode(entry.lat, entry.lng, level = 0, region = region) ?: continue
            val l1 = GridIndex.parentL1(l0, region) ?: continue
            val l2 = GridIndex.parentL2(l0, region) ?: continue
            val secretPlaceId = SecretPlaceId.forCatalog(entry.catalogId)
            val type = SecretPlaceType.fromId(entry.type) ?: continue
            val rarity = SecretPlaceRarity.fromId(entry.rarity) ?: SecretPlaceRarity.COMMON
            out += SecretPlaceSpec(
                secretPlaceId = secretPlaceId,
                type = type,
                rarity = rarity,
                lat = entry.lat,
                lng = entry.lng,
                regionL1Id = l1.id,
                regionL2Id = l2.id,
                displayName = entry.displayName,
                treasureNestSlots = entry.treasureNestSlots,
                storyFragmentId = entry.storyFragmentId,
                placementKind = "CATALOG",
                generatorVersion = GoldHuntConfig.SECRET_GENERATOR_VERSION,
            )
        }
        return out
    }

    private fun loadEntries(context: Context): List<CatalogEntry> {
        return runCatching {
            context.assets.open(ASSET_PATH).use { stream ->
                val json = stream.bufferedReader().readText()
                Gson().fromJson(json, CatalogFile::class.java).places
            }
        }.getOrDefault(emptyList())
    }

    private data class CatalogFile(
        val version: Int = 1,
        val places: List<CatalogEntry> = emptyList(),
    )

    private data class CatalogEntry(
        @SerializedName("catalogId") val catalogId: String,
        @SerializedName("displayName") val displayName: String,
        @SerializedName("type") val type: String,
        @SerializedName("rarity") val rarity: String,
        @SerializedName("lat") val lat: Double,
        @SerializedName("lng") val lng: Double,
        @SerializedName("treasureNestSlots") val treasureNestSlots: Int = 0,
        @SerializedName("storyFragmentId") val storyFragmentId: String? = null,
    )
}
