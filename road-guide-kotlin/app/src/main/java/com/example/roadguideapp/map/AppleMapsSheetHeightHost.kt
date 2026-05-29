package com.example.roadguideapp.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Stable
internal class AppleMapsSheetHeightHandle(
    val displayHeightPx: Float,
    val displayHeight: Dp,
    val contentScrollEnabled: Boolean,
    val sheetGrabberDragModifier: Modifier,
    val sheetBodyDragModifier: Modifier,
    val collapseNestedScrollConnection: NestedScrollConnection,
    private val animateToSnapInternal: (AppleSheetSnap, Float?) -> Unit,
    val handleDragRelease: (draggingUp: Boolean) -> Unit,
) {
    fun animateToSnap(targetSnap: AppleSheetSnap, fromHeightPx: Float? = null) {
        animateToSnapInternal(targetSnap, fromHeightPx)
    }
}

@Stable
private class SheetHeightDragController(
    val sheetBodyDragModifier: Modifier,
    val sheetGrabberDragModifier: Modifier,
    val collapseNestedScrollConnection: NestedScrollConnection,
    val animateToSnap: (AppleSheetSnap, Float?) -> Unit,
    val handleDragRelease: (Boolean) -> Unit,
)

@Composable
internal fun rememberAppleMapsSheetHeightHandle(
    screenHeight: Dp,
    snap: AppleSheetSnap,
    presentationKey: Int,
    onSnapChange: (AppleSheetSnap) -> Unit,
    scrollState: ScrollState? = null,
    /** When false, [presentationKey] only drives snap tweens, not peek→target enter. */
    animatePresentationFromPeek: Boolean = true,
    /**
     * False for frozen layers that mirror another sheet's height — avoids competing tweens.
     */
    observeExternalSnap: Boolean = true,
): AppleMapsSheetHeightHandle {
    val density = LocalDensity.current
    val heightState = rememberAppleMapsSheetHeightState(screenHeight)
    val scope = rememberCoroutineScope()
    val heightAnim = remember(heightState) {
        Animatable(heightState.heightPxFor(AppleSheetSnap.Peek))
    }
    var lastHandledPresentationKey by remember { mutableIntStateOf(0) }

    val settledHeightPx = remember(heightAnim) { { heightAnim.value } }

    val releaseSpring = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
    }

    val onDragRelease = remember(heightState, scope, onSnapChange, releaseSpring) {
        val handler: (Boolean) -> Unit = { draggingUp ->
            scope.launch {
                val releaseHeightPx = heightState.currentDragHeightPx()
                val targetSnap = heightState.resolveReleaseSnap(releaseHeightPx, draggingUp)
                heightState.endDrag()
                heightAnim.snapTo(releaseHeightPx)
                heightAnim.animateTo(heightState.heightPxFor(targetSnap), releaseSpring)
                onSnapChange(targetSnap)
            }
        }
        handler
    }

    val animateToSnap = remember(heightState, scope, onSnapChange, releaseSpring) {
        val handler: (AppleSheetSnap, Float?) -> Unit = { targetSnap, fromHeightPx ->
            scope.launch {
                heightState.endDrag()
                if (fromHeightPx != null) heightAnim.snapTo(fromHeightPx)
                heightAnim.animateTo(heightState.heightPxFor(targetSnap), releaseSpring)
                onSnapChange(targetSnap)
            }
        }
        handler
    }

    val collapseNestedScrollConnection =
        if (scrollState != null) {
            rememberAppleMapsSheetCollapseNestedScrollConnection(
                scrollState = scrollState,
                heightState = heightState,
                settledHeightPx = settledHeightPx,
                onDragRelease = onDragRelease,
            )
        } else {
            remember(heightState) {
                object : NestedScrollConnection {}
            }
        }

    val sheetBodyDragModifier = Modifier.appleMapsSheetHeightDrag(
        heightState = heightState,
        settledHeightPx = settledHeightPx,
        onDragRelease = onDragRelease,
    )
    val sheetGrabberDragModifier = Modifier.appleMapsSheetHeightDrag(
        heightState = heightState,
        settledHeightPx = settledHeightPx,
        onDragRelease = onDragRelease,
        onTapAdvance = { animateToSnap(snap.next(), null) },
    )

    val controller = remember(collapseNestedScrollConnection, animateToSnap, onDragRelease) {
        SheetHeightDragController(
            sheetBodyDragModifier = sheetBodyDragModifier,
            sheetGrabberDragModifier = sheetGrabberDragModifier,
            collapseNestedScrollConnection = collapseNestedScrollConnection,
            animateToSnap = animateToSnap,
            handleDragRelease = onDragRelease,
        )
    }

    LaunchedEffect(snap, presentationKey, animatePresentationFromPeek, observeExternalSnap) {
        if (!observeExternalSnap || heightState.isDragging) return@LaunchedEffect
        val targetPx = heightState.heightPxFor(snap)
        if (
            animatePresentationFromPeek &&
            presentationKey > 0 &&
            presentationKey != lastHandledPresentationKey
        ) {
            lastHandledPresentationKey = presentationKey
            heightAnim.snapTo(heightState.heightPxFor(AppleSheetSnap.Peek))
            heightAnim.animateTo(targetPx, releaseSpring)
        } else if (abs(heightAnim.value - targetPx) > 1f) {
            if (observeExternalSnap) {
                heightAnim.animateTo(targetPx, releaseSpring)
            } else {
                heightAnim.snapTo(targetPx)
            }
        }
    }

    val displayHeightPx = if (heightState.isDragging) {
        heightState.currentDragHeightPx()
    } else {
        heightAnim.value
    }
    val displayHeight = with(density) { displayHeightPx.toDp() }

    return AppleMapsSheetHeightHandle(
        displayHeightPx = displayHeightPx,
        displayHeight = displayHeight,
        contentScrollEnabled = heightState.contentScrollEnabled(displayHeightPx),
        sheetGrabberDragModifier = controller.sheetGrabberDragModifier,
        sheetBodyDragModifier = controller.sheetBodyDragModifier,
        collapseNestedScrollConnection = controller.collapseNestedScrollConnection,
        animateToSnapInternal = controller.animateToSnap,
        handleDragRelease = controller.handleDragRelease,
    )
}
