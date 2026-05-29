package com.example.roadguideapp.map

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity

@Composable
internal fun rememberAppleMapsSheetCollapseNestedScrollConnection(
    scrollState: ScrollState,
    heightState: AppleMapsSheetHeightState,
    settledHeightPx: () -> Float,
    onDragRelease: (draggingUp: Boolean) -> Unit,
): NestedScrollConnection {
    return remember(scrollState, heightState, settledHeightPx, onDragRelease) {
        object : NestedScrollConnection {
            private var collapseActive = false

            private fun canCollapseFromScroll(): Boolean =
                !scrollState.canScrollBackward

            private fun consumeCollapseDrag(availableY: Float): Offset {
                if (availableY <= 0f || !canCollapseFromScroll()) {
                    collapseActive = false
                    return Offset.Zero
                }
                if (!heightState.isDragging) {
                    heightState.beginDrag(settledHeightPx())
                }
                collapseActive = true
                heightState.dragBy(availableY)
                return Offset(0f, availableY)
            }

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                return consumeCollapseDrag(available.y)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                return consumeCollapseDrag(available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (collapseActive) {
                    onDragRelease(false)
                    collapseActive = false
                }
                return Velocity.Zero
            }
        }
    }
}

@Composable
internal fun Modifier.appleMapsSheetHeightDrag(
    heightState: AppleMapsSheetHeightState,
    settledHeightPx: () -> Float,
    onDragRelease: (draggingUp: Boolean) -> Unit,
    onTapAdvance: (() -> Unit)? = null,
): Modifier {
    return this
        .then(
            if (onTapAdvance != null) {
                Modifier.pointerInput(heightState, onTapAdvance) {
                    detectTapGestures(onTap = { onTapAdvance() })
                }
            } else {
                Modifier
            },
        )
        .pointerInput(heightState, settledHeightPx, onDragRelease) {
            var totalDragY = 0f
            detectVerticalDragGestures(
                onDragStart = {
                    totalDragY = 0f
                    heightState.beginDrag(settledHeightPx())
                },
                onVerticalDrag = { _, dragAmount ->
                    totalDragY += dragAmount
                    heightState.dragBy(dragAmount)
                },
                onDragEnd = {
                    onDragRelease(totalDragY < 0f)
                },
                onDragCancel = {
                    onDragRelease(totalDragY < 0f)
                },
            )
        }
}

@Composable
internal fun Modifier.appleMapsSheetInteractiveBlock(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = {},
    )
}
