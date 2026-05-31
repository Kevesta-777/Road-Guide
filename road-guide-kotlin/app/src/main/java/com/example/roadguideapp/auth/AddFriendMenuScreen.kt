package com.example.roadguideapp.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.QrCodeScanner

@Composable
internal fun AddFriendMenuScreen(
    onBack: () -> Unit,
    onScanQr: () -> Unit,
    onAddById: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetTheme = rememberAuthSheetTheme()

    AuthPageScaffold(
        title = stringResource(R.string.friends_add),
        subtitle = stringResource(R.string.friends_add_menu_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        AuthOptionCard(
            icon = Icons.Outlined.QrCodeScanner,
            title = stringResource(R.string.friends_scan_qr),
            subtitle = stringResource(R.string.friends_scan_qr_hint),
            sheetTheme = sheetTheme,
            onClick = onScanQr,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthOptionCard(
            icon = Icons.Outlined.Badge,
            title = stringResource(R.string.friends_add_by_id),
            subtitle = stringResource(R.string.friends_add_by_id_hint),
            sheetTheme = sheetTheme,
            onClick = onAddById,
        )
    }
}
