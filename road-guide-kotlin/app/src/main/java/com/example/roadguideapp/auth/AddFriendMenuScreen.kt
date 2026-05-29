package com.example.roadguideapp.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R

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
        subtitle = stringResource(R.string.friends_list_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        AuthPrimaryButton(
            text = stringResource(R.string.friends_scan_qr),
            onClick = onScanQr,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthSecondaryButton(
            text = stringResource(R.string.friends_add_by_id),
            onClick = onAddById,
            sheetTheme = sheetTheme,
        )
    }
}
