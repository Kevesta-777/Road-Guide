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
 * 1. Map viewport + search context (center, place, or route corridor)
 * 2. Category filtering
 * 3. POI indexing (dedupe by stable id)
 * 4. Ranking (distance from context)
 * 5. Progressive search expansion (viewport Pelias → expanded bounds → context-specific Pelias)
 */
internal object NearbyCategorySearchEngine {

    const val TARGET_MIN_RESULTS = 5
    const val RESULT_LIMIT = NearbyCategorySearch.RESULT_LIMIT
    private const val MAX_ROUTE_PELIAS_SAMPLES = 10

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
        searchContext: NearbySearchContext,
    ): SearchOutcome = withContext(Dispatchers.Main) {
        val mapCenter = MapViewportBounds.center(map)
            ?: return@withContext SearchOutcome(emptyList(), emptyMap())
        val viewportBounds = MapViewportBounds.visibleBounds(map, mapView)

        val viewportHighlights = if (style != null) {
            NearbyMapPoiQuery.queryVisibleCategory(
                context = context,
                style = style,
                map = map,
                mapView = mapView,
                category = category,
                searchContext = searchContext,
                mapCenter = mapCenter,
            )
        } else {
            emptyList()
        }

        var indexed = indexAndRank(
            searchContext = searchContext,
            mapCenter = mapCenter,
            entries = viewportHighlights.map { it.result to it.pick },
        )

        var peliasError: String? = null

        val primaryBounds = primarySearchBounds(searchContext, mapCenter, viewportBounds)

        if (indexed.size < TARGET_MIN_RESULTS && primaryBounds != null) {
            when (
                val response = withContext(Dispatchers.IO) {
                    PeliasSearchClient.nearbyInBounds(
                        categories = category.peliasCategories,
                        bounds = primaryBounds,
                        size = RESULT_LIMIT,
                    )
                }
            ) {
                is PeliasSearchResponse.Success -> {
                    indexed = mergeExpansion(
                        searchContext = searchContext,
                        mapCenter = mapCenter,
                        existing = indexed,
                        incoming = filterByContext(searchContext, mapCenter, response.results),
                        style = style,
                        map = map,
                        context = context,
                    )
                }
                is PeliasSearchResponse.Failure -> peliasError = response.message
            }
        }

        if (indexed.size < TARGET_MIN_RESULTS && primaryBounds != null) {
            val expanded = MapViewportBounds.expand(primaryBounds, factor = 1.6)
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
                            searchContext = searchContext,
                            mapCenter = mapCenter,
                            existing = indexed,
                            incoming = filterByContext(searchContext, mapCenter, response.results),
                            style = style,
                            map = map,
                            context = context,
                        )
                    }
                    is PeliasSearchResponse.Failure -> peliasError = peliasError ?: response.message
                }
            }
        }

        val needsContextPelias = indexed.size < TARGET_MIN_RESULTS ||
            searchContext is NearbySearchContext.AlongRoute
        if (needsContextPelias) {
            val pointResults = withContext(Dispatchers.IO) {
                fetchContextPointResults(category, searchContext, mapCenter)
            }
            when (pointResults) {
                is PeliasSearchResponse.Success -> {
                    indexed = mergeExpansion(
                        searchContext = searchContext,
                        mapCenter = mapCenter,
                        existing = indexed,
                        incoming = filterByContext(searchContext, mapCenter, pointResults.results),
                        style = style,
                        map = map,
                        context = context,
                    )
                }
                is PeliasSearchResponse.Failure -> peliasError = peliasError ?: pointResults.message
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

    private fun primarySearchBounds(
        searchContext: NearbySearchContext,
        mapCenter: LatLng,
        viewportBounds: LatLngBounds?,
    ): LatLngBounds? = when (searchContext) {
        // Route corridors are covered by point samples in [fetchContextPointResults]; Pelias
        // `/nearby` does not support full-route rects and caps circle radius (~5 km).
        is NearbySearchContext.AlongRoute -> null
        is NearbySearchContext.NearPlace -> {
            PolylineDistance.boundsWithBuffer(
                listOf(searchContext.location),
                NearbySearchContext.NEAR_PLACE_BOUNDS_PADDING_METERS,
            ) ?: viewportBounds
        }
        NearbySearchContext.MapCenter -> viewportBounds
    }

    private suspend fun fetchContextPointResults(
        category: AppleNearbyShortcut,
        searchContext: NearbySearchContext,
        mapCenter: LatLng,
    ): PeliasSearchResponse = when (searchContext) {
        is NearbySearchContext.AlongRoute -> {
            PeliasSearchClient.nearbyAlongPolyline(
                categories = category.peliasCategories,
                polyline = searchContext.polyline,
                corridorMeters = searchContext.radiusMeters,
                maxSamples = MAX_ROUTE_PELIAS_SAMPLES,
                size = RESULT_LIMIT,
            )
        }
        is NearbySearchContext.NearPlace -> {
            PeliasSearchClient.nearby(
                categories = category.peliasCategories,
                point = searchContext.location,
                size = RESULT_LIMIT,
            )
        }
        NearbySearchContext.MapCenter -> {
            PeliasSearchClient.nearby(
                categories = category.peliasCategories,
                point = mapCenter,
                size = RESULT_LIMIT,
            )
        }
    }

    private fun filterByContext(
        searchContext: NearbySearchContext,
        mapCenter: LatLng,
        results: List<PeliasSearchResult>,
    ): List<PeliasSearchResult> =
        results.filter { searchContext.includes(it.latLng, mapCenter) }

    internal fun indexAndRank(
        searchContext: NearbySearchContext,
        mapCenter: LatLng,
        entries: List<Pair<PeliasSearchResult, MapPlacePick?>>,
    ): List<IndexedPoi> {
        val byGid = LinkedHashMap<String, IndexedPoi>()
        for ((result, pick) in entries) {
            if (!searchContext.includes(result.latLng, mapCenter)) continue
            val distance = searchContext.distanceMeters(result.latLng, mapCenter)
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
        searchContext: NearbySearchContext,
        mapCenter: LatLng,
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
        return indexAndRank(searchContext, mapCenter, pairs)
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
