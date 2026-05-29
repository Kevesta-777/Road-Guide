package com.example.roadguideapp.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

@Composable
internal fun AppleMapsTopRightChrome(
    sheetTheme: AppleMapsSheetTheme,
    isDarkAppearance: Boolean,
    is3d: Boolean,
    mapBearingDegrees: Float,
    onChooseMapClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    onToggleAppearanceClick: () -> Unit,
    onToggle3dClick: () -> Unit,
    onCompassClick: () -> Unit,
    onCompassBearingDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(AppleMapsUiTokens.ControlCornerDp),
            color = sheetTheme.mapControlGlass,
            shadowElevation = 6.dp,
        ) {
            Column(modifier = Modifier.width(AppleMapsUiTokens.CompassSizeDp)) {
                IconButton(onClick = onChooseMapClick) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = stringResource(R.string.apple_choose_map),
                        tint = sheetTheme.mapControlIcon,
                    )
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = sheetTheme.mapControlDivider,
                )
                IconButton(onClick = onMyLocationClick) {
                    Icon(
                        imageVector = Icons.Outlined.NearMe,
                        contentDescription = stringResource(R.string.apple_my_location),
                        tint = sheetTheme.mapControlIcon,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppleMapsUiTokens.ControlSeparationDp))

        Surface(
            modifier = Modifier.size(AppleMapsUiTokens.CompassSizeDp),
            shape = RoundedCornerShape(10.dp),
            color = sheetTheme.mapControlGlass,
            shadowElevation = 6.dp,
        ) {
            IconButton(
                onClick = onToggleAppearanceClick,
                modifier = Modifier.size(AppleMapsUiTokens.CompassSizeDp),
            ) {
                Icon(
                    imageVector = if (isDarkAppearance) {
                        Icons.Outlined.LightMode
                    } else {
                        Icons.Outlined.DarkMode
                    },
                    contentDescription = if (isDarkAppearance) {
                        stringResource(R.string.apple_appearance_light)
                    } else {
                        stringResource(R.string.apple_appearance_dark)
                    },
                    tint = sheetTheme.mapControlIcon,
                )
            }
        }

        Spacer(modifier = Modifier.height(AppleMapsUiTokens.ControlSeparationDp))

        Surface(
            modifier = Modifier.size(AppleMapsUiTokens.CompassSizeDp),
            shape = RoundedCornerShape(10.dp),
            color = sheetTheme.mapControlGlass,
            shadowElevation = 6.dp,
        ) {
            IconButton(
                onClick = onToggle3dClick,
                modifier = Modifier.size(AppleMapsUiTokens.CompassSizeDp),
            ) {
                Text(
                    text = if (is3d) {
                        stringResource(R.string.apple_two_d)
                    } else {
                        stringResource(R.string.apple_three_d_label)
                    },
                    color = sheetTheme.mapControlIcon,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (shouldShowAppleMapsCompass(mapBearingDegrees, is3d)) {
            Spacer(modifier = Modifier.height(AppleMapsUiTokens.ControlSeparationDp))
            AppleMapsCompassControl(
                sheetTheme = sheetTheme,
                mapBearingDegrees = mapBearingDegrees,
                onClick = onCompassClick,
                onBearingDragDegrees = onCompassBearingDrag,
                compassContentDescription = stringResource(R.string.apple_compass),
            )
        }
    }
}
