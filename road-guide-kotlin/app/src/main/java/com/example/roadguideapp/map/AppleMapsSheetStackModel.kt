package com.example.roadguideapp.map

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Home, place detail, directions, and nearby browse share one detent (peek / mid / large). */
internal fun AppleMapSheet.isSyncedStackSheet(): Boolean = when (this) {
    is AppleMapSheet.Home,
    is AppleMapSheet.PlaceDetail,
    is AppleMapSheet.Directions,
    is AppleMapSheet.NearbyBrowse,
    -> true
    is AppleMapSheet.AddStop -> false
    is AppleMapSheet.UserProfile -> false
}

/** Sheets that must render a solid surface instead of the map blur material. */
internal fun AppleMapSheet.usesOpaqueSheetSurface(): Boolean = when (this) {
    is AppleMapSheet.UserProfile,
    is AppleMapSheet.AddStop,
    -> true
    else -> false
}

/** Identifies a sheet in the stacked bottom-sheet UI. */
internal sealed class AppleMapSheet {
    abstract val stackId: String

    data object Home : AppleMapSheet() {
        override val stackId: String = "home"
    }

    data class PlaceDetail(val place: MapPlaceDetail) : AppleMapSheet() {
        override val stackId: String = "place_${place.id}"
    }

    data class Directions(
        val origin: MapPlaceDetail,
        val stops: List<MapPlaceDetail> = emptyList(),
        val travelMode: DirectionsTravelMode = DirectionsTravelMode.Drive,
        /** Full trip in visit order (start → … → current destination) for cumulative routing. */
        val tripWaypoints: List<MapPlaceDetail> = listOf(origin),
    ) : AppleMapSheet() {
        override val stackId: String = "directions"

        val destination: MapPlaceDetail? get() = stops.lastOrNull()

        /**
         * Adds [place] as the new destination. The previous destination becomes the new origin
         * (sheet shows B→C), while [tripWaypoints] grows so the map can draw every leg (A→B→C…).
         */
        fun withRotatedDestination(place: MapPlaceDetail): Directions {
            val previousDestination = stops.lastOrNull()
            val extendedTrip = tripWaypoints + place
            return if (previousDestination == null) {
                copy(stops = listOf(place), tripWaypoints = extendedTrip)
            } else {
                copy(
                    origin = previousDestination,
                    stops = listOf(place),
                    tripWaypoints = extendedTrip,
                )
            }
        }
    }

    data object AddStop : AppleMapSheet() {
        override val stackId: String = "add_stop"
    }

    data class UserProfile(
        val selectedBusinessPoiId: String? = null,
    ) : AppleMapSheet() {
        override val stackId: String = "user_profile"
    }

    /** Nearby category results overlay; dismiss returns to the sheet underneath. */
    data object NearbyBrowse : AppleMapSheet() {
        override val stackId: String = "nearby_browse"
    }
}

internal data class AppleMapSheetLayer(
    val sheet: AppleMapSheet,
    val snap: AppleSheetSnap = AppleSheetSnap.Mid,
    val presentationKey: Int = 0,
)

