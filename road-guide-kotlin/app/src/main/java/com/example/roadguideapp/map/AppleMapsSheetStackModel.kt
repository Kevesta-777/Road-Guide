package com.example.roadguideapp.map

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Home, place detail, and directions share one detent (peek / mid / large). */
internal fun AppleMapSheet.isSyncedStackSheet(): Boolean = when (this) {
    is AppleMapSheet.Home,
    is AppleMapSheet.PlaceDetail,
    is AppleMapSheet.Directions,
    -> true
    is AppleMapSheet.AddStop -> false
    is AppleMapSheet.UserProfile -> false
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
    ) : AppleMapSheet() {
        override val stackId: String = "directions"
    }

    data object AddStop : AppleMapSheet() {
        override val stackId: String = "add_stop"
    }

    data class UserProfile(
        val selectedBusinessPoiId: String? = null,
    ) : AppleMapSheet() {
        override val stackId: String = "user_profile"
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
        layers = when {
            withoutDuplicate.size == 1 && withoutDuplicate[0].sheet is AppleMapSheet.Home -> {
                listOf(AppleMapSheetLayer(sheet, snapForNewLayer, key))
            }
            else -> withoutDuplicate + AppleMapSheetLayer(sheet, snapForNewLayer, key)
        }
    }

    fun pop() {
        layers = when {
            layers.size <= 1 -> listOf(AppleMapSheetLayer(AppleMapSheet.Home, AppleSheetSnap.Peek))
            else -> layers.dropLast(1)
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
        layers = remaining.ifEmpty { listOf(AppleMapSheetLayer(AppleMapSheet.Home, AppleSheetSnap.Peek)) }
    }

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

    fun updateDirections(
        origin: MapPlaceDetail,
        stops: List<MapPlaceDetail>,
        travelMode: DirectionsTravelMode? = null,
    ) {
        layers = layers.map { layer ->
            if (layer.sheet is AppleMapSheet.Directions) {
                val prev = layer.sheet
                layer.copy(
                    sheet = AppleMapSheet.Directions(
                        origin = origin,
                        stops = stops,
                        travelMode = travelMode ?: prev.travelMode,
                    ),
                )
            } else {
                layer
            }
        }
    }
}
