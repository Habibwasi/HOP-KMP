package com.example.hop.ui.screens.driver

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TripModel
import com.example.hop.presentation.edittrip.EditTripDraft
import com.example.hop.presentation.edittrip.EditTripEffect
import com.example.hop.presentation.edittrip.EditTripEvent
import com.example.hop.presentation.edittrip.EditTripUiState
import com.example.hop.presentation.edittrip.EditTripViewModel
import com.example.hop.pricing.PricingEngine
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.screens.passenger.LocationPickerOverlay
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ── Route ─────────────────────────────────────────────────────────────────────

@Composable
fun EditTripRoute(
    tripId: String,
    onNavigateToPriceReview: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditTripViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onEvent(EditTripEvent.LoadTrip(tripId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EditTripEffect.NavigateToPriceReview -> onNavigateToPriceReview()
                is EditTripEffect.NavigateBack -> onNavigateBack()
                is EditTripEffect.NavigateToMyTrips -> Unit
                is EditTripEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        EditTripScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Edit Price Review Route ───────────────────────────────────────────────────

@Composable
fun EditTripPriceReviewRoute(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditTripViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EditTripEffect.NavigateToMyTrips -> onSaved()
                is EditTripEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        EditTripPriceReviewScreen(
            state = state,
            onConfirmAndUpdate = { viewModel.onEvent(EditTripEvent.ConfirmAndUpdate) },
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripScreen(
    state: EditTripUiState,
    onEvent: (EditTripEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trip = state.trip

    if (state.isLoading || trip == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(HopColors.background)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = HopColors.authAccent, modifier = Modifier.size(36.dp))
            } else {
                Text("Trip not found.", color = HopColors.authTextSecondary)
            }
        }
        return
    }

    val isModelA = trip.model == TripModel.A
    val isModelB = trip.model == TripModel.B

    // ── Local form state, pre-populated from loaded trip ─────────────────────
    var originName by remember(trip.id) { mutableStateOf(trip.originName) }
    var destName by remember(trip.id) { mutableStateOf(trip.destName) }

    // Parse departsAt ("2026-05-10T08:00:00Z") into date + time components
    val initialTime = remember(trip.id) {
        val iso = trip.departsAt
        val tIdx = iso.indexOf('T')
        if (tIdx > 0 && iso.length > tIdx + 5) iso.substring(tIdx + 1, tIdx + 6) else "08:00"
    }
    val initialDate = remember(trip.id) {
        val iso = trip.departsAt
        val tIdx = iso.indexOf('T')
        if (tIdx > 0) iso.substring(0, tIdx) else ""
    }
    var departureTime by remember(trip.id) { mutableStateOf(initialTime) }
    var selectedDate by remember(trip.id) { mutableStateOf(initialDate) }

    var locationPickerField by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Validation
    var originError by remember { mutableStateOf<String?>(null) }
    var destError by remember { mutableStateOf<String?>(null) }

    // Auto-trigger route calculation when both addresses are set
    LaunchedEffect(originName, destName) {
        if (originName.isNotBlank() && destName.isNotBlank()) {
            onEvent(EditTripEvent.CalculateRoute(originName, destName))
        }
    }

    fun buildDepartsAt(): String {
        return if (isModelB) {
            val date = selectedDate.ifBlank { initialDate }
            val time = departureTime
            "${date}T${time}:00.000Z"
        } else {
            // Model A: only time changes; preserve original date component
            "${initialDate}T${departureTime}:00.000Z"
        }
    }

    fun validate(): Boolean {
        originError = if (originName.isBlank()) "Enter a departure location" else null
        destError = if (destName.isBlank()) "Enter a destination" else null
        return originError == null && destError == null && state.routeDistanceMetres > 0
    }

    // ── Time picker dialog ────────────────────────────────────────────────────
    if (showTimePicker) {
        val hourMinute = departureTime.split(":")
        val initialHour = hourMinute.getOrNull(0)?.toIntOrNull() ?: 8
        val initialMinute = hourMinute.getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerState.hour.toString().padStart(2, '0')
                    val m = timePickerState.minute.toString().padStart(2, '0')
                    departureTime = "$h:$m"
                    showTimePicker = false
                }) { Text("OK", color = HopColors.authAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = HopColors.authTextSecondary)
                }
            },
            containerColor = HopColors.cardSurface,
            titleContentColor = HopColors.authTextPrimary,
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = HopColors.authInputSurface,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = HopColors.authTextPrimary,
                        selectorColor = HopColors.authAccent,
                        timeSelectorSelectedContainerColor = HopColors.authAccent,
                        timeSelectorUnselectedContainerColor = HopColors.authInputSurface,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = HopColors.authTextPrimary,
                    ),
                )
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.xs, vertical = HopSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = HopColors.authTextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = "Edit Trip",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = HopColors.authTextPrimary,
            )
        }

        // ── Scrollable form body ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.lg),
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // Route section
            FormSection(title = "Route") {
                AddressPickerRow(
                    label = "From",
                    value = originName,
                    placeholder = "e.g. Aarhus C",
                    errorMessage = originError,
                    onClick = { locationPickerField = "from" },
                )
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                AddressPickerRow(
                    label = "To",
                    value = destName,
                    placeholder = "e.g. Copenhagen Central",
                    errorMessage = destError,
                    onClick = { locationPickerField = "to" },
                )
                if (originName.isNotBlank() && destName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                    RouteSummaryRow(
                        isCalculating = state.isCalculatingRoute,
                        distanceMetres = state.routeDistanceMetres,
                        seatsTotal = trip.seatsTotal,
                    )
                }
            }

            // Departure section
            FormSection(title = "Departure") {
                // Time row
                AddressPickerRow(
                    label = "Time",
                    value = departureTime,
                    placeholder = "HH:mm",
                    errorMessage = null,
                    onClick = { showTimePicker = true },
                )

                // Date row (Model B only)
                if (isModelB) {
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                    EditDatePickerRow(
                        label = "Date",
                        value = selectedDate,
                        onDateSelected = { selectedDate = it },
                    )
                }
            }
        }

        // ── Sticky CTA ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HopColors.background)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md)
                .navigationBarsPadding(),
        ) {
            HorizontalDivider(color = HopColors.authTextSecondary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(HopSpacing.md))
            HopButton(
                text = "Review Changes",
                onClick = {
                    if (validate()) {
                        onEvent(
                            EditTripEvent.SubmitDraft(
                                EditTripDraft(
                                    tripId = trip.id,
                                    model = trip.model,
                                    originName = originName.trim(),
                                    destName = destName.trim(),
                                    departsAt = buildDepartsAt(),
                                    departureTime = departureTime,
                                    date = selectedDate,
                                    seatsTotal = trip.seatsTotal,
                                    distanceMetres = state.routeDistanceMetres.takeIf { it > 0 }
                                        ?: trip.trip.distanceMetres,
                                )
                            )
                        )
                    }
                },
                enabled = !state.isCalculatingRoute,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // ── Location picker overlay ───────────────────────────────────────────────
    locationPickerField?.let { field ->
        Popup(
            properties = PopupProperties(focusable = true),
            onDismissRequest = { locationPickerField = null },
        ) {
            BackHandler { locationPickerField = null }
            LocationPickerOverlay(
                title = if (field == "from") "Where from?" else "Where to?",
                initialText = if (field == "from") originName else destName,
                savedPlaces = emptyList(),
                recentSearches = emptyList(),
                onDismiss = { locationPickerField = null },
                onConfirm = { address ->
                    if (field == "from") {
                        originName = address
                        originError = null
                    } else {
                        destName = address
                        destError = null
                    }
                    locationPickerField = null
                },
                onRouteConfirm = { origin, dest ->
                    originName = origin
                    destName = dest
                    originError = null
                    destError = null
                    locationPickerField = null
                },
                onRequestAddPlace = { locationPickerField = null },
            )
        }
    }
}

// ── Edit Trip Price Review Screen ─────────────────────────────────────────────

@Composable
fun EditTripPriceReviewScreen(
    state: EditTripUiState,
    onConfirmAndUpdate: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = state.pendingDraft
    val distanceMetres = draft?.distanceMetres ?: 0
    val seatsTotal = draft?.seatsTotal ?: 1

    val priceResult = remember(distanceMetres, seatsTotal) {
        if (distanceMetres > 0) PricingEngine.calculate(distanceMetres, seatsTotal) else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.xs, vertical = HopSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = HopColors.authTextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = "Review Changes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = HopColors.authTextPrimary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // Trip summary
            if (draft != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HopColors.authInputSurface)
                        .border(1.dp, HopColors.authTextSecondary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(HopSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                ) {
                    Text(
                        "Updated Route",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HopColors.authTextSecondary,
                        letterSpacing = 0.5.sp,
                    )
                    HorizontalDivider(color = HopColors.authTextSecondary.copy(alpha = 0.1f))
                    SummaryRow(label = "From", value = draft.originName.ifBlank { "—" })
                    SummaryRow(label = "To", value = draft.destName.ifBlank { "—" })
                    SummaryRow(label = "Departure", value = draft.departureTime.ifBlank { "—" })
                    if (draft.model == TripModel.B) {
                        SummaryRow(label = "Date", value = draft.date.ifBlank { "—" })
                    }
                }
            }

            // Price breakdown
            PriceBreakdownCard(
                distanceMetres = distanceMetres,
                seatsTotal = seatsTotal,
                priceResult = priceResult,
            )

            SystemPriceWarning()
            Spacer(modifier = Modifier.height(HopSpacing.sm))
        }

        // CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HopColors.background)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md)
                .navigationBarsPadding(),
        ) {
            HorizontalDivider(color = HopColors.authTextSecondary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(HopSpacing.md))
            HopButton(
                text = "Save Changes",
                onClick = onConfirmAndUpdate,
                isLoading = state.isSaving,
                enabled = priceResult != null && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Date picker row for Model B ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDatePickerRow(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    AddressPickerRow(
        label = label,
        value = value.ifBlank { "Pick a date" },
        placeholder = "YYYY-MM-DD",
        errorMessage = null,
        onClick = { showPicker = true },
    )

    if (showPicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState()
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(localDate.toString())
                    }
                    showPicker = false
                }) { Text("OK", color = HopColors.authAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = HopColors.authTextSecondary)
                }
            },
            containerColor = HopColors.cardSurface,
            titleContentColor = HopColors.authTextPrimary,
            text = {
                @OptIn(ExperimentalMaterial3Api::class)
                androidx.compose.material3.DatePicker(state = datePickerState)
            },
        )
    }
}
