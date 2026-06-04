package com.example.roadguideapp.map

import androidx.compose.ui.graphics.Color

/**
 * Shared chrome for map sheets, directions, and floating map controls.
 * Follows [MapTimeOfDay] so UI stays in sync with map day / night styling.
 */
internal data class AppleMapsSheetTheme(
    val isLight: Boolean,
    val sheetSurface: Color,
    val stickyHeaderSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val accent: Color,
    val onAccent: Color,
    val searchFieldFill: Color,
    val searchFieldHint: Color,
    val searchFieldText: Color,
    val profileAvatarBg: Color,
    val profileAvatarText: Color,
    val nearbyCellBackground: Color,
    val nearbyCellText: Color,
    val bannerBackground: Color,
    val bannerSubtitle: Color,
    val bannerIconTint: Color,
    val grabber: Color,
    val divider: Color,
    val cardElevated: Color,
    val cardElevatedSecondary: Color,
    val controlSegmentUnselected: Color,
    val directionsInnerCard: Color,
    val directionsConnectorLine: Color,
    val pillChipBackground: Color,
    val summaryCardBackground: Color,
    val headerIconSecondaryBg: Color,
    val dragHandleTint: Color,
    val stopGlyphSecondary: Color,
    /** Floating map chrome (Choose map, My location, 3D, zoom pill, Look Around). */
    val mapControlGlass: Color,
    val mapControlIcon: Color,
    val mapControlDivider: Color,
    /** Compass dial (matches other floating controls in light / dark). */
    val compassDialSurface: Color,
    val compassDialRing: Color,
    val compassTick: Color,
    val compassCardinalText: Color,
)

internal fun appleMapsSheetTheme(timeOfDay: MapTimeOfDay): AppleMapsSheetTheme {
    return when (timeOfDay) {
        MapTimeOfDay.Dawn,
        MapTimeOfDay.Day,
        -> lightSheetTheme()
        MapTimeOfDay.Dusk,
        MapTimeOfDay.Night,
        -> darkSheetTheme()
    }
}

private fun lightSheetTheme(): AppleMapsSheetTheme {
    val surface = Color(0xFFF2F2F7)
    val text = Color(0xFF000000)
    val secondary = Color(0xFF8E8E93)
    return AppleMapsSheetTheme(
        isLight = true,
        sheetSurface = surface,
        stickyHeaderSurface = surface,
        primaryText = text,
        secondaryText = secondary,
        tertiaryText = secondary,
        accent = Color(0xFF0A84FF),
        onAccent = Color.White,
        searchFieldFill = Color(0xFFE5E5EA),
        searchFieldHint = Color(0xFF8E8E93),
        searchFieldText = text,
        profileAvatarBg = Color(0xFF48484A),
        profileAvatarText = Color.White,
        nearbyCellBackground = Color(0xFFE5E5EA),
        nearbyCellText = text,
        bannerBackground = Color(0xFFE5E5EA),
        bannerSubtitle = secondary,
        bannerIconTint = Color(0xFF0A84FF),
        grabber = Color(0xFFC7C7CC),
        divider = Color(0xFFE5E5EA),
        cardElevated = Color.White,
        cardElevatedSecondary = Color(0xFFE5E5EA),
        controlSegmentUnselected = Color(0xFFE5E5EA),
        directionsInnerCard = Color.White,
        directionsConnectorLine = Color(0xFFD1D1D6),
        pillChipBackground = Color(0xFFE5E5EA),
        summaryCardBackground = Color(0xFF0A84FF),
        headerIconSecondaryBg = Color(0xFFE5E5EA),
        dragHandleTint = secondary,
        stopGlyphSecondary = Color(0xFFFF3B30),
        mapControlGlass = Color(0xFFFFFFFF),
        mapControlIcon = Color(0xFF1C1C1E),
        mapControlDivider = Color(0x22000000),
        compassDialSurface = Color(0xFFFFFFFF),
        compassDialRing = Color(0xFFC7C7CC),
        compassTick = Color(0xFFAEAEB2),
        compassCardinalText = Color(0xFF1C1C1E),
    )
}

private fun darkSheetTheme(): AppleMapsSheetTheme {
    val surface = Color(0xFF242426)
    val text = Color.White
    val secondary = Color(0xFF8E8E93)
    return AppleMapsSheetTheme(
        isLight = false,
        sheetSurface = surface,
        stickyHeaderSurface = surface,
        primaryText = text,
        secondaryText = secondary,
        tertiaryText = Color(0xFF8E8E93),
        accent = Color(0xFF0A84FF),
        onAccent = Color.White,
        searchFieldFill = Color(0xFF3A3A3C),
        searchFieldHint = Color(0xFFB0B0B5),
        searchFieldText = text,
        profileAvatarBg = Color(0xFF48484A),
        profileAvatarText = text,
        nearbyCellBackground = Color(0xFF2C2C2E),
        nearbyCellText = text,
        bannerBackground = Color(0xFF2C2C2E),
        bannerSubtitle = Color(0xFFAEAEB2),
        bannerIconTint = Color(0xFF5AC8FA),
        grabber = AppleMapsUiTokens.SheetGrabber,
        divider = Color(0xFF48484A),
        cardElevated = Color(0xFF2C2C2E),
        cardElevatedSecondary = Color(0xFF3A3A3C),
        controlSegmentUnselected = Color(0xFF2C2C2E),
        directionsInnerCard = Color(0xFF000000),
        directionsConnectorLine = Color(0xFF48484A),
        pillChipBackground = Color(0xFF2C2C2E),
        summaryCardBackground = Color(0xFF0A84FF),
        headerIconSecondaryBg = Color(0xFF3A3A3C),
        dragHandleTint = Color(0xFF636366),
        stopGlyphSecondary = Color(0xFFFF3B30),
        mapControlGlass = Color(0xFF1C1C1E),
        mapControlIcon = Color.White,
        mapControlDivider = Color(0x33FFFFFF),
        compassDialSurface = Color(0xFF1C1C1E),
        compassDialRing = Color(0xFF48484A),
        compassTick = Color(0xFF8E8E93),
        compassCardinalText = Color.White,
    )
}

internal fun AppleMapsSheetTheme.toPlaceDetailPalette(): PlaceDetailPalette {
    return PlaceDetailPalette(
        sheetSurface = sheetSurface,
        primaryText = primaryText,
        secondaryText = secondaryText,
        accent = accent,
        cardSurface = cardElevated,
        divider = divider,
        grabber = grabber,
        headerSecondaryFill = headerIconSecondaryBg,
        headerIconTint = accent,
        primaryActionBg = accent,
        secondaryActionBg = cardElevatedSecondary,
        secondaryActionIconTint = accent,
        secondaryActionLabel = if (isLight) accent else primaryText,
        closedStatus = AppleMapsUiTokens.PlaceClosedStatus,
    )
}

/** Place detail palette — kept in this file for use by [toPlaceDetailPalette]. */
internal data class PlaceDetailPalette(
    val sheetSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val cardSurface: Color,
    val divider: Color,
    val grabber: Color,
    val headerSecondaryFill: Color,
    val headerIconTint: Color,
    val primaryActionBg: Color,
    val secondaryActionBg: Color,
    val secondaryActionIconTint: Color,
    val secondaryActionLabel: Color,
    val closedStatus: Color,
)
