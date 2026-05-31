package com.example.hop.data.repository.dev

import com.example.hop.domain.model.PassengerSummary
import com.example.hop.domain.model.Trip
import com.example.hop.domain.model.TripModel
import com.example.hop.domain.model.TripStatus
import com.example.hop.domain.repository.PostTripRequest
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.ApiResponse

class DevTripRepository : TripRepository {

    private val sampleTrips = listOf(
        Trip(
            id = "dev-trip-1",
            driverId = "dev-driver-1",
            model = TripModel.A,
            originName = "Copenhagen Central",
            originLat = 55.6726,
            originLng = 12.5648,
            destName = "Aarhus H",
            destLat = 56.1504,
            destLng = 10.2048,
            distanceMetres = 187_000,
            departsAt = "2026-04-21T08:00:00Z",
            seatsTotal = 3,
            seatsBooked = 1,
            minThreshold = null,
            priceOerePerSeat = 15000,
            driverNetOere = 15000,
            status = TripStatus.ACTIVE,
            recurrenceDays = null,
        ),
        Trip(
            id = "dev-trip-2",
            driverId = DevAuthRepository.DEV_USER.id,
            model = TripModel.B,
            originName = "Odense Station",
            originLat = 55.4004,
            originLng = 10.3882,
            destName = "Aalborg Station",
            destLat = 57.0428,
            destLng = 9.9170,
            distanceMetres = 220_000,
            departsAt = "2026-04-22T09:30:00Z",
            seatsTotal = 4,
            seatsBooked = 2,
            minThreshold = 2,
            priceOerePerSeat = 18000,
            driverNetOere = 18000,
            status = TripStatus.CONFIRMED,
            recurrenceDays = listOf("MON", "WED", "FRI"),
        ),
        Trip(
            id = "dev-trip-3",
            driverId = "dev-driver-2",
            model = TripModel.A,
            originName = "Roskilde",
            originLat = 55.6419,
            originLng = 12.0877,
            destName = "Copenhagen Airport",
            destLat = 55.6181,
            destLng = 12.6561,
            distanceMetres = 35_000,
            departsAt = "2026-04-21T06:00:00Z",
            seatsTotal = 2,
            seatsBooked = 0,
            minThreshold = null,
            priceOerePerSeat = 5500,
            driverNetOere = 5500,
            status = TripStatus.ACTIVE,
            recurrenceDays = null,
        ),
    )

    private var tripCounter = sampleTrips.size

    override suspend fun searchTrips(
        origin: String,
        dest: String,
        date: String,
        seats: Int,
    ): ApiResponse<List<Trip>> = ApiResponse.Success(
        sampleTrips.filter { it.status == TripStatus.ACTIVE || it.status == TripStatus.CONFIRMED }
    )

    override suspend fun getTripById(id: String): ApiResponse<Trip> {
        val trip = sampleTrips.find { it.id == id } ?: sampleTrips.first()
        return ApiResponse.Success(trip)
    }

    override suspend fun getMyTripsAsPassenger(): ApiResponse<List<Trip>> = ApiResponse.Success(
        listOf(sampleTrips[0].copy(bookingId = "dev-booking-1"))
    )

    override suspend fun getMyTripsAsDriver(): ApiResponse<List<Trip>> = ApiResponse.Success(
        listOf(sampleTrips[1])
    )

    override suspend fun postTrip(request: PostTripRequest): ApiResponse<Trip> {
        val trip = Trip(
            id = "dev-trip-${++tripCounter}",
            driverId = DevAuthRepository.DEV_USER.id,
            model = if (request.model == "B") TripModel.B else TripModel.A,
            originName = request.originName,
            originLat = request.originLat,
            originLng = request.originLng,
            destName = request.destName,
            destLat = request.destLat,
            destLng = request.destLng,
            distanceMetres = 45_000, // dev stub — backend calculates from lat/lng
            departsAt = request.departsAt,
            seatsTotal = request.seatsTotal,
            seatsBooked = 0,
            minThreshold = request.minThreshold,
            priceOerePerSeat = 10000,
            driverNetOere = 10000,
            status = TripStatus.ACTIVE,
            recurrenceDays = request.recurrenceDays,
        )
        return ApiResponse.Success(trip)
    }

    override suspend fun completeTrip(tripId: String): ApiResponse<Unit> = ApiResponse.Success(Unit)

    override suspend fun cancelTrip(tripId: String): ApiResponse<Unit> = ApiResponse.Success(Unit)

    override suspend fun updateTrip(tripId: String, request: com.example.hop.domain.repository.UpdateTripRequest): ApiResponse<com.example.hop.domain.model.Trip> {
        val existing = sampleTrips.firstOrNull() ?: return ApiResponse.Error(404, "Trip not found")
        return ApiResponse.Success(existing)
    }

    override suspend fun getTripPassengers(tripId: String): ApiResponse<List<PassengerSummary>> =
        ApiResponse.Success(
            listOf(
                PassengerSummary("dev-booking-1", "dev-pax-1", "Emma Jensen", 4.8f, 1),
                PassengerSummary("dev-booking-2", "dev-pax-2", "Lars Eriksen", 4.5f, 2),
            )
        )
}
