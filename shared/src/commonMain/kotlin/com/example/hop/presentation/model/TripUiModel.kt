package com.example.hop.presentation.model

import com.example.hop.domain.model.Trip
import com.example.hop.domain.model.isBroken

/**
 * Trip wrapped with UI-layer concerns.
 * Used in ViewModels to separate domain state from presentation metadata.
 *
 * @param trip The domain Trip object.
 * @param isBroken True if trip has UNKNOWN status or model (version mismatch or corruption).
 *                 UI renders as disabled/greyed-out. Not filtered out of lists.
 */
data class TripUiModel(
    val trip: Trip,
    val isBroken: Boolean = trip.isBroken(),
) {
    // Delegate key properties for convenience in UI layer
    val id: String get() = trip.id
    val status get() = trip.status
    val model get() = trip.model
    val originName get() = trip.originName
    val destName get() = trip.destName
    val departsAt get() = trip.departsAt
    val seatsTotal get() = trip.seatsTotal
    val seatsBooked get() = trip.seatsBooked
    val priceOerePerSeat get() = trip.priceOerePerSeat
    // Non-null only on passenger-scoped trip lists. Use for PA-08 TripDetailActive navigation.
    val bookingId: String? get() = trip.bookingId
}

/**
 * Safely wraps a trip, flagging UNKNOWN states.
 * Call after fetching from repository.
 */
fun Trip.toUiModel(): TripUiModel = TripUiModel(this)

/**
 * Batch wrapping for list results.
 */
fun List<Trip>.toUiModels(): List<TripUiModel> = map { it.toUiModel() }
