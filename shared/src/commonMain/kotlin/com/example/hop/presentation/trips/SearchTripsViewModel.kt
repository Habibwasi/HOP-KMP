package com.example.hop.presentation.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hop.domain.model.Trip
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse
import com.example.hop.presentation.model.TripUiModel
import com.example.hop.presentation.model.toUiModels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ─ OPTION 4 UNKNOWN ENUM HANDLING PATTERN ─
 *
 * Pattern for ViewModels that fetch and display trips.
 *
 * • Broken trips (UNKNOWN status/model) are KEPT in lists, not filtered.
 * • Each trip is wrapped in TripUiModel with isBroken flag.
 * • ViewModels soft-log broken trips for observability.
 * • UI renders broken trips as disabled/greyed-out.
 * • Pull-to-refresh can self-heal if backend was temporarily buggy.
 *
 * Copy and adapt this template for any trip-fetching ViewModel.
 */

// ─ State ──────────────────────────────────────────────────────────────────────

data class SearchTripsUiState(
    val trips: List<TripUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface SearchTripsEvent {
    data class Search(
        val origin: String,
        val dest: String,
        val date: String,
        val seats: Int,
    ) : SearchTripsEvent

    object ClearError : SearchTripsEvent
}

sealed interface SearchTripsEffect {
    data class ShowSnackbar(val message: String) : SearchTripsEffect
}

// ─ ViewModel ──────────────────────────────────────────────────────────────────

/**
 * Demonstrates Option 4: hybrid approach for UNKNOWN trip states.
 *
 * • Wraps fetched trips in TripUiModel immediately after repository call.
 * • Soft-logs any broken trips (optional: track in Sentry/Crashlytics).
 * • State contains TripUiModel objects, not raw Trip objects.
 * • UI checks isBroken flag to disable interactions.
 */
class SearchTripsViewModel(
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchTripsUiState())
    val state: StateFlow<SearchTripsUiState> = _state.asStateFlow()

    // Effects channel not shown here (see AuthViewModel.kt for pattern)

    fun onEvent(event: SearchTripsEvent) {
        when (event) {
            is SearchTripsEvent.Search -> searchTrips(
                origin = event.origin,
                dest = event.dest,
                date = event.date,
                seats = event.seats,
            )

            SearchTripsEvent.ClearError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun searchTrips(
        origin: String,
        dest: String,
        date: String,
        seats: Int,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = tripRepository.searchTrips(
                origin = origin,
                dest = dest,
                date = date,
                seats = seats,
            )

            when (result) {
                is ApiResponse.Success -> {
                    // STEP 1: Wrap trips in TripUiModel (adds isBroken flag)
                    val uiModels = result.data.toUiModels()

                    // STEP 2: Soft-log broken trips for observability
                    uiModels.forEach { tripUi ->
                        if (tripUi.isBroken) {
                            logBrokenTrip(tripUi.trip)
                        }
                    }

                    // STEP 3: Update state with wrapped trips
                    _state.value = _state.value.copy(
                        trips = uiModels,
                        isLoading = false,
                        error = null,
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Soft-log broken trips. Optional: send to Sentry/Crashlytics.
     * This helps detect version mismatches or API drift early.
     */
    private fun logBrokenTrip(trip: Trip) {
        // EXAMPLE: Send to Sentry
        // Sentry.captureMessage(
        //     "Unknown trip state detected",
        //     SentryLevel.INFO,
        //     mapOf(
        //         "tripId" to trip.id,
        //         "status" to trip.status.name,
        //         "model" to trip.model.name,
        //     )
        // )

        // For now, just print to logcat (dev environment)
        println("⚠️  Unknown trip state: tripId=${trip.id}, status=${trip.status}, model=${trip.model}")
    }
}

/**
 * ─ Usage in Route/Screen ──────────────────────────────────────────────────────
 *
 * In your Route composable:
 *
 *   @Composable
 *   fun SearchTripsRoute(
 *       onTapTrip: (String) -> Unit,
 *       viewModel: SearchTripsViewModel = koinViewModel(),
 *   ) {
 *       val state by viewModel.state.collectAsStateWithLifecycle()
 *
 *       SearchTripsScreen(
 *           state = state,
 *           onEvent = viewModel::onEvent,
 *           onTapTrip = onTapTrip,
 *       )
 *   }
 *
 * In your Screen composable:
 *
 *   @Composable
 *   fun SearchTripsScreen(
 *       state: SearchTripsUiState,
 *       onEvent: (SearchTripsEvent) -> Unit,
 *       onTapTrip: (String) -> Unit,
 *   ) {
 *       LazyColumn {
 *           items(state.trips, key = { it.id }) { tripUi ->
 *               TripListItem(
 *                   tripUi = tripUi,
 *                   onTap = { onTapTrip(tripUi.id) },
 *                   // UI layer handles isBroken flag:
 *                   enabled = !tripUi.isBroken,
 *                   modifier = Modifier.alpha(if (tripUi.isBroken) 0.6f else 1f),
 *               )
 *           }
 *       }
 *   }
 */
