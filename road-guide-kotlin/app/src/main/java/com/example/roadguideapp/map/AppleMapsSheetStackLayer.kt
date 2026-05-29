package com.example.roadguideapp.map

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 * One layer in the stacked sheet system. Lower layers can be [isFrozen] so they keep
 * their detent when a new sheet is pushed on top.
 */
@OptIn(ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
internal fun AppleMapsSheetStackLayer(
    layer: AppleMapSheetLayer,
    isFrozen: Boolean,
    /** When set (frozen synced layer), matches the active sheet height while dragging. */
    syncedHeightOverride: Dp? = null,
    scrollState: ScrollState,
    onSnapChange: (AppleSheetSnap) -> Unit,
    onHeightChange: (Dp) -> Unit,
    onBlurRadiusPxChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sheetSurfaceColor: Color = AppleMapsUiTokens.SheetSurface,
    hazeState: HazeState? = null,
    content: @Composable (
        contentScrollEnabled: Boolean,
        sheetGestures: AppleMapsSheetGestures,
        modifier: Modifier,
    ) -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val heightState = rememberAppleMapsSheetHeightState(screenHeight)
    val mirrorsSyncedHeight = syncedHeightOverride != null
    val heightHandle = rememberAppleMapsSheetHeightHandle(
        screenHeight = screenHeight,
        snap = layer.snap,
        presentationKey = if (isFrozen) 0 else layer.presentationKey,
        onSnapChange = onSnapChange,
        scrollState = scrollState,
        animatePresentationFromPeek = !isFrozen,
        observeExternalSnap = !mirrorsSyncedHeight,
    )

    val density = LocalDensity.current
    val displayHeight = syncedHeightOverride ?: heightHandle.displayHeight
    val displayHeightPx = with(density) { displayHeight.toPx() }

    SideEffect {
        onBlurRadiusPxChange(heightState.mapBlurRadiusPx(displayHeightPx))
    }

    LaunchedEffect(heightHandle.contentScrollEnabled) {
        if (!heightHandle.contentScrollEnabled) {
            scrollState.scrollTo(0)
        }
    }

    val shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
    val columnModifier = modifier
        .fillMaxWidth()
        .height(displayHeight)
        .onSizeChanged { size ->
            if (mirrorsSyncedHeight) return@onSizeChanged
            val measured = with(density) { size.height.toDp() }
            onHeightChange(measured)
        }
        .clip(shape)
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.regular(sheetSurfaceColor),
                )
            } else {
                Modifier
            },
        )

    val sheetGestures = heightHandle.toGestures()

    Column(modifier = columnModifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            content(
                heightHandle.contentScrollEnabled,
                sheetGestures,
                Modifier.fillMaxSize(),
            )
        }
    }
}
