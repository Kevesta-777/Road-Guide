package com.example.roadguideapp.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme

internal data class CompanionVehicleType(
    val name: String,
    val seatsLabel: String,
    val defaultSeats: Int,
    val icon: ImageVector,
)

internal val companionVehicleTypeOptions = listOf(
    CompanionVehicleType("Sedan", "4 seats", 4, Icons.Outlined.DirectionsCar),
    CompanionVehicleType("SUV", "6 seats", 6, Icons.Outlined.DirectionsCar),
    CompanionVehicleType("Hatchback", "4 seats", 4, Icons.Outlined.DirectionsCar),
    CompanionVehicleType("Truck", "2 seats", 2, Icons.Outlined.LocalShipping),
    CompanionVehicleType("Van", "7 seats", 7, Icons.Outlined.DirectionsCar),
    CompanionVehicleType("Bus", "20+ seats", 20, Icons.Outlined.DirectionsBus),
    CompanionVehicleType("Motorbike", "1 seat", 1, Icons.Outlined.TwoWheeler),
)

@Composable
internal fun CompanionVehicleTypePicker(
    selectedVehicle: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onVehicleSelected: (CompanionVehicleType) -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    embeddedInForm: Boolean = true,
) {
    val selectedOption = companionVehicleTypeOptions.firstOrNull { it.name == selectedVehicle }
    val accent = sheetTheme.accent
    val selectedBg = accent.copy(alpha = if (sheetTheme.isLight) 0.10f else 0.18f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sheetTheme.searchFieldFill)
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuthIconBadge(
                icon = selectedOption?.icon ?: Icons.Outlined.DirectionsCar,
                sheetTheme = sheetTheme,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Text(
                text = selectedOption?.name ?: stringResource(R.string.companion_vehicle_select_hint),
                color = if (selectedOption != null) sheetTheme.primaryText else sheetTheme.searchFieldHint,
                fontSize = 16.sp,
                fontWeight = if (selectedOption != null) FontWeight.Normal else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = sheetTheme.secondaryText,
            )
        }

        if (expanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (embeddedInForm) {
                    AuthFormDivider(sheetTheme = sheetTheme)
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                companionVehicleTypeOptions.forEachIndexed { index, option ->
                    if (embeddedInForm && index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = sheetTheme.divider.copy(alpha = 0.6f),
                        )
                    }
                    val isSelected = option.name == selectedVehicle
                    VehicleTypeListItem(
                        option = option,
                        isSelected = isSelected,
                        sheetTheme = sheetTheme,
                        accent = accent,
                        selectedBg = selectedBg,
                        onClick = {
                            onVehicleSelected(option)
                            onExpandedChange(false)
                        },
                    )
                }
                if (embeddedInForm) {
                    AuthFormDivider(sheetTheme = sheetTheme)
                } else {
                    HorizontalDivider(color = sheetTheme.divider.copy(alpha = 0.7f))
                }
                VehicleTypeTrustFooter(sheetTheme = sheetTheme, accent = accent)
            }
        }
    }
}

@Composable
private fun VehicleTypeListItem(
    option: CompanionVehicleType,
    isSelected: Boolean,
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
    selectedBg: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) selectedBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AuthIconBadge(
            icon = option.icon,
            sheetTheme = sheetTheme,
            size = 40.dp,
            iconSize = 20.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.name,
                color = sheetTheme.primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = option.seatsLabel,
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun VehicleTypeTrustFooter(
    sheetTheme: AppleMapsSheetTheme,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(24.dp),
        )
        Column {
            Text(
                text = stringResource(R.string.companion_vehicle_trust_title),
                color = sheetTheme.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.companion_vehicle_trust_subtitle),
                color = sheetTheme.secondaryText,
                fontSize = 12.sp,
            )
        }
    }
}
