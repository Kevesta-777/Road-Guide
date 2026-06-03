package com.example.roadguideapp.map

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
internal fun NearbyShortcutsSection(
    sheetTheme: AppleMapsSheetTheme,
    onShortcutClick: (AppleNearbyShortcut) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.apple_find_nearby),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = sheetTheme.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        NearbyCategorySearch.shortcuts.chunked(2).forEach { rowShortcuts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowShortcuts.forEach { item ->
                    AppleNearbyCell(
                        item = item,
                        sheetTheme = sheetTheme,
                        onClick = { onShortcutClick(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowShortcuts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
internal fun AppleNearbyCell(
    item: AppleNearbyShortcut,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick)
            .background(sheetTheme.nearbyCellBackground, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(item.tint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (item.labelRes == R.string.apple_cat_parking) {
                Text(
                    text = "P",
                    color = sheetTheme.nearbyCellText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            } else {
                Text(text = item.emoji, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(item.labelRes),
            color = sheetTheme.nearbyCellText,
            fontSize = 16.sp,
        )
    }
}
