package com.example.roadguideapp.map

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R

/**
 * Appearance + optional 3D controls for map settings sheets (dark sheets use [useDarkOnSurfaceColors]).
 */
@Composable
internal fun MapSettingsSheetContent(
    timeOfDay: MapTimeOfDay,
    modifier: Modifier = Modifier,
    is3d: Boolean = false,
    on3dChange: ((Boolean) -> Unit)? = null,
    /** When false, omits the 3D row (3D is controlled elsewhere, e.g. chrome toggle). */
    show3dToggle: Boolean = false,
    useDarkOnSurfaceColors: Boolean = false,
) {
    val muted = if (useDarkOnSurfaceColors) Color(0xFFAEAEB2) else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryText = if (useDarkOnSurfaceColors) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.map_settings),
            style = MaterialTheme.typography.titleLarge,
            color = primaryText,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.appearance),
            style = MaterialTheme.typography.titleSmall,
            color = muted,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.appearance_follows_system),
            style = MaterialTheme.typography.bodySmall,
            color = muted,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MapTimeOfDay.entries.forEach { mode ->
                FilterChip(
                    selected = timeOfDay == mode,
                    onClick = { },
                    enabled = false,
                    label = { Text(mode.label) },
                )
            }
        }

        if (show3dToggle && on3dChange != null) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = if (useDarkOnSurfaceColors) Color(0x33FFFFFF) else MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.three_d_view),
                    style = MaterialTheme.typography.bodyLarge,
                    color = primaryText,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = is3d,
                    onCheckedChange = on3dChange,
                )
            }
        }
    }
}
