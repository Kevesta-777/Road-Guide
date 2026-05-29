package com.example.roadguideapp.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LookAroundModal(
    sheetTheme: AppleMapsSheetTheme,
    target: LookAroundTarget,
    onDismiss: () -> Unit,
    onCenterMap: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    LaunchedEffect(Unit) { sheetState.expand() }

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
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.apple_look_around),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.apple_close),
                        tint = sheetTheme.primaryText,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF5AC8FA), Color(0xFF34C759)),
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.TravelExplore,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.height(48.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = target.title,
                color = sheetTheme.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            if (target.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = target.subtitle,
                    color = sheetTheme.secondaryText,
                    fontSize = 15.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.apple_look_around_body),
                color = sheetTheme.secondaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            if (target.latLng != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    onClick = {
                        onCenterMap()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = sheetTheme.accent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.apple_look_around_center_map),
                        color = sheetTheme.onAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
