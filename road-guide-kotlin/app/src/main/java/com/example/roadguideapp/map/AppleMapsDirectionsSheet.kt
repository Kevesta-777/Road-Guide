package com.example.roadguideapp.map

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.roadguideapp.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * In-map directions panel (not a modal): map stays interactive outside this card.
 * Height is driven by [linkedSheetHeightDp], which must match the persistent bottom sheet’s
 * live height (including during drag) so both move together.
 */
@Composable
internal fun AppleMapsDirectionsPanel(
    origin: MapPlaceDetail,
    stops: List<MapPlaceDetail>,
    travelMode: DirectionsTravelMode,
    onTravelModeChange: (DirectionsTravelMode) -> Unit,
    linkedSheetHeightDp: Dp? = null,
    sheetGestures: AppleMapsSheetGestures,
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    onAddStopRowClick: () -> Unit,
    onDismiss: () -> Unit,
    onNearbyShortcutClick: (AppleNearbyShortcut) -> Unit = {},
    routeResult: DirectionsRouteResult? = null,
    routeSource: DirectionsRouteSource? = null,
    isRouteCalculating: Boolean = false,
    isRouteRefining: Boolean = false,
    offlineGraphLoaded: Boolean = false,
    /** Legs in the full trip (for cumulative ETA / distance on the map route). */
    tripLegCount: Int = 0,
    onImportGraphClick: (() -> Unit)? = null,
    isNavigationActive: Boolean = false,
    onStartNavigation: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val travelModeIndex = DirectionsTravelMode.chipIndex(travelMode)

    val accent = sheetTheme.accent
    val cardBg = sheetTheme.directionsInnerCard
    val muted = sheetTheme.secondaryText
    val lineColor = sheetTheme.directionsConnectorLine

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                linkedSheetHeightDp?.let { Modifier.height(it) } ?: Modifier.fillMaxHeight(),
            ),
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        color = sheetTheme.sheetSurface,
        shadowElevation = 16.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding()
                .then(sheetGestures.chromeDrag),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(sheetGestures.grabberDrag),
            ) {
                AppleMapsSheetGrabber(
                    grabberColor = sheetTheme.grabber,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .then(
                        sheetGestures.scrollContent(
                            scrollState = scrollState,
                            scrollEnabled = contentScrollEnabled,
                        ),
                    )
                    .padding(bottom = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.apple_directions_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = sheetTheme.primaryText,
                        fontSize = 28.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = sheetTheme.cardElevatedSecondary,
                        shadowElevation = 0.dp,
                    ) {
                        IconButton(
                            onClick = { /* share pipeline */ },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.apple_place_share),
                                tint = sheetTheme.primaryText,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = sheetTheme.cardElevatedSecondary,
                        shadowElevation = 0.dp,
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.apple_close),
                                tint = sheetTheme.primaryText,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TravelModeChip(
                        selected = travelModeIndex == 0,
                        icon = Icons.Outlined.DirectionsCar,
                        label = stringResource(R.string.apple_directions_mode_drive),
                        onClick = { onTravelModeChange(DirectionsTravelMode.Drive) },
                        sheetTheme = sheetTheme,
                        modifier = Modifier.weight(1f),
                    )
                    TravelModeChip(
                        selected = travelModeIndex == 1,
                        icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
                        label = stringResource(R.string.apple_directions_mode_walk),
                        onClick = { onTravelModeChange(DirectionsTravelMode.Walk) },
                        sheetTheme = sheetTheme,
                        modifier = Modifier.weight(1f),
                    )
                    TravelModeChip(
                        selected = travelModeIndex == 2,
                        icon = Icons.AutoMirrored.Outlined.DirectionsBike,
                        label = stringResource(R.string.apple_directions_mode_cycle),
                        onClick = { onTravelModeChange(DirectionsTravelMode.Bicycle) },
                        sheetTheme = sheetTheme,
                        modifier = Modifier.weight(1f),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    val allLegs = listOf(origin) + stops
                    allLegs.forEachIndexed { index, leg ->
                        DirectionsStopRow(
                            place = leg,
                            isDestination = index == allLegs.lastIndex && index > 0,
                            showLineAbove = index > 0,
                            showLineBelow = index < allLegs.lastIndex,
                            lineColor = lineColor,
                            accent = accent,
                            muted = muted,
                            primaryText = sheetTheme.primaryText,
                            dragHandleTint = sheetTheme.dragHandleTint,
                            stopGlyphSecondary = sheetTheme.stopGlyphSecondary,
                            onAccent = sheetTheme.onAccent,
                            onGlyphClick = onAddStopRowClick,
                        )
                    }
                    DirectionsAddStopRow(
                        accent = accent,
                        onClick = onAddStopRowClick,
                        sheetTheme = sheetTheme,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                NearbyShortcutsSection(
                    sheetTheme = sheetTheme,
                    onShortcutClick = onNearbyShortcutClick,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PillChip(
                        label = stringResource(R.string.apple_directions_now),
                        sheetTheme = sheetTheme,
                        modifier = Modifier.weight(1f),
                    )
                    PillChip(
                        label = stringResource(R.string.apple_directions_avoid),
                        sheetTheme = sheetTheme,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (tripLegCount > 0) {
                    DirectionsSummaryCard(
                        stopCount = tripLegCount,
                        routeResult = routeResult,
                        routeSource = routeSource,
                        isCalculating = isRouteCalculating,
                        isRefining = isRouteRefining,
                        summaryBackground = sheetTheme.summaryCardBackground,
                        onAccent = sheetTheme.onAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }

                if (!offlineGraphLoaded && onImportGraphClick != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onImportGraphClick),
                        color = sheetTheme.cardElevatedSecondary,
                    ) {
                        Text(
                            text = stringResource(R.string.directions_offline_import_title),
                            color = accent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }

                val showNavigate =
                    !isNavigationActive &&
                        !isRouteCalculating &&
                        !isRouteRefining &&
                        routeResult != null &&
                        routeResult.geometry.size >= 2 &&
                        onStartNavigation != null
                if (showNavigate) {
                    Button(
                        onClick = onStartNavigation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = sheetTheme.onAccent,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.apple_directions_navigate),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelModeChip(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) sheetTheme.accent else sheetTheme.controlSegmentUnselected
    val iconTint = if (selected) sheetTheme.onAccent else sheetTheme.primaryText
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun DirectionsStopRow(
    place: MapPlaceDetail,
    isDestination: Boolean,
    showLineAbove: Boolean,
    showLineBelow: Boolean,
    lineColor: Color,
    accent: Color,
    muted: Color,
    primaryText: Color,
    dragHandleTint: Color,
    stopGlyphSecondary: Color,
    onAccent: Color,
    onGlyphClick: () -> Unit,
) {
    val isFirst = !showLineAbove
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp),
        ) {
            if (showLineAbove) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(14.dp)
                        .background(lineColor),
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
            StopGlyph(
                isFirst = isFirst,
                accent = accent,
                stopGlyphSecondary = stopGlyphSecondary,
                onAccent = onAccent,
                onClick = onGlyphClick,
            )
            if (showLineBelow) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(14.dp)
                        .background(lineColor),
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (isFirst) {
                Text(
                    text = stringResource(R.string.apple_directions_from_label),
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(2.dp))
            } else if (isDestination) {
                Text(
                    text = stringResource(R.string.apple_directions_to_label),
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = place.name,
                color = primaryText,
                fontSize = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!isFirst && !isDestination) {
                Text(
                    text = place.category,
                    color = muted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.DragHandle,
            contentDescription = null,
            tint = dragHandleTint,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun StopGlyph(
    isFirst: Boolean,
    accent: Color,
    stopGlyphSecondary: Color,
    onAccent: Color,
    onClick: () -> Unit,
) {
    val ring = if (isFirst) accent else stopGlyphSecondary
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .background(ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = onAccent,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun DirectionsAddStopRow(
    accent: Color,
    onClick: () -> Unit,
    sheetTheme: AppleMapsSheetTheme,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = sheetTheme.onAccent,
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.apple_directions_add_stop),
            color = accent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PillChip(
    label: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(sheetTheme.pillChipBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = sheetTheme.primaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "  ∨",
            color = sheetTheme.secondaryText,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun DirectionsSummaryCard(
    stopCount: Int,
    routeResult: DirectionsRouteResult?,
    routeSource: DirectionsRouteSource?,
    isCalculating: Boolean,
    isRefining: Boolean,
    summaryBackground: Color,
    onAccent: Color,
    modifier: Modifier = Modifier,
) {
    val stopsLabel = if (stopCount == 1) {
        stringResource(R.string.apple_directions_stop_one)
    } else {
        stringResource(R.string.apple_directions_stop_many, stopCount)
    }
    val timeLabel = when {
        isCalculating -> stringResource(R.string.directions_route_calculating)
        routeResult != null -> {
            val minutes = (routeResult.totalDurationSeconds / 60.0).toInt().coerceAtLeast(1)
            stringResource(R.string.directions_route_minutes, minutes)
        }
        else -> stringResource(R.string.apple_directions_summary_time_placeholder)
    }
    val subtitle = when {
        isCalculating -> stringResource(R.string.directions_route_calculating_subtitle)
        isRefining -> stringResource(R.string.directions_route_refining_subtitle)
        routeResult != null -> {
            val km = routeResult.totalLengthKm
            val sourceLabel = when (routeSource) {
                DirectionsRouteSource.Valhalla -> stringResource(R.string.directions_route_source_valhalla)
                DirectionsRouteSource.OfflineGraph -> stringResource(R.string.directions_route_source_offline)
                DirectionsRouteSource.Preview -> stringResource(R.string.directions_route_source_preview)
                else -> ""
            }
            stringResource(R.string.directions_route_distance_km, km, sourceLabel)
        }
        else -> stringResource(R.string.apple_directions_summary_subtitle_placeholder)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = summaryBackground,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeLabel,
                    color = onAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = onAccent.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                )
                Text(
                    text = stopsLabel,
                    color = onAccent.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                )
            }
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = onAccent.copy(alpha = 0.2f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = onAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
