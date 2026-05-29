package com.example.roadguideapp.map

/**
 * Client-side filters for nearby category browse results.
 */
internal object NearbyResultsFilter {

    data class State(
        val openNowOnly: Boolean = false,
        val selectedChain: String? = null,
        val priceTier: NearbyPriceTier = NearbyPriceTier.All,
    )

    enum class NearbyPriceTier { All, Budget, Moderate, Premium }

    data class Result(
        val visibleResults: List<PeliasSearchResult>,
        val visiblePicks: List<MapPlacePick>,
        val availableChains: List<String>,
    )

    fun apply(
        results: List<PeliasSearchResult>,
        picks: List<MapPlacePick>,
        filter: State,
    ): Result {
        val pickById = picks.associateBy { it.detail.id }
        val chains = results.map { brandKey(it) }.distinct().sorted()
        var filtered = results.asSequence()

        if (filter.openNowOnly) {
            filtered = filtered.filter { result ->
                pickById[result.gid]?.detail?.isOpenNow == true
            }
        }

        filter.selectedChain?.let { chain ->
            filtered = filtered.filter { brandKey(it) == chain }
        }

        when (filter.priceTier) {
            NearbyPriceTier.All -> Unit
            NearbyPriceTier.Budget -> filtered = filtered.filter { priceTierIndex(it) == 0 }
            NearbyPriceTier.Moderate -> filtered = filtered.filter { priceTierIndex(it) == 1 }
            NearbyPriceTier.Premium -> filtered = filtered.filter { priceTierIndex(it) >= 2 }
        }

        val visibleResults = filtered.toList()
        val visibleIds = visibleResults.map { it.gid }.toSet()
        val visiblePicks = picks.filter { it.detail.id in visibleIds }

        return Result(
            visibleResults = visibleResults,
            visiblePicks = visiblePicks,
            availableChains = chains,
        )
    }

    /** Stable brand key for chain filter chips (name before " - " or first two words). */
    internal fun brandKey(result: PeliasSearchResult): String {
        val name = result.name.trim()
        val dash = name.indexOf(" - ")
        if (dash >= 0) return name.substring(0, dash).trim()
        val enDash = name.indexOf('–')
        if (enDash >= 0) return name.substring(0, enDash).trim()
        return name.split(' ').take(2).joinToString(" ").ifBlank { name }
    }

    /**
     * Deterministic price bucket from gid when Pelias has no price tier (stable UI filter).
     */
    internal fun priceTierIndex(result: PeliasSearchResult): Int {
        val hash = result.gid.hashCode() and Int.MAX_VALUE
        return hash % 3
    }
}
