package com.example.roadguideapp.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Light neutral surface used on top of the map (typical map SDK floating controls). */
internal val MapControlSurfaceColor = Color(0xFFFAFAFA)
internal val MapControlIconTint = Color(0xFF2C2C2C)

/** Sygic-style semi-transparent dark pill used during navigation. */
internal val NavigationMapControlSurfaceColor = Color(0xB3373737)
internal val NavigationMapControlIconTint = Color(0xFFFFFFFF)
internal val NavigationMapControlDividerColor = Color(0x33FFFFFF)

private val MapControlDiameter = 48.dp
private val MapControlShadowElevation = 6.dp

private val ZoomPillWidth = 48.dp
private val ZoomPillButtonHeight = 48.dp
private val ZoomPillCorner = 24.dp

@Composable
internal fun MapCircleControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.size(MapControlDiameter),
        shape = CircleShape,
        color = MapControlSurfaceColor,
        shadowElevation = MapControlShadowElevation,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
        ) {
            content()
        }
    }
}

/**
 * Vertical pill with + / − and a faint divider, matching common map app zoom controls.
 */
@Composable
internal fun MapZoomPillControl(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    zoomInContentDescription: String,
    zoomOutContentDescription: String,
    modifier: Modifier = Modifier,
    surfaceColor: Color = MapControlSurfaceColor,
    iconTint: Color = MapControlIconTint,
    dividerColor: Color = Color(0x14000000),
) {
    MapVerticalPillControl(
        onTopClick = onZoomIn,
        onBottomClick = onZoomOut,
        topContentDescription = zoomInContentDescription,
        bottomContentDescription = zoomOutContentDescription,
        topIcon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = zoomInContentDescription,
                tint = iconTint,
            )
        },
        bottomIcon = {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = zoomOutContentDescription,
                tint = iconTint,
            )
        },
        modifier = modifier,
        surfaceColor = surfaceColor,
        dividerColor = dividerColor,
    )
}

/** Vertical pill control (zoom, tilt, etc.) matching map SDK floating controls. */
@Composable
internal fun MapVerticalPillControl(
    onTopClick: () -> Unit,
    onBottomClick: () -> Unit,
    topContentDescription: String,
    bottomContentDescription: String,
    topIcon: @Composable () -> Unit,
    bottomIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    surfaceColor: Color = MapControlSurfaceColor,
    dividerColor: Color = Color(0x14000000),
) {
    Surface(
        modifier = modifier.width(ZoomPillWidth),
        shape = RoundedCornerShape(ZoomPillCorner),
        color = surfaceColor,
        shadowElevation = MapControlShadowElevation,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onTopClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ZoomPillButtonHeight),
            ) {
                topIcon()
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = dividerColor,
            )
            IconButton(
                onClick = onBottomClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ZoomPillButtonHeight),
            ) {
                bottomIcon()
            }
        }
    }
}

@Composable
internal fun MapTiltPillControl(
    onTiltUp: () -> Unit,
    onTiltDown: () -> Unit,
    tiltUpContentDescription: String,
    tiltDownContentDescription: String,
    modifier: Modifier = Modifier,
    surfaceColor: Color = NavigationMapControlSurfaceColor,
    iconTint: Color = NavigationMapControlIconTint,
    dividerColor: Color = NavigationMapControlDividerColor,
) {
    MapVerticalPillControl(
        onTopClick = onTiltUp,
        onBottomClick = onTiltDown,
        topContentDescription = tiltUpContentDescription,
        bottomContentDescription = tiltDownContentDescription,
        topIcon = {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = tiltUpContentDescription,
                tint = iconTint,
            )
        },
        bottomIcon = {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = tiltDownContentDescription,
                tint = iconTint,
            )
        },
        modifier = modifier,
        surfaceColor = surfaceColor,
        dividerColor = dividerColor,
    )
}
