package com.example.roadguideapp.map

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Drag + nested-scroll modifiers for a stacked sheet layer.
 * Apply [chromeDrag] to sticky headers; apply [scrollContent] to [verticalScroll] columns.
 */
@Stable
internal data class AppleMapsSheetGestures(
    val grabberDrag: Modifier,
    val chromeDrag: Modifier,
    private val collapseNestedScroll: Modifier,
) {
    fun scrollContent(scrollState: ScrollState, scrollEnabled: Boolean): Modifier =
        if (scrollEnabled) {
            collapseNestedScroll.verticalScroll(scrollState, enabled = true)
        } else {
            chromeDrag.verticalScroll(scrollState, enabled = false)
        }
}

internal fun AppleMapsSheetHeightHandle.toGestures(): AppleMapsSheetGestures =
    AppleMapsSheetGestures(
        grabberDrag = sheetGrabberDragModifier,
        chromeDrag = sheetBodyDragModifier,
        collapseNestedScroll = Modifier.nestedScroll(collapseNestedScrollConnection),
    )
