package com.example.roadguideapp.goldhunt.treasure

import android.content.Context
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import kotlin.random.Random

internal object TreasureHoardCatalog {
    private data class NamedCenter(
        val name: String,
        val lat: Double,
        val lng: Double,
    )

    private val LONDON_REGIONS = listOf(
        NamedCenter("Soho", 51.5134, -0.1367),
        NamedCenter("Camden", 51.5392, -0.1426),
        NamedCenter("Shoreditch", 51.5246, -0.0780),
        NamedCenter("Greenwich", 51.4826, -0.0077),
        NamedCenter("Richmond", 51.4613, -0.3037),
        NamedCenter("Hampstead", 51.5557, -0.1782),
        NamedCenter("Covent Garden", 51.5127, -0.1240),
        NamedCenter("Notting Hill", 51.5099, -0.1963),
    )

    /** Dense treasure pile centered on the secret place the player just discovered. */
    fun fromSecretPlace(spec: SecretPlaceSpec): TreasureHoardRegion {
        val digest = TreasureHash.digest32(spec.secretPlaceId).toLong() and 0xFFFFFFFFL
        return TreasureHoardRegion(
            regionName = spec.displayName,
            lat = spec.lat,
            lng = spec.lng,
            seed = digest xor spec.secretPlaceId.hashCode().toLong(),
        )
    }

    fun rollRegion(context: Context, playRegion: PlayRegion): TreasureHoardRegion =
        rollFromPool(context, LONDON_REGIONS.filter { playRegion.contains(it.lat, it.lng) })

    fun rollRegionWithoutPlayRegion(context: Context): TreasureHoardRegion =
        rollFromPool(context, LONDON_REGIONS)

    private fun rollFromPool(context: Context, pool: List<NamedCenter>): TreasureHoardRegion {
        val candidates = pool.ifEmpty { LONDON_REGIONS }
        val pick = candidates[Random.nextInt(candidates.size)]
        val seed = context.applicationContext.hashCode().toLong() xor System.nanoTime()
        return TreasureHoardRegion(
            regionName = pick.name,
            lat = pick.lat,
            lng = pick.lng,
            seed = seed,
        )
    }
}
