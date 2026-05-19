package com.example.hop.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.Trip
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.presentation.search.SearchEffect
import com.example.hop.presentation.search.SearchEvent
import com.example.hop.presentation.search.SearchFilter
import com.example.hop.presentation.search.SearchUiState
import com.example.hop.presentation.search.SearchViewModel
import com.example.hop.ui.components.BadgeType
import com.example.hop.ui.components.EmptyState
import com.example.hop.ui.components.TripCard
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-02 — Search Results Route.
 *
 * Wires [SearchViewModel] from Koin, collects [SearchUiState] and
 * [SearchEffect] on entry, and delegates all rendering to the stateless
 * [SearchResultsScreen].
 */
@Composable
fun SearchResultsRoute(
    origin: String,
    dest: String,
    date: String,
    seats: Int,
    onNavigateBack: () -> Unit,
    onNavigateToTripDetail: (tripId: String) -> Unit,
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = koinViewModel(),
) {
    val state by searchViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Fire search when the screen mounts with non-empty params.
    LaunchedEffect(origin, dest, date, seats) {
        if (origin.isNotBlank() && dest.isNotBlank()) {
            searchViewModel.onEvent(SearchEvent.Search(origin, dest, date, seats))
        }
    }

    LaunchedEffect(searchViewModel) {
        searchViewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchEffect.NavigateToTripDetail -> onNavigateToTripDetail(effect.tripId)
                is SearchEffect.AlertCreated -> scope.launch {
                    snackbarHostState.showSnackbar("Alert set! We'll notify you when a ride appears.")
                }
                is SearchEffect.AlertError -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        SearchResultsScreen(
            state = state,
            onBack = onNavigateBack,
            onFilterChipClick = { filter ->
                searchViewModel.onEvent(SearchEvent.ApplyFilter(filter))
            },
            onTripCardClick = { tripId ->
                searchViewModel.onEvent(SearchEvent.SelectTrip(tripId))
            },
            onAlertMe = {
                searchViewModel.onEvent(SearchEvent.AlertMe)
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * PA-02 — Search Results Screen.
 *
 * Stateless renderer. Renders:
 *  - Top bar with back arrow, "Origin → Dest" summary, and date chip
 *  - Horizontal-scroll filter bar (Earliest | Cheapest | Top Rated | Model A | Model B)
 *  - Result count or empty state depending on [SearchUiState.results]
 *  - [TripCard] list or loading indicator
 */
@Composable
fun SearchResultsScreen(
    state: SearchUiState,
    onBack: () -> Unit,
    onFilterChipClick: (SearchFilter) -> Unit,
    onTripCardClick: (tripId: String) -> Unit,
    onAlertMe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        SearchResultsTopBar(
            origin = state.origin,
            dest = state.dest,
            date = state.date,
            onBack = onBack,
        )

        FilterBar(
            activeFilters = state.activeFilters,
            onFilterClick = onFilterChipClick,
        )

        when {
            state.isLoading -> SearchResultsLoading(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            state.error != null -> EmptyState(
                headline = "Couldn't load trips",
                subtext = state.error,
                ctaLabel = "Alert me when one appears",
                onCtaClick = onAlertMe,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            state.results.isEmpty() -> EmptyState(
                headline = "No rides on this route yet",
                ctaLabel = "Alert me when one appears",
                onCtaClick = onAlertMe,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            else -> TripResultsList(
                results = state.results,
                onTripCardClick = onTripCardClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun SearchResultsTopBar(
    origin: String,
    dest: String,
    date: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.semantics { contentDescription = "Back" },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = HopColors.authTextPrimary,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            val routeSummary = when {
                origin.isNotEmpty() && dest.isNotEmpty() -> "$origin → $dest"
                else -> "Search Results"
            }
            Text(
                text = routeSummary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = HopColors.authTextPrimary,
                maxLines = 1,
            )
        }

        if (date.isNotEmpty()) {
            DateChip(date = date)
        }
    }
}

@Composable
private fun DateChip(date: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, HopColors.textSecondary, RoundedCornerShape(16.dp))
            .padding(horizontal = HopSpacing.sm, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = HopColors.authTextSecondary,
        )
    }
}

// ── Filter bar ────────────────────────────────────────────────────────────────

// TOP_RATED is intentionally excluded: the filter is a no-op until Trip.rating is added
// to the domain model. Re-add once the backend field is available.
private val ALL_FILTER_CHIPS = listOf(
    SearchFilter.EARLIEST      to "Earliest",
    SearchFilter.CHEAPEST      to "Cheapest",
    SearchFilter.DAILY_COMMUTE to "Model A",
    SearchFilter.LONG_DISTANCE to "Model B",
)

@Composable
private fun FilterBar(
    activeFilters: Set<SearchFilter>,
    onFilterClick: (SearchFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        ALL_FILTER_CHIPS.forEach { (filter, label) ->
            HopFilterChip(
                label = label,
                isActive = filter in activeFilters,
                onClick = { onFilterClick(filter) },
            )
        }
    }
}

@Composable
private fun HopFilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isActive) HopColors.primaryLime else Color.Transparent
    val borderColor     = if (isActive) HopColors.primaryLime else HopColors.authTextSecondary
    val textColor       = if (isActive) Color(0xFF1A1A1A) else HopColors.authTextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = textColor,
        )
    }
}

// ── Content states ────────────────────────────────────────────────────────────

@Composable
private fun SearchResultsLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = HopColors.primaryLime,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun TripResultsList(
    results: List<TripUiModel>,
    onTripCardClick: (tripId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = HopSpacing.md,
            end = HopSpacing.md,
            bottom = HopSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        // Result count label
        item {
            Text(
                text = "${results.size} rides available",
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.authTextSecondary,
                modifier = Modifier.padding(vertical = HopSpacing.xs),
            )
        }

        items(results, key = { it.id }) { tripUiModel ->
            TripCard(
                driverName = "Driver",
                driverInitials = "D",
                driverRating = 5.0f,
                originName = tripUiModel.originName,
                destinationName = tripUiModel.destName,
                departureTime = tripUiModel.formattedDepartsAt,
                tripModel = when (tripUiModel.model) {
                    TripModel.A -> BadgeType.ModelA
                    TripModel.B -> BadgeType.ModelB
                    TripModel.UNKNOWN -> BadgeType.Custom(
                        label = "UNKNOWN",
                        background = HopColors.surfaceElevated,
                        contentColor = HopColors.authTextSecondary,
                    )
                },
                pricePerSeatOere = tripUiModel.priceOerePerSeat,
                // Broken cards are dimmed and non-tappable per the UNKNOWN contract.
                onClick = if (tripUiModel.isBroken) ({}) else ({ onTripCardClick(tripUiModel.id) }),
                modifier = if (tripUiModel.isBroken) Modifier.alpha(0.6f) else Modifier,
            )
        }

        item { Spacer(modifier = Modifier.height(HopSpacing.md)) }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private fun previewTrip(
    id: String,
    origin: String = "Copenhagen H",
    dest: String = "Aarhus C",
    model: TripModel = TripModel.A,
    priceOere: Int = 20400,
    time: String = "08:30",
): TripUiModel = TripUiModel(
    trip = Trip(
        id = id,
        driverId = "driver-preview",
        model = model,
        originName = origin,
        originLat = 55.672,
        originLng = 12.564,
        destName = dest,
        destLat = 56.157,
        destLng = 10.212,
        distanceMetres = 190_000,
        departsAt = time,
        seatsTotal = 3,
        seatsBooked = 1,
        minThreshold = null,
        priceOerePerSeat = priceOere,
        driverNetOere = 18_000,
        status = TripStatus.ACTIVE,
        recurrenceDays = null,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SearchResultsScreenWithResultsPreview() {
    HopTheme {
        SearchResultsScreen(
            state = SearchUiState(
                origin = "Copenhagen H",
                dest = "Aarhus C",
                date = "Today",
                results = listOf(
                    previewTrip(id = "1", model = TripModel.A, priceOere = 20400, time = "08:30"),
                    previewTrip(id = "2", model = TripModel.B, priceOere = 35000, time = "09:15"),
                    previewTrip(id = "3", model = TripModel.A, priceOere = 18000, time = "10:00"),
                ),
                activeFilters = setOf(SearchFilter.CHEAPEST),
            ),
            onBack = {},
            onFilterChipClick = {},
            onTripCardClick = {},
            onAlertMe = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SearchResultsScreenEmptyPreview() {
    HopTheme {
        SearchResultsScreen(
            state = SearchUiState(
                origin = "Odense",
                dest = "Vejle",
                date = "Tomorrow",
                results = emptyList(),
            ),
            onBack = {},
            onFilterChipClick = {},
            onTripCardClick = {},
            onAlertMe = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SearchResultsScreenLoadingPreview() {
    HopTheme {
        SearchResultsScreen(
            state = SearchUiState(
                origin = "Copenhagen H",
                dest = "Aarhus C",
                date = "Today",
                isLoading = true,
            ),
            onBack = {},
            onFilterChipClick = {},
            onTripCardClick = {},
            onAlertMe = {},
        )
    }
}
