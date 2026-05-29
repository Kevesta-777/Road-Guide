package com.example.roadguideapp.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Stable
internal class AppleMapsSheetHeightState(
    private val peekPx: Float,
    private val midPx: Float,
    private val profileEditPx: Float,
    private val largePx: Float,
) {
    var isDragging by mutableStateOf(false)
        private set

    var dragAnchorPx by mutableFloatStateOf(peekPx)
        private set
    var dragOffsetPx by mutableFloatStateOf(0f)
        private set

    fun heightPxFor(snap: AppleSheetSnap): Float = when (snap) {
        AppleSheetSnap.Peek -> peekPx
        AppleSheetSnap.Mid -> midPx
        AppleSheetSnap.ProfileEdit -> profileEditPx
        AppleSheetSnap.Large -> largePx
    }

    fun contentScrollEnabled(displayPx: Float): Boolean = displayPx >= largePx - 0.5f

    fun mapBlurRadiusPx(displayPx: Float): Float {
        if (displayPx <= peekPx + 0.5f) return 0f
        val progress = ((displayPx - peekPx) / (largePx - peekPx)).coerceIn(0f, 1f)
        return 18f + (28f - 18f) * progress
    }

    fun currentDragHeightPx(): Float = (dragAnchorPx + dragOffsetPx).coerceIn(peekPx, largePx)

    fun beginDrag(currentSettledPx: Float) {
        isDragging = true
        dragAnchorPx = currentSettledPx
        dragOffsetPx = 0f
    }

    fun dragBy(deltaY: Float) {
        if (!isDragging) return
        dragOffsetPx -= deltaY
    }

    fun endDrag() {
        isDragging = false
        dragOffsetPx = 0f
    }

    fun resolveReleaseSnap(currentHeightPx: Float, draggingUp: Boolean): AppleSheetSnap {
        return when {
            currentHeightPx > largePx - (largePx - profileEditPx) * 0.5f ->
                if (draggingUp) AppleSheetSnap.Large else AppleSheetSnap.ProfileEdit
            currentHeightPx > profileEditPx - (profileEditPx - midPx) * 0.5f ->
                if (draggingUp) AppleSheetSnap.Large else AppleSheetSnap.ProfileEdit
            currentHeightPx > midPx - (midPx - peekPx) * 0.5f ->
                if (draggingUp) AppleSheetSnap.Mid else AppleSheetSnap.Peek
            currentHeightPx > peekPx -> if (draggingUp) AppleSheetSnap.Mid else AppleSheetSnap.Peek
            else -> AppleSheetSnap.Peek
        }
    }
}

@Composable
internal fun rememberAppleMapsSheetHeightState(screenHeight: Dp): AppleMapsSheetHeightState {
    val density = LocalDensity.current
    return remember(screenHeight, density) {
        val screenHeightPx = with(density) { screenHeight.toPx() }
        AppleMapsSheetHeightState(
            peekPx = with(density) { AppleMapsUiTokens.SheetPeekMinDp.toPx() },
            midPx = screenHeightPx * AppleMapsUiTokens.SheetMidHeightFraction,
            profileEditPx = screenHeightPx * AppleMapsUiTokens.SheetProfileEditHeightFraction,
            largePx = screenHeightPx * AppleMapsUiTokens.SheetLargeHeightFraction,
        )
    }
}

internal fun AppleSheetSnap.mapBlurRadiusPx(): Float = when (this) {
    AppleSheetSnap.Peek -> 0f
    AppleSheetSnap.Mid -> 18f
    AppleSheetSnap.ProfileEdit -> 20f
    AppleSheetSnap.Large -> 28f
}

internal fun AppleSheetSnap.sheetHeight(configurationScreenHeightDp: Dp): Dp = when (this) {
    AppleSheetSnap.Peek -> AppleMapsUiTokens.SheetPeekMinDp
    AppleSheetSnap.Mid -> configurationScreenHeightDp * AppleMapsUiTokens.SheetMidHeightFraction
    AppleSheetSnap.ProfileEdit -> configurationScreenHeightDp * AppleMapsUiTokens.SheetProfileEditHeightFraction
    AppleSheetSnap.Large -> configurationScreenHeightDp * AppleMapsUiTokens.SheetLargeHeightFraction
}
