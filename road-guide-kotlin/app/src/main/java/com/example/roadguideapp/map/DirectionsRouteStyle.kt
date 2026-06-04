package com.example.roadguideapp.map

import org.maplibre.android.style.expressions.Expression

/**
 * Route line colors and widths tuned for light vs dark map appearance.
 */
internal data class DirectionsRoutePalette(
    val driveFill: String,
    val walkFill: String,
    val bikeFill: String,
    val previewCasing: String,
    val legLabelText: String,
    val walkDotCasing: String,
    val navDriveFill: String,
    val navDriveBorder: String,
    val navWalkFill: String,
    val navWalkBorder: String,
    val navBikeFill: String,
    val navBikeBorder: String,
)

internal object DirectionsRouteStyle {

    private const val ROUTE_CASING_GAP_PREVIEW = 2.5f
    private const val ROUTE_CASING_GAP_NAV = 3.0f

    fun palette(isDarkAppearance: Boolean): DirectionsRoutePalette =
        if (isDarkAppearance) darkPalette() else lightPalette()

    fun previewFillColor(
        palette: DirectionsRoutePalette,
        travelMode: DirectionsTravelMode,
    ): String = when (travelMode) {
        DirectionsTravelMode.Drive -> palette.driveFill
        DirectionsTravelMode.Walk -> palette.walkFill
        DirectionsTravelMode.Bicycle -> palette.bikeFill
    }

    fun navFillColor(
        palette: DirectionsRoutePalette,
        travelMode: DirectionsTravelMode,
    ): String = when (travelMode) {
        DirectionsTravelMode.Drive -> palette.navDriveFill
        DirectionsTravelMode.Walk -> palette.navWalkFill
        DirectionsTravelMode.Bicycle -> palette.navBikeFill
    }

    fun navBorderColor(
        palette: DirectionsRoutePalette,
        travelMode: DirectionsTravelMode,
    ): String = when (travelMode) {
        DirectionsTravelMode.Drive -> palette.navDriveBorder
        DirectionsTravelMode.Walk -> palette.navWalkBorder
        DirectionsTravelMode.Bicycle -> palette.navBikeBorder
    }

    fun routeFillColor(
        palette: DirectionsRoutePalette,
        navigationMode: Boolean,
        travelMode: DirectionsTravelMode,
    ): String = if (navigationMode) {
        navFillColor(palette, travelMode)
    } else {
        previewFillColor(palette, travelMode)
    }

    fun routeLineWidthExpression(navigationMode: Boolean): Expression =
        if (navigationMode) navigationLineWidthExpression() else previewLineWidthExpression()

    fun routeCasingWidthExpression(navigationMode: Boolean): Expression = Expression.sum(
        routeLineWidthExpression(navigationMode),
        Expression.literal(if (navigationMode) ROUTE_CASING_GAP_NAV else ROUTE_CASING_GAP_PREVIEW),
    )

    fun dotRadiusExpression(navigationMode: Boolean): Expression =
        if (navigationMode) navigationDotRadiusExpression() else previewDotRadiusExpression()

    fun dotCasingRadiusExpression(navigationMode: Boolean): Expression = Expression.sum(
        dotRadiusExpression(navigationMode),
        Expression.literal(ROUTE_CASING_GAP_PREVIEW + 0.3f),
    )

    private fun previewLineWidthExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(12, 2.5f),
        Expression.stop(14, 3.5f),
        Expression.stop(16, 5.0f),
        Expression.stop(18, 7.5f),
        Expression.stop(20, 9.0f),
        Expression.stop(22, 9.5f),
    )

    private fun navigationLineWidthExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(12, 3.5f),
        Expression.stop(14, 5.0f),
        Expression.stop(16, 7.0f),
        Expression.stop(18, 10.0f),
        Expression.stop(20, 12.0f),
        Expression.stop(22, 13.0f),
    )

    private fun previewDotRadiusExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(14, 4.0f),
        Expression.stop(16, 4.8f),
        Expression.stop(18, 5.6f),
        Expression.stop(20, 6.2f),
        Expression.stop(22, 6.5f),
    )

    private fun navigationDotRadiusExpression(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.zoom(),
        Expression.stop(14, 5.0f),
        Expression.stop(16, 6.0f),
        Expression.stop(18, 7.0f),
        Expression.stop(20, 7.8f),
        Expression.stop(22, 8.2f),
    )

    private fun lightPalette(): DirectionsRoutePalette = DirectionsRoutePalette(
        driveFill = "#0066EB",
        walkFill = "#9A5416",
        bikeFill = "#1B7A3A",
        previewCasing = "#FFFFFF",
        legLabelText = "#FFFFFF",
        walkDotCasing = "#FFFFFF",
        navDriveFill = "#1A7FFF",
        navDriveBorder = "#003D82",
        navWalkFill = "#B87333",
        navWalkBorder = "#5C3D1E",
        navBikeFill = "#2A9D5C",
        navBikeBorder = "#0F4D28",
    )

    private fun darkPalette(): DirectionsRoutePalette = DirectionsRoutePalette(
        driveFill = "#5EB4FF",
        walkFill = "#E8A864",
        bikeFill = "#5CD68A",
        previewCasing = "#1C1C1E",
        legLabelText = "#FFFFFF",
        walkDotCasing = "#1C1C1E",
        navDriveFill = "#7EC8FF",
        navDriveBorder = "#0A1E33",
        navWalkFill = "#F0BC72",
        navWalkBorder = "#3D2810",
        navBikeFill = "#6EE09A",
        navBikeBorder = "#0F3D22",
    )
}
