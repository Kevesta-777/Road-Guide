package com.example.roadguideapp.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

internal val SearchFieldHeight = 48.dp
internal val SearchFieldCornerRadius = 12.dp
internal val SearchCardCornerRadius = 14.dp

@Composable
internal fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = Icons.Outlined.Search,
    showClearWhenNonEmpty: Boolean = true,
    focusRequester: FocusRequester? = null,
    onFocus: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SearchFieldHeight)
            .background(sheetTheme.searchFieldFill, RoundedCornerShape(SearchFieldCornerRadius))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = sheetTheme.searchFieldHint,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        val fieldModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .then(
                if (onFocus != null) {
                    Modifier.onFocusChanged { state ->
                        if (state.isFocused) onFocus()
                    }
                } else {
                    Modifier
                },
            )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            modifier = fieldModifier,
            textStyle = TextStyle(
                color = sheetTheme.searchFieldText,
                fontSize = 17.sp,
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = if (onSubmit != null) {
                KeyboardActions(onSearch = { onSubmit.invoke() })
            } else {
                keyboardActions
            },
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(sheetTheme.searchFieldText),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = sheetTheme.searchFieldHint,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            },
        )
        when {
            trailingContent != null -> trailingContent()
            showClearWhenNonEmpty && value.isNotEmpty() && !readOnly -> {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.apple_close),
                    tint = sheetTheme.searchFieldHint,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            onValueChange("")
                            onClear?.invoke()
                        }
                        .padding(5.dp),
                )
            }
        }
    }
}

@Composable
internal fun SearchFilterChip(
    label: String,
    sheetTheme: AppleMapsSheetTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDropdown: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(
                color = if (selected) {
                    sheetTheme.accent.copy(alpha = 0.16f)
                } else {
                    sheetTheme.searchFieldFill
                },
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (selected) sheetTheme.accent else sheetTheme.primaryText,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showDropdown) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = if (selected) sheetTheme.accent else sheetTheme.secondaryText,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
internal fun SearchFilterChipRow(
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
internal fun SearchResultsCountLabel(
    count: Int,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    labelRes: Int = R.string.search_results_count,
) {
    Text(
        text = stringResource(labelRes, count),
        color = sheetTheme.tertiaryText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 6.dp),
    )
}

@Composable
internal fun SearchLoadingRow(
    sheetTheme: AppleMapsSheetTheme,
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = sheetTheme.accent,
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            color = sheetTheme.secondaryText,
            fontSize = 15.sp,
        )
    }
}

@Composable
internal fun SearchEmptyState(
    title: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(sheetTheme.searchFieldFill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = sheetTheme.secondaryText,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = sheetTheme.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = sheetTheme.secondaryText,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

internal data class PeliasLayerIconStyle(
    val icon: ImageVector,
    val background: Color,
    val foreground: Color,
)

internal fun peliasLayerIconStyle(layer: String?): PeliasLayerIconStyle {
    return when (layer?.lowercase()) {
        "venue", "address" -> PeliasLayerIconStyle(
            Icons.Outlined.Place,
            Color(0xFFE8F0FE),
            Color(0xFF0A84FF),
        )
        "street", "intersection" -> PeliasLayerIconStyle(
            Icons.Outlined.Route,
            Color(0xFFF2F2F7),
            Color(0xFF636366),
        )
        "locality", "localadmin", "borough", "neighbourhood" -> PeliasLayerIconStyle(
            Icons.Outlined.LocationCity,
            Color(0xFFF2F2F7),
            Color(0xFF636366),
        )
        "region", "macroregion", "country" -> PeliasLayerIconStyle(
            Icons.Outlined.Public,
            Color(0xFFF2F2F7),
            Color(0xFF636366),
        )
        else -> PeliasLayerIconStyle(
            Icons.Outlined.Place,
            Color(0xFFF2F2F7),
            Color(0xFF636366),
        )
    }
}
