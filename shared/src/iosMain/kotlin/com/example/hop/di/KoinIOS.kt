package com.example.hop.di

import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.presentation.trip.TripViewModel
import com.example.hop.presentation.trips.SearchTripsViewModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun initKoin() = startKoin {
    modules(appModules)
}

/** Typed accessor so Swift can resolve AuthViewModel from the Koin container
 *  without needing a generic [get] call (which is not expressible in Swift). */
fun getAuthViewModel(): AuthViewModel = KoinPlatform.getKoin().get()

/** Typed accessor for SearchTripsViewModel. */
fun getSearchTripsViewModel(): SearchTripsViewModel = KoinPlatform.getKoin().get()

/** Typed accessor for TripViewModel. */
fun getTripViewModel(): TripViewModel = KoinPlatform.getKoin().get()

/** Typed accessor for BookingViewModel. */
fun getBookingViewModel(): BookingViewModel = KoinPlatform.getKoin().get()
