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

internal enum class AppleSheetSnap {
    Peek,
    Mid,
    ProfileEdit,
    Large,
    ;

    fun next(): AppleSheetSnap = when (this) {
        Peek -> Mid
        Mid -> Large
        ProfileEdit -> Large
        Large -> Peek
    }
}

@OptIn(ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
internal fun AppleMapsPersistentBottomSheet(
    snap: AppleSheetSnap,
    onSnapChange: (AppleSheetSnap) -> Unit,
    hazeState: HazeState,
    scrollState: ScrollState,
    sheetPresentationKey: Int = 0,
    /** When an overlay sheet is visible, the base sheet keeps its height (no peek enter). */
    freezeHeightForOverlay: Boolean = false,
    modifier: Modifier = Modifier,
    sheetSurfaceColor: Color = AppleMapsUiTokens.SheetSurface,
    onSheetHeightChange: (Dp) -> Unit = {},
    onSheetBlurRadiusPxChange: (Float) -> Unit = {},
    sheetContent: @Composable (
        scrollState: ScrollState,
        contentScrollEnabled: Boolean,
        snap: AppleSheetSnap,
        onSnapChange: (AppleSheetSnap) -> Unit,
        sheetGestures: AppleMapsSheetGestures,
        modifier: Modifier,
    ) -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val heightState = rememberAppleMapsSheetHeightState(screenHeight)
    val sheetBlurStyle = HazeMaterials.regular(sheetSurfaceColor)
    val heightHandle = rememberAppleMapsSheetHeightHandle(
        screenHeight = screenHeight,
        snap = snap,
        presentationKey = if (freezeHeightForOverlay) 0 else sheetPresentationKey,
        onSnapChange = onSnapChange,
        scrollState = scrollState,
        animatePresentationFromPeek = !freezeHeightForOverlay,
    )

    SideEffect {
        onSheetHeightChange(heightHandle.displayHeight)
        onSheetBlurRadiusPxChange(heightState.mapBlurRadiusPx(heightHandle.displayHeightPx))
    }

    LaunchedEffect(heightHandle.contentScrollEnabled) {
        if (!heightHandle.contentScrollEnabled) {
            scrollState.scrollTo(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(heightHandle.displayHeight)
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .hazeEffect(state = hazeState, style = sheetBlurStyle),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            sheetContent(
                scrollState,
                heightHandle.contentScrollEnabled,
                snap,
                onSnapChange,
                heightHandle.toGestures(),
                Modifier.fillMaxSize(),
            )
        }
    }
}
