package com.example.roadguideapp.map

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TurnRight
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.roadguideapp.R
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.JsonObject
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Feature
import org.maplibre.geojson.MultiPoint
import org.maplibre.geojson.Point as GeoPoint

/**
 * Uses vector feature geometry when present so the selection halo lines up with the map icon,
 * not the arbitrary screen tap.
 */
internal fun Feature.anchorLatLng(fallback: LatLng): LatLng {
    return when (val geom = geometry()) {
        is GeoPoint -> LatLng(geom.latitude(), geom.longitude())
        is MultiPoint -> {
            val coords = geom.coordinates()
            val first = coords.firstOrNull() ?: return fallback
            LatLng(first.latitude(), first.longitude())
        }
        else -> fallback
    }
}

internal data class MapPlaceDetail(
    val id: String,
    val name: String,
    val category: String,
    val locality: String,
    val hoursSummary: String,
    val isOpenNow: Boolean,
    val website: String?,
    val phone: String?,
    val address: String,
    val latLng: LatLng,
    val backendPoiId: String? = null,
    val storedExternalRef: String? = null,
    val isClaimEligible: Boolean = false,
) {
    companion object {
        fun fromRenderedFeatures(
            context: Context,
            features: Collection<Feature>,
            latLng: LatLng,
        ): MapPlaceDetail? {
            return features.asSequence()
                .mapNotNull { feature -> fromMapFeature(context, feature, latLng) }
                .firstOrNull()
        }

        fun fromMapFeature(context: Context, feature: Feature, latLng: LatLng): MapPlaceDetail? {
            val properties = feature.properties() ?: return null
            val name = properties.readString("name")
                ?: properties.readString("name_en")
                ?: properties.readString("name:en")
                ?: return null
            val anchor = feature.anchorLatLng(latLng)
            val category = properties.readString("class")
                ?: properties.readString("subclass")
                ?: properties.readString("type")
                ?: properties.readString("amenity")
                ?: properties.readString("shop")
                ?: properties.readString("transport")
                ?: context.getString(R.string.apple_place_unknown_category)
            val meta = PlaceMetadataResolver.fromJsonObject(properties)
            val website = properties.readString("website")
            val phone = properties.readString("phone")
            val address = PlaceAddressResolver.formatFromOsmProperties(properties, anchor)
            val hoursSummary = meta.hoursSummary.ifBlank {
                context.getString(R.string.apple_place_hours_unknown)
            }
            return MapPlaceDetail(
                id = feature.id() ?: "${anchor.latitude},${anchor.longitude}",
                name = name,
                category = category,
                locality = meta.locality.ifBlank { localityFromAddress(properties) },
                hoursSummary = hoursSummary,
                isOpenNow = meta.isOpenNow,
                website = website,
                phone = phone,
                address = address,
                latLng = anchor,
                isClaimEligible = PlaceClaimEligibility.fromOsmProperties(properties, category),
            )
        }

        fun fallback(context: Context, latLng: LatLng): MapPlaceDetail {
            return MapPlaceDetail(
                id = "${latLng.latitude},${latLng.longitude}",
                name = context.getString(R.string.apple_place_unknown_name),
                category = context.getString(R.string.apple_place_unknown_category),
                locality = "",
                hoursSummary = context.getString(R.string.apple_place_hours_unknown),
                isOpenNow = false,
                website = null,
                phone = null,
                address = formatCoordinateAddress(latLng),
                latLng = latLng,
            )
        }

        private fun localityFromAddress(properties: JsonObject): String {
            return properties.readString("addr:city")
                ?: properties.readString("addr:state")
                ?: properties.readString("addr:country")
                ?: ""
        }

        private fun formatCoordinateAddress(latLng: LatLng): String {
            return "${latLng.latitude}, ${latLng.longitude}"
        }
    }
}

private fun JsonObject.readString(key: String): String? {
    return get(key)?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
}

internal enum class PlaceDetailPrimaryRouteAction {
    Directions,
    AddStop,
}

