package com.example.roadguideapp.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

@Composable
internal fun MyQrCodeScreen(
    profileId: String,
    displayName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetTheme = rememberAuthSheetTheme()
    val density = LocalDensity.current
    val qrSizePx = with(density) { 220.dp.roundToPx() }
    val payload = remember(profileId, displayName) {
        FriendQrPayload.encode(profileId, displayName)
    }
    val qrBitmap = remember(payload, qrSizePx) {
        payload?.let { QrCodeBitmap.encode(it, qrSizePx) }
    }

    AuthPageScaffold(
        title = stringResource(R.string.friends_my_qr_title),
        subtitle = stringResource(R.string.friends_my_qr_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        if (payload == null) {
            Text(
                text = stringResource(R.string.friends_qr_profile_unavailable),
                color = sheetTheme.accent,
                fontSize = 15.sp,
            )
        } else {
            AuthGroupedCard(sheetTheme = sheetTheme) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = displayName,
                        color = sheetTheme.primaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.friends_my_qr),
                            modifier = Modifier.size(220.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = profileId,
                        color = sheetTheme.secondaryText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
