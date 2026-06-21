package com.example.roadguideapp.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppleMapsChooseMapModal(
    sheetTheme: AppleMapsSheetTheme,
    currentVariant: MapStyleVariant,
    onDismiss: () -> Unit,
    onStyleSelected: (MapStyleVariant) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selected by remember(currentVariant) { mutableStateOf(currentVariant) }

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetTheme.sheetSurface,
        contentColor = sheetTheme.primaryText,
        dragHandle = { AppleMapsSheetGrabber(grabberColor = sheetTheme.grabber) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.apple_choose_map_title),
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

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                AppleChooseMapCard(
                    sheetTheme = sheetTheme,
                    selected = selected == MapStyleVariant.Standard,
                    onClick = {
                        selected = MapStyleVariant.Standard
                        onStyleSelected(MapStyleVariant.Standard)
                    },
                    label = stringResource(R.string.apple_map_style_standard),
                    modifier = Modifier.fillMaxWidth(),
                    squarePreview = false,
                ) {
                    AppleMapsChooseMapPreview.Standard(
                        isLight = sheetTheme.isLight,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppleChooseMapCard(
                        sheetTheme = sheetTheme,
                        selected = selected == MapStyleVariant.Hybrid,
                        onClick = {
                            selected = MapStyleVariant.Hybrid
                            onStyleSelected(MapStyleVariant.Hybrid)
                        },
                        label = stringResource(R.string.apple_map_style_hybrid),
                        modifier = Modifier.weight(1f),
                        squarePreview = true,
                    ) {
                        AppleMapsChooseMapPreview.Hybrid(Modifier.fillMaxSize())
                    }
                    AppleChooseMapCard(
                        sheetTheme = sheetTheme,
                        selected = selected == MapStyleVariant.Satellite,
                        onClick = {
                            selected = MapStyleVariant.Satellite
                            onStyleSelected(MapStyleVariant.Satellite)
                        },
                        label = stringResource(R.string.apple_map_style_satellite),
                        modifier = Modifier.weight(1f),
                        squarePreview = true,
                    ) {
                        AppleMapsChooseMapPreview.Satellite(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun AppleChooseMapCard(
    sheetTheme: AppleMapsSheetTheme,
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    squarePreview: Boolean = false,
    preview: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val selectionAccent = sheetTheme.accent
    val interactionSource = remember { MutableInteractionSource() }

    val previewModifier = if (squarePreview) {
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f) // height = 2/3 of width (was 1:1)
    } else {
        Modifier
            .fillMaxWidth()
            .height(128.dp * (2f / 3f))
    }

    Column(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.border(width = 2.5.dp, color = selectionAccent, shape = shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = previewModifier
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(sheetTheme.directionsInnerCard),
        ) {
            preview()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(sheetTheme.cardElevated),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = sheetTheme.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppleMapsMyLocationModal(
    sheetTheme: AppleMapsSheetTheme,
    onDismiss: () -> Unit,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onGoToLocation: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetTheme.sheetSurface,
        contentColor = sheetTheme.primaryText,
        dragHandle = { AppleMapsSheetGrabber(grabberColor = sheetTheme.grabber) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.apple_my_location_title),
                style = MaterialTheme.typography.titleLarge,
                color = sheetTheme.primaryText,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.apple_my_location_body),
                style = MaterialTheme.typography.bodyMedium,
                color = sheetTheme.secondaryText,
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (!hasPermission) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sheetTheme.accent,
                        contentColor = sheetTheme.onAccent,
                    ),
                ) {
                    Text(stringResource(R.string.apple_location_allow))
                }
            } else {
                Button(
                    onClick = {
                        onGoToLocation()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sheetTheme.accent,
                        contentColor = sheetTheme.onAccent,
                    ),
                ) {
                    Text(stringResource(R.string.apple_location_go_here))
                }
            }
        }
    }
}

@Composable
internal fun OfflineGraphImportAlertDialog(
    onDismiss: () -> Unit,
    onImportFolderClick: () -> Unit,
    isImporting: Boolean,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        title = {
            Text(stringResource(R.string.directions_offline_import_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isImporting) {
                    Text(stringResource(R.string.directions_offline_import_message))
                }
                if (isImporting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                        )
                        Text(stringResource(R.string.directions_offline_import_in_progress))
                    }
                } 
            }
        },
        confirmButton = {
            Button(
                onClick = onImportFolderClick,
                enabled = !isImporting,
            ) {
                Text(stringResource(R.string.directions_offline_import_action_folder))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isImporting,
            ) {
                Text(stringResource(R.string.apple_cancel))
            }
        },
    )
}

@Composable
internal fun AppleMapsSheetGrabber(
    onClick: (() -> Unit)? = null,
    grabberColor: Color = AppleMapsUiTokens.SheetGrabber,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {     
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(5.dp)
                .background(grabberColor, RoundedCornerShape(2.5.dp)),
        )
    }
    
}
