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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stacked sheet layer (Directions, Add Stop) with its own height animation so the
 * persistent base sheet underneath can stay at its current detent.
 */
@Composable
internal fun AppleMapsOverlayBottomSheet(
    snap: AppleSheetSnap,
    onSnapChange: (AppleSheetSnap) -> Unit,
    sheetPresentationKey: Int,
    scrollState: ScrollState,
    onSheetHeightChange: (Dp) -> Unit,
    onContentScrollEnabledChange: (Boolean) -> Unit,
    onGrabberDragModifierChange: (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (
        contentScrollEnabled: Boolean,
        sheetGestures: AppleMapsSheetGestures,
        modifier: Modifier,
    ) -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val heightHandle = rememberAppleMapsSheetHeightHandle(
        screenHeight = screenHeight,
        snap = snap,
        presentationKey = sheetPresentationKey,
        onSnapChange = onSnapChange,
        scrollState = scrollState,
        animatePresentationFromPeek = true,
    )

    SideEffect {
        onSheetHeightChange(heightHandle.displayHeight)
        onContentScrollEnabledChange(heightHandle.contentScrollEnabled)
        onGrabberDragModifierChange(heightHandle.sheetGrabberDragModifier)
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
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            content(
                heightHandle.contentScrollEnabled,
                heightHandle.toGestures(),
                Modifier.fillMaxSize(),
            )
        }
    }
}