@Stable
internal class AppleMapsSheetStackState(
    initialLayers: List<AppleMapSheetLayer> = listOf(
        AppleMapSheetLayer(AppleMapSheet.Home, AppleSheetSnap.Peek),
    ),
) {
    var layers by mutableStateOf(initialLayers)
        private set

    private var nextPresentationKey by mutableIntStateOf(1)

    val topLayer: AppleMapSheetLayer? get() = layers.lastOrNull()
    val hasOverlay: Boolean get() = layers.size > 1

    fun activeDirections(): AppleMapSheet.Directions? =
        layers.map { it.sheet }.filterIsInstance<AppleMapSheet.Directions>().lastOrNull()

    fun topPlaceDetail(): AppleMapSheet.PlaceDetail? =
        layers.map { it.sheet }.filterIsInstance<AppleMapSheet.PlaceDetail>().lastOrNull()

    fun topUserProfile(): AppleMapSheet.UserProfile? =
        layers.map { it.sheet }.filterIsInstance<AppleMapSheet.UserProfile>().lastOrNull()

    /** Detent shared by [AppleMapSheet.isSyncedStackSheet] layers (top synced layer wins). */
    fun currentSyncedSnap(): AppleSheetSnap =
        layers.lastOrNull { it.sheet.isSyncedStackSheet() }?.snap
            ?: layers.firstOrNull()?.snap
            ?: AppleSheetSnap.Peek

    /**
     * Pushes [sheet] onto the stack. If replacing Home, animates the first sheet in.
     * Synced sheets inherit the stack's current detent so every layer stays aligned.
     */
    fun push(sheet: AppleMapSheet, targetSnap: AppleSheetSnap? = null) {
        if (layers.lastOrNull()?.sheet?.stackId == sheet.stackId) return

        val key = nextPresentationKey++
        val withoutDuplicate = layers.filter { it.sheet.stackId != sheet.stackId }
        val inheritedSnap = targetSnap
            ?: currentSyncedSnap()
            ?: AppleSheetSnap.Mid
        val snapForNewLayer = if (sheet.isSyncedStackSheet()) inheritedSnap else (targetSnap ?: AppleSheetSnap.Mid)
        val base = ensureHomeBase(withoutDuplicate)
        layers = base + AppleMapSheetLayer(sheet, snapForNewLayer, key)
    }

    private fun ensureHomeBase(layers: List<AppleMapSheetLayer>): List<AppleMapSheetLayer> {
        if (layers.isEmpty()) {
            return listOf(AppleMapSheetLayer(AppleMapSheet.Home, AppleSheetSnap.Peek))
        }
        return if (layers.first().sheet is AppleMapSheet.Home) {
            layers
        } else {
            listOf(AppleMapSheetLayer(AppleMapSheet.Home, AppleSheetSnap.Peek)) +
                layers.filter { it.sheet !is AppleMapSheet.Home }
        }
    }

    fun pop() {
        layers = when {
            layers.size <= 1 -> listOf(AppleMapSheetLayer(AppleMapSheet.Home, AppleSheetSnap.Peek))
            else -> layers.dropLast(1)
        }
    }

    /** Closes Add Stop search and destination place sheets after a stop is confirmed. */
    fun popAddStopOverlays() {
        while (layers.lastOrNull()?.sheet is AppleMapSheet.AddStop ||
            layers.lastOrNull()?.sheet is AppleMapSheet.PlaceDetail
        ) {
            pop()
        }
    }

    fun clearToHome() {
        layers = listOf(AppleMapSheetLayer(AppleMapSheet.Home, AppleSheetSnap.Peek))
        nextPresentationKey = 1
    }

    fun removeDirectionsAndAddStop() {
        val remaining = layers.filter { layer ->
            layer.sheet !is AppleMapSheet.Directions && layer.sheet !is AppleMapSheet.AddStop
        }
        layers = ensureHomeBase(remaining)
    }

    fun removeNearbyBrowse() {
        val remaining = layers.filter { it.sheet !is AppleMapSheet.NearbyBrowse }
        layers = ensureHomeBase(remaining)
    }

    fun hasNearbyBrowse(): Boolean = layers.any { it.sheet is AppleMapSheet.NearbyBrowse }

    fun updateSnap(layerIndex: Int, snap: AppleSheetSnap) {
        val sheet = layers.getOrNull(layerIndex)?.sheet
        if (sheet?.isSyncedStackSheet() == true) {
            updateAllSyncedSnaps(snap)
        } else {
            layers = layers.mapIndexed { index, layer ->
                if (index == layerIndex) layer.copy(snap = snap) else layer
            }
        }
    }

    /** Keeps Home, place detail, and directions on the same peek / mid / large detent. */
    fun updateAllSyncedSnaps(snap: AppleSheetSnap) {
        layers = layers.map { layer ->
            if (layer.sheet.isSyncedStackSheet()) layer.copy(snap = snap) else layer
        }
    }

    fun updateUserProfileSelection(selectedBusinessPoiId: String?) {
        layers = layers.map { layer ->
            if (layer.sheet is AppleMapSheet.UserProfile) {
                layer.copy(
                    sheet = AppleMapSheet.UserProfile(selectedBusinessPoiId = selectedBusinessPoiId),
                    snap = if (selectedBusinessPoiId != null) {
                        AppleSheetSnap.ProfileEdit
                    } else {
                        AppleSheetSnap.Large
                    },
                )
            } else {
                layer
            }
        }
    }

    fun updatePlaceDetail(placeId: String, place: MapPlaceDetail) {
        layers = layers.map { layer ->
            if (layer.sheet is AppleMapSheet.PlaceDetail && layer.sheet.place.id == placeId) {
                layer.copy(sheet = AppleMapSheet.PlaceDetail(place))
            } else {
                layer
            }
        }
    }

    fun updateDirections(
        origin: MapPlaceDetail,
        stops: List<MapPlaceDetail>,
        travelMode: DirectionsTravelMode? = null,
        tripWaypoints: List<MapPlaceDetail>? = null,
    ) {
        layers = layers.map { layer ->
            if (layer.sheet is AppleMapSheet.Directions) {
                val prev = layer.sheet
                layer.copy(
                    sheet = AppleMapSheet.Directions(
                        origin = origin,
                        stops = stops,
                        travelMode = travelMode ?: prev.travelMode,
                        tripWaypoints = tripWaypoints ?: prev.tripWaypoints,
                    ),
                )
            } else {
                layer
            }
        }
    }
}
