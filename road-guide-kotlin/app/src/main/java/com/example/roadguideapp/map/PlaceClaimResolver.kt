package com.example.roadguideapp.map

import android.content.Context
import com.example.roadguideapp.auth.OfflineAuthStore
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class PlaceClaimResolution(
    val mode: PlaceClaimButtonMode,
    val backendPoiId: String?,
)

/** Resolves whether the place detail sheet shows Claim vs Edit business detail. */
internal object PlaceClaimResolver {

    suspend fun resolve(
        context: Context,
        place: MapPlaceDetail,
        isLoggedIn: Boolean,
    ): PlaceClaimResolution {
        if (!place.isClaimEligible && place.backendPoiId.isNullOrBlank()) {
            return PlaceClaimResolution(PlaceClaimButtonMode.Hidden, null)
        }
        if (!isLoggedIn) {
            return PlaceClaimResolution(PlaceClaimButtonMode.Claim, null)
        }

        withContext(Dispatchers.IO) {
            OfflineAuthStore.refreshUserFromBackend(context)
        }
        val token = OfflineAuthStore.sessionToken(context)
            ?: return PlaceClaimResolution(PlaceClaimButtonMode.Claim, null)

        place.backendPoiId?.takeIf { it.isNotBlank() }?.let { poiId ->
            return claimStatusForPoi(token, poiId)
        }

        if (OfflineAuthStore.isBusinessUser(context)) {
            findOwnedMatch(token, place)?.let { ownedPoiId ->
                return PlaceClaimResolution(PlaceClaimButtonMode.BusinessEdit, ownedPoiId)
            }
        }

        val externalRef = place.businessPoiExternalRef()
        val resolveResult = withContext(Dispatchers.IO) {
            BusinessClaimClient.resolvePoi(
                externalRef = externalRef,
                name = place.name,
                address = place.address,
                category = place.category,
                latitude = place.latLng.latitude,
                longitude = place.latLng.longitude,
                bearerToken = token,
            )
        }

        return when (resolveResult) {
            is BusinessClaimClient.ResolveResult.Failure ->
                PlaceClaimResolution(PlaceClaimButtonMode.Claim, null)
            is BusinessClaimClient.ResolveResult.Success -> {
                if (resolveResult.canEditBusiness) {
                    PlaceClaimResolution(PlaceClaimButtonMode.BusinessEdit, resolveResult.poiId)
                } else if (OfflineAuthStore.isBusinessUser(context)) {
                    findOwnedMatch(token, place)?.let { ownedPoiId ->
                        PlaceClaimResolution(PlaceClaimButtonMode.BusinessEdit, ownedPoiId)
                    } ?: claimStatusForPoi(token, resolveResult.poiId)
                } else {
                    claimStatusForPoi(token, resolveResult.poiId)
                }
            }
        }
    }

    private suspend fun claimStatusForPoi(
        token: String,
        poiId: String,
    ): PlaceClaimResolution {
        val statusResult = withContext(Dispatchers.IO) {
            BusinessClaimClient.fetchClaimStatus(poiId, token)
        }
        return when (statusResult) {
            is BusinessClaimClient.ClaimStatusResult.Success ->
                if (statusResult.status.canEditBusiness) {
                    PlaceClaimResolution(PlaceClaimButtonMode.BusinessEdit, poiId)
                } else {
                    PlaceClaimResolution(PlaceClaimButtonMode.Claim, poiId)
                }
            is BusinessClaimClient.ClaimStatusResult.Failure ->
                PlaceClaimResolution(PlaceClaimButtonMode.Claim, poiId)
        }
    }

    private suspend fun findOwnedMatch(
        token: String,
        place: MapPlaceDetail,
    ): String? {
        val mineResult = withContext(Dispatchers.IO) {
            BusinessPoiClient.listMyBusinessPois(token)
        }
        if (mineResult !is BusinessPoiClient.ListMineResult.Success) return null
        return mineResult.pois.firstOrNull { owned -> matchesOwnedPoi(owned, place) }?.id
    }

    private fun matchesOwnedPoi(
        owned: BusinessPoiClient.MyBusinessPoi,
        place: MapPlaceDetail,
    ): Boolean {
        if (place.backendPoiId == owned.id) return true
        val placeRef = place.businessPoiExternalRef()
        if (!owned.externalRef.isNullOrBlank() && owned.externalRef == placeRef) return true
        val ownedLat = owned.latitude ?: return false
        val ownedLng = owned.longitude ?: return false
        if (abs(ownedLat - place.latLng.latitude) > 0.0008) return false
        if (abs(ownedLng - place.latLng.longitude) > 0.0008) return false
        return namesSimilar(owned.name, place.name)
    }

    private fun namesSimilar(left: String, right: String): Boolean {
        val a = left.trim().lowercase(Locale.US)
        val b = right.trim().lowercase(Locale.US)
        if (a.isEmpty() || b.isEmpty()) return true
        return a == b || a.contains(b) || b.contains(a)
    }
}

/** Stable key for claim state when [MapPlaceDetail.id] changes after vector-tile resolution. */
internal fun MapPlaceDetail.stableClaimKey(): String {
    backendPoiId?.takeIf { it.isNotBlank() }?.let { return "poi:$it" }
    storedExternalRef?.takeIf { it.isNotBlank() }?.let { return "ref:$it" }
    val lat = String.format(Locale.US, "%.5f", latLng.latitude)
    val lng = String.format(Locale.US, "%.5f", latLng.longitude)
    val normalizedName = name.trim().lowercase(Locale.US).replace(Regex("\\s+"), "-")
    return "geo:$lat,$lng:$normalizedName"
}
