package com.example.roadguideapp.map

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * Nearby category pipeline:
 * 1. Map viewport + center point
 * 2. Category filtering
 * 3. POI indexing (dedupe by stable id)
 * 4. Ranking (distance from center)
 * 5. Progressive search expansion (viewport Pelias → expanded bounds → wider Pelias)
 */
internal object NearbyCategorySearchEngine {

    const val TARGET_MIN_RESULTS = 5
    const val RESULT_LIMIT = NearbyCategorySearch.RESULT_LIMIT

    data class IndexedPoi(
        val result: PeliasSearchResult,
        val pick: MapPlacePick?,
        val distanceMeters: Double,
    )

    data class SearchOutcome(
        val ranked: List<IndexedPoi>,
        val picksByGid: Map<String, MapPlacePick>,
        val errorMessage: String? = null,
    )

    suspend fun search(
        context: Context,
        style: Style?,
        map: MapLibreMap,
        mapView: MapView,
        category: AppleNearbyShortcut,
    ): SearchOutcome = withContext(Dispatchers.Main) {
        val center = MapViewportBounds.center(map) ?: return@withContext SearchOutcome(emptyList(), emptyMap())
        val viewportBounds = MapViewportBounds.visibleBounds(map, mapView)

        // Phase 1 — vector tiles currently drawn in the viewport.
        val viewportHighlights = if (style != null) {
            NearbyMapPoiQuery.queryVisibleCategory(
                context = context,
                style = style,
                map = map,
                mapView = mapView,
                category = category,
            )
        } else {
            emptyList()
        }

        var indexed = indexAndRank(
            center = center,
            entries = viewportHighlights.map { it.result to it.pick },
        )

        var peliasError: String? = null

        // Phase 2 — Pelias nearby within current viewport bounds.
        if (indexed.size < TARGET_MIN_RESULTS && viewportBounds != null) {
            when (
                val response = withContext(Dispatchers.IO) {
                    PeliasSearchClient.nearbyInBounds(
                        categories = category.peliasCategories,
                        bounds = viewportBounds,
                        size = RESULT_LIMIT,
                    )
                }
            ) {
                is PeliasSearchResponse.Success -> {
                    indexed = mergeExpansion(
                        center = center,
                        existing = indexed,
                        incoming = response.results,
                        style = style,
                        map = map,
                        context = context,
                    )
                }
                is PeliasSearchResponse.Failure -> peliasError = response.message
            }
        }

        // Phase 3 — expand bounds (~1.6×) and query Pelias again.
        if (indexed.size < TARGET_MIN_RESULTS && viewportBounds != null) {
            val expanded = MapViewportBounds.expand(viewportBounds, factor = 1.6)
            if (expanded != null) {
                when (
                    val response = withContext(Dispatchers.IO) {
                        PeliasSearchClient.nearbyInBounds(
                            categories = category.peliasCategories,
                            bounds = expanded,
                            size = RESULT_LIMIT,
                        )
                    }
                ) {
                    is PeliasSearchResponse.Success -> {
                        indexed = mergeExpansion(
                            center = center,
                            existing = indexed,
                            incoming = response.results,
                            style = style,
                            map = map,
                            context = context,
                        )
                    }
                    is PeliasSearchResponse.Failure -> peliasError = peliasError ?: response.message
                }
            }
        }

        // Phase 4 — point-based Pelias fallback around map center.
        if (indexed.size < TARGET_MIN_RESULTS) {
            when (
                val response = withContext(Dispatchers.IO) {
                    PeliasSearchClient.nearby(
                        categories = category.peliasCategories,
                        point = center,
                        size = RESULT_LIMIT,
                    )
                }
            ) {
                is PeliasSearchResponse.Success -> {
                    indexed = mergeExpansion(
                        center = center,
                        existing = indexed,
                        incoming = response.results,
                        style = style,
                        map = map,
                        context = context,
                    )
                }
                is PeliasSearchResponse.Failure -> peliasError = peliasError ?: response.message
            }
        }

        val capped = indexed.take(RESULT_LIMIT)
        val picksByGid = capped.mapNotNull { poi ->
            poi.pick?.let { poi.result.gid to it }
        }.toMap()

        SearchOutcome(
            ranked = capped,
            picksByGid = picksByGid,
            errorMessage = if (capped.isEmpty()) peliasError else null,
        )
    }

    internal fun indexAndRank(
        center: LatLng,
        entries: List<Pair<PeliasSearchResult, MapPlacePick?>>,
    ): List<IndexedPoi> {
        val byGid = LinkedHashMap<String, IndexedPoi>()
        for ((result, pick) in entries) {
            val distance = DirectionsPathOptimizer.haversineMeters(center, result.latLng)
            val existing = byGid[result.gid]
            if (existing == null || distance < existing.distanceMeters) {
                byGid[result.gid] = IndexedPoi(
                    result = result,
                    pick = pick ?: existing?.pick,
                    distanceMeters = distance,
                )
            } else if (pick != null && existing.pick == null) {
                byGid[result.gid] = existing.copy(pick = pick)
            }
        }
        return byGid.values.sortedBy { it.distanceMeters }
    }

    private fun mergeExpansion(
        center: LatLng,
        existing: List<IndexedPoi>,
        incoming: List<PeliasSearchResult>,
        style: Style?,
        map: MapLibreMap,
        context: Context,
    ): List<IndexedPoi> {
        val pairs = ArrayList<Pair<PeliasSearchResult, MapPlacePick?>>(existing.size + incoming.size)
        existing.forEach { pairs.add(it.result to it.pick) }
        for (result in incoming) {
            val pick = style?.let {
                MapPoiSelectionController.resolvePickNear(context, it, map, result.latLng)
            }
            pairs.add(result to pick)
        }
        return indexAndRank(center, pairs)
    }

    fun boundsForIndexed(pois: List<IndexedPoi>): LatLngBounds? =
        NearbyCategorySearch.boundsForResults(pois.map { it.result })

    fun findNearestResult(
        results: List<PeliasSearchResult>,
        tap: LatLng,
        maxDistanceMeters: Double = 80.0,
    ): PeliasSearchResult? {
        var best: PeliasSearchResult? = null
        var bestDistance = maxDistanceMeters
        for (result in results) {
            val distance = DirectionsPathOptimizer.haversineMeters(tap, result.latLng)
            if (distance <= bestDistance) {
                bestDistance = distance
                best = result
            }
        }
        return best
    }
}
