package com.example.roadguideapp.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal object AppleMapsUiTokens {

    /** Dark translucent floating controls (similar to Apple Maps dark mode chrome). */
    val ControlGlass = Color(0xCC1C1C1E)
    val ControlIcon = Color.White
    val ControlDivider = Color(0x33FFFFFF)

    /** Bottom sheet surface dark (slightly translucent). */
    val SheetSurface = Color(0xF0242426)

    /** Mid and large sheet heights as fractions of the full screen. */
    const val SheetMidHeightFraction = 0.4f
    const val SheetProfileEditHeightFraction = 0.45f
    const val SheetLargeHeightFraction = 0.93f
    /** Top-right map chrome stays visible while the sheet is at or below this height. */
    const val SheetTopChromeMaxHeightFraction = 0.7f
    val SheetGrabber = Color(0xFF636366)

    /** Search field inside sheet. */
    val SearchFieldFill = Color(0xFF3A3A3C)
    val SearchFieldHint = Color(0xFFB0B0B5)

    val ControlCornerDp = 12.dp
    val ControlSeparationDp = 8.dp
    val CompassSizeDp = 44.dp

    /** Apple-like short peek (grabber + search row). */
    val SheetPeekMinDp = 118.dp

    /** Light place-detail sheet (business / POI). */
    val PlaceSheetSurface = Color(0xFFF2F2F7)
    val PlaceSheetCard = Color.White
    val PlaceSheetGrabber = Color(0xFFC7C7CC)
    val PlacePrimaryText = Color(0xFF000000)
    val PlaceSecondaryText = Color(0xFF8E8E93)
    val PlaceAccentBlue = Color(0xFF0A84FF)
    val PlaceActionSecondaryFill = Color(0xFFE5E5EA)
    val PlaceClosedStatus = Color(0xFFFF3B30)
    val PlaceDivider = Color(0xFFE5E5EA)

    val ProfileAvatarBg = Color(0xFF48484A)
}
