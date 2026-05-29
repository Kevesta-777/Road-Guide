package com.example.roadguideapp.map

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

/**
 * Add-stop sheet shown over the map while directions are active (Apple Maps–style).
 * Height tracks the persistent sheet via [linkedSheetHeightDp].
 */
@Composable
internal fun AppleMapsAddStopPanel(
    linkedSheetHeightDp: Dp? = null,
    sheetGestures: AppleMapsSheetGestures,
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    onCancel: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

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
                AppleMapsSheetGrabber(grabberColor = sheetTheme.grabber)
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
                        .padding(horizontal = 8.dp)
                        .padding(top = 4.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = stringResource(R.string.apple_cancel),
                            color = sheetTheme.accent,
                            fontSize = 17.sp,
                        )
                    }
                    Text(
                        text = stringResource(R.string.apple_directions_add_stop),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = sheetTheme.primaryText,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(72.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AddStopSearchRow(
                        sheetTheme = sheetTheme,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                    )
                    AddStopMyLocationRow(
                        sheetTheme = sheetTheme,
                        onClick = onMyLocationClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddStopSearchRow(
    sheetTheme: AppleMapsSheetTheme,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(sheetTheme.cardElevated),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 14.dp)
                .size(28.dp)
                .background(sheetTheme.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = sheetTheme.onAccent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 14.dp),
            textStyle = TextStyle(
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
            ),
            singleLine = true,
            cursorBrush = SolidColor(sheetTheme.accent),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.apple_add_stop_search_hint),
                            color = sheetTheme.secondaryText,
                            fontSize = 17.sp,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun AddStopMyLocationRow(
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(sheetTheme.cardElevated)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 14.dp)
                .size(28.dp)
                .background(sheetTheme.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.NearMe,
                contentDescription = null,
                tint = sheetTheme.onAccent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.apple_my_location),
            color = sheetTheme.primaryText,
            fontSize = 17.sp,
        )
    }
}
