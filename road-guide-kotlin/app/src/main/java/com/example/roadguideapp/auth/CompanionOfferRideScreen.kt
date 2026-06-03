package com.example.roadguideapp.auth

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.map.PeliasSearchClient
import com.example.roadguideapp.map.PeliasSearchResponse
import com.example.roadguideapp.map.PeliasSearchResult
import com.example.roadguideapp.map.AppleMapsSheetTheme
import com.example.roadguideapp.map.PeliasSearchResultsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.gestures.detectDragGestures
import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private sealed class AddressField {
    data object From : AddressField()
    data object To : AddressField()
    data class Waypoint(val index: Int) : AddressField()
}

@Composable
internal fun CompanionOfferRideScreen(
    onBack: () -> Unit,
    onRideOffered: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sheetTheme = rememberAuthSheetTheme()
    val scope = rememberCoroutineScope()

    var fromInput by rememberSaveable { mutableStateOf("") }
    var toInput by rememberSaveable { mutableStateOf("") }
    val waypointInputs = remember { mutableStateListOf<String>() }

    var selectedAddressField by remember { mutableStateOf<AddressField?>(null) }
    var addressSuggestions by remember { mutableStateOf<List<PeliasSearchResult>>(emptyList()) }
    var addressLoading by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var selectingSuggestion by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showPickupPicker by remember { mutableStateOf(false) }

    var seatsInput by rememberSaveable { mutableStateOf("1") }
    var priceInput by rememberSaveable { mutableStateOf("0") }
    var vehicleInput by rememberSaveable { mutableStateOf("") }
    var vehicleMenuExpanded by remember { mutableStateOf(false) }
    var preferencesInput by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Subscription gating disabled until subscription page is implemented.
    // var canOfferRide by remember { mutableStateOf<Boolean?>(null) }
    // val premiumRequiredMessage = stringResource(R.string.subscription_premium_required_message)
    // LaunchedEffect(Unit) {
    //     val token = OfflineAuthStore.sessionToken(context)
    //     if (token.isNullOrBlank()) {
    //         canOfferRide = false
    //         return@LaunchedEffect
    //     }
    //     when (val result = withContext(Dispatchers.IO) { SubscriptionClient.getStatus(token) }) {
    //         is SubscriptionClient.StatusResult.Success -> canOfferRide = result.status.canOfferRide
    //         is SubscriptionClient.StatusResult.Failure -> canOfferRide = false
    //     }
    // }

    val notSignedInMessage = stringResource(R.string.companion_error_not_signed_in)
    val requiredFieldsMessage = stringResource(R.string.companion_error_required_fields)
    val seatsMessage = stringResource(R.string.companion_error_seats)
    val dateTimeMessage = stringResource(R.string.companion_error_depart_at)

    val departAtIso = remember(selectedDate, selectedTime) {
        if (selectedDate == null || selectedTime == null) {
            ""
        } else {
            LocalDateTime.of(selectedDate, selectedTime)
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
    }

    val activeQuery = remember(selectedAddressField, fromInput, toInput, waypointInputs.size, waypointInputs.joinToString("|")) {
        when (val field = selectedAddressField) {
            AddressField.From -> fromInput.trim()
            AddressField.To -> toInput.trim()
            is AddressField.Waypoint -> waypointInputs.getOrNull(field.index).orEmpty().trim()
            null -> ""
        }
    }

    LaunchedEffect(activeQuery, selectedAddressField) {
        if (selectingSuggestion) {
            selectingSuggestion = false
            return@LaunchedEffect
        }
        if (selectedAddressField == null || activeQuery.length < 2) {
            addressSuggestions = emptyList()
            addressLoading = false
            addressError = null
            return@LaunchedEffect
        }

        addressLoading = true
        addressError = null
        when (val result = withContext(Dispatchers.IO) { PeliasSearchClient.autocomplete(activeQuery) }) {
            is PeliasSearchResponse.Success -> {
                addressSuggestions = result.results
                addressLoading = false
            }
            is PeliasSearchResponse.Failure -> {
                addressSuggestions = emptyList()
                addressError = result.message
                addressLoading = false
            }
        }
    }

    val normalizedWaypoints = remember(waypointInputs.size, waypointInputs.joinToString("|")) {
        waypointInputs.map { it.trim() }.filter { it.isNotEmpty() }
    }
    val routePreview = remember(fromInput, toInput, normalizedWaypoints) {
        val segments = mutableListOf<String>()
        val from = fromInput.trim()
        val to = toInput.trim()
        if (from.isNotEmpty()) segments.add(from)
        segments.addAll(normalizedWaypoints)
        if (to.isNotEmpty()) segments.add(to)
        segments.joinToString(" → ")
    }

    fun moveWaypoint(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in waypointInputs.indices || toIndex !in waypointInputs.indices || fromIndex == toIndex) return
        val item = waypointInputs.removeAt(fromIndex)
        waypointInputs.add(toIndex, item)
        selectedAddressField = when (val selected = selectedAddressField) {
            is AddressField.Waypoint -> when {
                selected.index == fromIndex -> AddressField.Waypoint(toIndex)
                fromIndex < selected.index && toIndex >= selected.index -> AddressField.Waypoint(selected.index - 1)
                fromIndex > selected.index && toIndex <= selected.index -> AddressField.Waypoint(selected.index + 1)
                else -> selected
            }
            else -> selected
        }
    }

    LaunchedEffect(showPickupPicker) {
        if (showPickupPicker) {
            vehicleMenuExpanded = false
        }
    }

    CompanionPickupDateTimePickerSheet(
        visible = showPickupPicker,
        initialDate = selectedDate,
        initialTime = selectedTime,
        sheetTheme = sheetTheme,
        onDismiss = { showPickupPicker = false },
        onConfirm = { date, time ->
            selectedDate = date
            selectedTime = time
        },
    )

    AuthPageScaffold(
        title = stringResource(R.string.companion_offer_title),
        subtitle = stringResource(R.string.companion_offer_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        AuthSectionLabel(
            text = stringResource(R.string.companion_section_route),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            AuthField(
                label = stringResource(R.string.companion_field_from),
                value = fromInput,
                onValueChange = {
                    fromInput = it
                    selectedAddressField = AddressField.From
                    vehicleMenuExpanded = false
                },
                placeholder = stringResource(R.string.companion_hint_from),
                sheetTheme = sheetTheme,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
            if (selectedAddressField == AddressField.From && activeQuery.length >= 2) {
                PeliasSearchResultsList(
                    sheetTheme = sheetTheme,
                    suggestions = addressSuggestions,
                    loading = addressLoading,
                    errorMessage = addressError,
                    query = activeQuery,
                    onResultSelected = { selected ->
                        selectingSuggestion = true
                        fromInput = selected.label
                        selectedAddressField = null
                        addressSuggestions = emptyList()
                        addressError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    embeddedInForm = true,
                    minQueryLengthToShow = 2,
                )
            }

            waypointInputs.forEachIndexed { index, stop ->
                AuthFormDivider(sheetTheme = sheetTheme)
                WaypointStopRow(
                    index = index,
                    value = stop,
                    waypointCount = waypointInputs.size,
                    sheetTheme = sheetTheme,
                    showSuggestions = selectedAddressField == AddressField.Waypoint(index),
                    suggestions = addressSuggestions,
                    loading = addressLoading,
                    errorMessage = addressError,
                    query = activeQuery,
                    onValueChange = {
                        waypointInputs[index] = it
                        selectedAddressField = AddressField.Waypoint(index)
                        vehicleMenuExpanded = false
                    },
                    onFocus = { selectedAddressField = AddressField.Waypoint(index) },
                    onMoveTo = { target -> moveWaypoint(index, target) },
                    onRemove = {
                        waypointInputs.removeAt(index)
                        selectedAddressField = when (val selected = selectedAddressField) {
                            is AddressField.Waypoint -> when {
                                selected.index == index -> null
                                selected.index > index -> AddressField.Waypoint(selected.index - 1)
                                else -> selected
                            }
                            else -> selected
                        }
                    },
                    onSuggestionSelected = { selected ->
                        selectingSuggestion = true
                        if (index in waypointInputs.indices) {
                            waypointInputs[index] = selected.label
                        }
                        selectedAddressField = null
                        addressSuggestions = emptyList()
                        addressError = null
                    },
                )
            }

            AuthFormDivider(sheetTheme = sheetTheme)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        waypointInputs.add("")
                        selectedAddressField = AddressField.Waypoint(waypointInputs.lastIndex)
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.companion_waypoint_add),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                )
            }

            AuthFormDivider(sheetTheme = sheetTheme)
            AuthField(
                label = stringResource(R.string.companion_field_to),
                value = toInput,
                onValueChange = {
                    toInput = it
                    selectedAddressField = AddressField.To
                    vehicleMenuExpanded = false
                },
                placeholder = stringResource(R.string.companion_hint_to),
                sheetTheme = sheetTheme,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
            if (selectedAddressField == AddressField.To && activeQuery.length >= 2) {
                PeliasSearchResultsList(
                    sheetTheme = sheetTheme,
                    suggestions = addressSuggestions,
                    loading = addressLoading,
                    errorMessage = addressError,
                    query = activeQuery,
                    onResultSelected = { selected ->
                        selectingSuggestion = true
                        toInput = selected.label
                        selectedAddressField = null
                        addressSuggestions = emptyList()
                        addressError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    embeddedInForm = true,
                    minQueryLengthToShow = 2,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (routePreview.isBlank()) stringResource(R.string.companion_route_preview_empty) else routePreview,
            color = sheetTheme.secondaryText,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        AuthSectionLabel(
            text = stringResource(R.string.companion_section_schedule),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            CompanionPickupDateTimeField(
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                sheetTheme = sheetTheme,
                onClick = {
                    vehicleMenuExpanded = false
                    showPickupPicker = true
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        AuthSectionLabel(
            text = stringResource(R.string.companion_section_ride_details),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            AuthField(
                label = stringResource(R.string.companion_field_seats),
                value = seatsInput,
                onValueChange = { seatsInput = it.filter { ch -> ch.isDigit() } },
                placeholder = "1",
                sheetTheme = sheetTheme,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
            AuthFormDivider(sheetTheme = sheetTheme)
            AuthField(
                label = stringResource(R.string.companion_field_price),
                value = priceInput,
                onValueChange = { priceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                placeholder = "0",
                sheetTheme = sheetTheme,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
            AuthFormDivider(sheetTheme = sheetTheme)
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(
                    text = stringResource(R.string.companion_field_vehicle),
                    color = sheetTheme.secondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                CompanionVehicleTypePicker(
                    selectedVehicle = vehicleInput,
                    expanded = vehicleMenuExpanded,
                    onExpandedChange = { expanded ->
                        vehicleMenuExpanded = expanded
                        if (expanded) {
                            selectedAddressField = null
                            addressSuggestions = emptyList()
                        }
                    },
                    onVehicleSelected = { option ->
                        vehicleInput = option.name
                        seatsInput = option.defaultSeats.toString()
                    },
                    sheetTheme = sheetTheme,
                    embeddedInForm = true,
                )
            }
            AuthFormDivider(sheetTheme = sheetTheme)
            AuthField(
                label = stringResource(R.string.companion_field_preferences),
                value = preferencesInput,
                onValueChange = { preferencesInput = it },
                placeholder = stringResource(R.string.companion_hint_preferences),
                sheetTheme = sheetTheme,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        AuthErrorText(
            message = errorMessage,
            sheetTheme = sheetTheme,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        // if (canOfferRide == false) {
        //     Spacer(modifier = Modifier.height(8.dp))
        //     Text(
        //         text = premiumRequiredMessage,
        //         color = sheetTheme.accent,
        //         fontSize = 14.sp,
        //     )
        // }

        Spacer(modifier = Modifier.height(18.dp))
        AuthPrimaryButton(
            text = if (busy) stringResource(R.string.companion_offering) else stringResource(R.string.companion_offer_submit),
            onClick = {
                // if (canOfferRide != true) {
                //     errorMessage = premiumRequiredMessage
                //     return@AuthPrimaryButton
                // }
                val token = OfflineAuthStore.sessionToken(context)
                if (token.isNullOrBlank()) {
                    errorMessage = notSignedInMessage
                    return@AuthPrimaryButton
                }
                val seats = seatsInput.toIntOrNull()
                if (fromInput.trim().isEmpty() || toInput.trim().isEmpty()) {
                    errorMessage = requiredFieldsMessage
                    return@AuthPrimaryButton
                }
                if (departAtIso.isEmpty()) {
                    errorMessage = dateTimeMessage
                    return@AuthPrimaryButton
                }
                if (seats == null || seats < 1 || seats > 60) {
                    errorMessage = seatsMessage
                    return@AuthPrimaryButton
                }
                val price = priceInput.toDoubleOrNull() ?: 0.0
                errorMessage = null
                busy = true
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        CompanionClient.createDriverPost(
                            bearerToken = token,
                            from = fromInput,
                            to = toInput,
                            waypoints = normalizedWaypoints,
                            routeSummary = routePreview,
                            departAtIso = departAtIso,
                            seats = seats,
                            pricePerSeat = price,
                            vehicle = vehicleInput,
                            preferences = preferencesInput,
                        )
                    }
                    busy = false
                    when (result) {
                        is CompanionClient.CreateDriverPostResult.Success -> {
                            Toast.makeText(context, R.string.companion_offer_success, Toast.LENGTH_SHORT).show()
                            onRideOffered()
                        }
                        is CompanionClient.CreateDriverPostResult.Failure -> {
                            errorMessage = result.message
                        }
                    }
                }
            },
            sheetTheme = sheetTheme,
            enabled = !busy,
            // enabled = !busy && canOfferRide == true,
        )
    }
}

@Composable
private fun WaypointStopRow(
    index: Int,
    value: String,
    waypointCount: Int,
    sheetTheme: AppleMapsSheetTheme,
    showSuggestions: Boolean,
    suggestions: List<PeliasSearchResult>,
    loading: Boolean,
    errorMessage: String?,
    query: String,
    onValueChange: (String) -> Unit,
    onFocus: () -> Unit,
    onMoveTo: (Int) -> Unit,
    onRemove: () -> Unit,
    onSuggestionSelected: (PeliasSearchResult) -> Unit,
) {
    var dragAccum by remember(index, waypointCount) { mutableFloatStateOf(0f) }
    val waypointHint = stringResource(R.string.companion_waypoint_hint)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .pointerInput(index, waypointCount) {
                        detectDragGestures(
                            onDragStart = { dragAccum = 0f },
                            onDragEnd = {
                                val steps = (dragAccum / 56f).roundToInt()
                                if (steps != 0) {
                                    val target = (index + steps).coerceIn(0, waypointCount - 1)
                                    onMoveTo(target)
                                }
                                dragAccum = 0f
                            },
                            onDragCancel = { dragAccum = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccum += dragAmount.y
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.DragIndicator,
                    contentDescription = stringResource(R.string.companion_waypoint_drag),
                    tint = sheetTheme.tertiaryText,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.companion_waypoint_label, index + 1),
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WaypointActionText(
                    text = stringResource(R.string.companion_waypoint_up),
                    enabled = index > 0,
                    sheetTheme = sheetTheme,
                    onClick = { if (index > 0) onMoveTo(index - 1) },
                )
                WaypointActionText(
                    text = stringResource(R.string.companion_waypoint_down),
                    enabled = index < waypointCount - 1,
                    sheetTheme = sheetTheme,
                    onClick = { if (index < waypointCount - 1) onMoveTo(index + 1) },
                )
                WaypointActionText(
                    text = stringResource(R.string.companion_waypoint_remove),
                    enabled = true,
                    sheetTheme = sheetTheme,
                    onClick = onRemove,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        com.example.roadguideapp.map.SearchTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = waypointHint,
            sheetTheme = sheetTheme,
            leadingIcon = null,
            showClearWhenNonEmpty = false,
            modifier = Modifier.clickable(onClick = onFocus),
            onFocus = onFocus,
        )
        if (showSuggestions && query.trim().length >= 2) {
            Spacer(modifier = Modifier.height(4.dp))
            PeliasSearchResultsList(
                sheetTheme = sheetTheme,
                suggestions = suggestions,
                loading = loading,
                errorMessage = errorMessage,
                query = query,
                onResultSelected = onSuggestionSelected,
                embeddedInForm = true,
                minQueryLengthToShow = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WaypointActionText(
    text: String,
    enabled: Boolean,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = if (enabled) sheetTheme.accent else sheetTheme.tertiaryText,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}