@Composable
internal fun AppleMapsPlaceDetailSheetContent(
    place: MapPlaceDetail,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    sheetGestures: AppleMapsSheetGestures,
    onClose: () -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    primaryRouteAction: PlaceDetailPrimaryRouteAction,
    onPrimaryRouteClick: () -> Unit,
    claimButtonMode: PlaceClaimButtonMode = PlaceClaimButtonMode.Claim,
    onClaimPlaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = sheetTheme.toPlaceDetailPalette()
    val density = LocalDensity.current
    var stickyHeaderHeightPx by remember(place.id) { mutableIntStateOf(0) }
    val stickyHeaderHeight = with(density) { stickyHeaderHeightPx.toDp() }
    var businessDetail by remember(place.id) { mutableStateOf<PlaceBusinessDetail?>(null) }

    LaunchedEffect(place.id) {
        businessDetail = when (
            val result = withContext(Dispatchers.IO) {
                PlaceDetailClient.fetch(place.businessPoiExternalRef())
            }
        ) {
            is PlaceDetailClient.FetchResult.Success -> result.detail
            is PlaceDetailClient.FetchResult.Failure -> null
        }
    }

    val enriched = businessDetail?.takeIf { it.hasContent }
    val displayHours = enriched?.metadata?.hours?.takeIf { it.isNotBlank() } ?: place.hoursSummary
    val displayWebsite = enriched?.metadata?.website?.takeIf { it.isNotBlank() } ?: place.website
    val displayPhone = enriched?.metadata?.phone?.takeIf { it.isNotBlank() } ?: place.phone
    val displayAddress = enriched?.address?.takeIf { it.isNotBlank() } ?: place.address
    val locationLabel = listOfNotNull(
        enriched?.metadata?.city?.takeIf { it.isNotBlank() },
        enriched?.metadata?.state?.takeIf { it.isNotBlank() },
    ).joinToString(", ").ifBlank { place.locality }
    val coordinatesLabel = "${place.latLng.latitude}, ${place.latLng.longitude}"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.sheetSurface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    sheetGestures.scrollContent(
                        scrollState = scrollState,
                        scrollEnabled = contentScrollEnabled,
                    ),
                )
                .padding(top = stickyHeaderHeight)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            enriched?.description?.takeIf { it.isNotBlank() }?.let { aboutText ->
                PlaceDetailSectionTitle(
                    title = stringResource(R.string.apple_place_about),
                    palette = palette,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = aboutText,
                    color = palette.primaryText,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            enriched?.photos?.takeIf { it.isNotEmpty() }?.let { photos ->
                PlaceDetailSectionTitle(
                    title = stringResource(R.string.apple_place_images),
                    palette = palette,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(photos, key = { it.id }) { photo ->
                        AsyncImage(
                            model = photo.url,
                            contentDescription = photo.caption,
                            modifier = Modifier
                                .size(width = 220.dp, height = 150.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            PlaceDetailSectionTitle(
                title = stringResource(R.string.apple_place_ratings_reviews),
                palette = palette,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(
                        R.string.apple_place_rating_average,
                        PlaceDetailDummyReviews.averageRating,
                    ),
                    color = palette.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    PlaceDetailDummyReviews.reviews,
                    key = { "${it.authorName}:${it.text}" },
                ) { review ->
                    PlaceDetailReviewCard(review = review, palette = palette)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.apple_place_details),
                color = palette.primaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))

            PlaceDetailDetailsCard(
                hoursSummary = displayHours,
                isOpenNow = place.isOpenNow,
                website = displayWebsite,
                phone = displayPhone,
                location = locationLabel,
                coordinates = coordinatesLabel,
                address = displayAddress,
                palette = palette,
            )

            if (claimButtonMode != PlaceClaimButtonMode.Hidden) {
                Spacer(modifier = Modifier.height(12.dp))
                PlaceDetailClaimCard(
                    palette = palette,
                    mode = claimButtonMode,
                    onClick = onClaimPlaceClick,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
                .background(palette.sheetSurface)
                .then(sheetGestures.chromeDrag)
                .onSizeChanged { stickyHeaderHeightPx = it.height },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(sheetGestures.grabberDrag),
            ) {
                AppleMapsSheetGrabber(
                    grabberColor = palette.grabber,
                )
            }
            PlaceDetailHeader(
                place = place,
                palette = palette,
                onClose = onClose,
            )
            PlaceDetailQuickActions(
                palette = palette,
                primaryRouteAction = primaryRouteAction,
                onPrimaryRouteClick = onPrimaryRouteClick,
            )
        }
    }
}

@Composable
private fun PlaceDetailHeader(
    place: MapPlaceDetail,
    palette: PlaceDetailPalette,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                color = palette.primaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = place.category,
                    color = palette.accent,
                    fontSize = 15.sp,
                )
                if (place.locality.isNotBlank()) {
                    Text(
                        text = " · ${place.locality}",
                        color = palette.accent,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        PlaceDetailHeaderIconButton(
            imageVector = Icons.Outlined.Share,
            contentDescription = stringResource(R.string.apple_place_share),
            iconTint = palette.headerIconTint,
            surfaceColor = palette.headerSecondaryFill,
        )
        Spacer(modifier = Modifier.width(8.dp))
        PlaceDetailHeaderIconButton(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.apple_close),
            iconTint = palette.headerIconTint,
            surfaceColor = palette.headerSecondaryFill,
            onClick = onClose,
        )
    }
}

@Composable
private fun PlaceDetailHeaderIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    iconTint: Color,
    surfaceColor: Color,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .appleMapsSheetInteractiveBlock(),
        shape = CircleShape,
        color = surfaceColor,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun PlaceDetailQuickActions(
    palette: PlaceDetailPalette,
    primaryRouteAction: PlaceDetailPrimaryRouteAction,
    onPrimaryRouteClick: () -> Unit,
) {
    val primaryLabel = when (primaryRouteAction) {
        PlaceDetailPrimaryRouteAction.Directions ->
            stringResource(R.string.apple_place_directions)
        PlaceDetailPrimaryRouteAction.AddStop ->
            stringResource(R.string.apple_place_add_stop)
    }
    val primaryIcon = when (primaryRouteAction) {
        PlaceDetailPrimaryRouteAction.Directions -> Icons.Outlined.Directions
        PlaceDetailPrimaryRouteAction.AddStop -> Icons.Outlined.AddCircle
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlaceDetailPrimaryAction(
            label = primaryLabel,
            icon = primaryIcon,
            onClick = onPrimaryRouteClick,
            backgroundColor = palette.primaryActionBg,
            modifier = Modifier.weight(1f),
        )
        PlaceDetailSecondaryAction(
            label = stringResource(R.string.apple_place_call),
            icon = Icons.Outlined.Call,
            onClick = {},
            palette = palette,
            modifier = Modifier.weight(1f),
        )
        PlaceDetailSecondaryAction(
            label = stringResource(R.string.apple_place_website),
            icon = Icons.Outlined.Language,
            onClick = {},
            palette = palette,
            modifier = Modifier.weight(1f),
        )
        PlaceDetailSecondaryAction(
            label = stringResource(R.string.apple_place_more),
            icon = Icons.Outlined.MoreHoriz,
            onClick = {},
            palette = palette,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PlaceDetailPrimaryAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .appleMapsSheetInteractiveBlock(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlaceDetailSecondaryAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    palette: PlaceDetailPalette,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .appleMapsSheetInteractiveBlock(),
        shape = RoundedCornerShape(12.dp),
        color = palette.secondaryActionBg,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.secondaryActionIconTint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = palette.secondaryActionLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlaceDetailSectionTitle(
    title: String,
    palette: PlaceDetailPalette,
) {
    Text(
        text = title,
        color = palette.primaryText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun PlaceDetailReviewCard(
    review: PlaceReview,
    palette: PlaceDetailPalette,
) {
    PlaceDetailCard(
        modifier = Modifier.width(260.dp),
        palette = palette,
    ) {
        Text(
            text = review.authorName.ifBlank { "Anonymous" },
            color = palette.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(review.rating.coerceIn(0, 5)) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        if (review.text.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.text,
                color = palette.secondaryText,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun PlaceDetailDetailsCard(
    hoursSummary: String,
    isOpenNow: Boolean,
    website: String?,
    phone: String?,
    location: String,
    coordinates: String,
    address: String,
    palette: PlaceDetailPalette,
) {
    PlaceDetailCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        palette = palette,
    ) {
        PlaceDetailLabeledValue(
            label = stringResource(R.string.apple_place_hours),
            value = hoursSummary,
            valueColor = palette.primaryText,
            labelColor = palette.secondaryText,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                if (isOpenNow) {
                    R.string.apple_place_open_now
                } else {
                    R.string.apple_place_closed_now
                },
            ),
            color = if (isOpenNow) palette.accent else palette.closedStatus,
            fontSize = 15.sp,
        )
        PlaceDetailDivider(palette = palette)
        if (!website.isNullOrBlank()) {
            PlaceDetailLabeledValue(
                label = stringResource(R.string.apple_place_website),
                value = website,
                valueColor = palette.accent,
                labelColor = palette.secondaryText,
            )
            PlaceDetailDivider(palette = palette)
        }
        if (location.isNotBlank()) {
            PlaceDetailLabeledValue(
                label = stringResource(R.string.apple_place_location),
                value = location,
                valueColor = palette.primaryText,
                labelColor = palette.secondaryText,
            )
            PlaceDetailDivider(palette = palette)
        }
        PlaceDetailLabeledValue(
            label = stringResource(R.string.apple_place_coordinates),
            value = coordinates,
            valueColor = palette.primaryText,
            labelColor = palette.secondaryText,
        )
        PlaceDetailDivider(palette = palette)
        if (!phone.isNullOrBlank()) {
            PlaceDetailLabeledValue(
                label = stringResource(R.string.apple_place_phone),
                value = phone,
                valueColor = palette.accent,
                labelColor = palette.secondaryText,
            )
            PlaceDetailDivider(palette = palette)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                PlaceDetailLabeledValue(
                    label = stringResource(R.string.apple_place_address),
                    value = address,
                    valueColor = palette.primaryText,
                    labelColor = palette.secondaryText,
                )
            }
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .appleMapsSheetInteractiveBlock(),
                shape = CircleShape,
                color = palette.accent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.TurnRight,
                        contentDescription = stringResource(R.string.apple_place_directions_short),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceDetailClaimCard(
    palette: PlaceDetailPalette,
    mode: PlaceClaimButtonMode,
    onClick: () -> Unit,
) {
    val labelRes = when (mode) {
        PlaceClaimButtonMode.Hidden -> R.string.apple_place_claim
        PlaceClaimButtonMode.Loading -> R.string.apple_place_claim
        PlaceClaimButtonMode.Claim -> R.string.apple_place_claim
        PlaceClaimButtonMode.BusinessEdit -> R.string.apple_place_business_edit
    }
    PlaceDetailCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .appleMapsSheetInteractiveBlock(),
        palette = palette,
        onClick = if (mode == PlaceClaimButtonMode.Loading) null else onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Verified,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(labelRes),
                color = palette.accent.copy(alpha = if (mode == PlaceClaimButtonMode.Loading) 0.5f else 1f),
                fontSize = 17.sp,
            )
        }
    }
}

@Composable
private fun PlaceDetailCard(
    modifier: Modifier = Modifier,
    palette: PlaceDetailPalette,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            color = palette.cardSurface,
            shadowElevation = 0.dp,
            onClick = onClick,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            color = palette.cardSurface,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PlaceDetailLabeledValue(
    label: String,
    value: String,
    valueColor: Color,
    labelColor: Color,
) {
    Text(
        text = label,
        color = labelColor,
        fontSize = 13.sp,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = value,
        color = valueColor,
        fontSize = 17.sp,
    )
}

@Composable
private fun PlaceDetailDivider(palette: PlaceDetailPalette) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        color = palette.divider,
    )
}
