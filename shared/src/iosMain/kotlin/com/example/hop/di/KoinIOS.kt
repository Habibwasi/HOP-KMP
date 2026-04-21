package com.example.hop.di

import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.presentation.trip.TripViewModel
import com.example.hop.presentation.trips.SearchTripsViewModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun initKoin() = startKoin {
    modules(appModules())
}

fun getAuthViewModel(): AuthViewModel = KoinPlatform.getKoin().get()

fun getSearchTripsViewModel(): SearchTripsViewModel = KoinPlatform.getKoin().get()

fun getTripViewModel(): TripViewModel = KoinPlatform.getKoin().get()

fun getBookingViewModel(): BookingViewModel = KoinPlatform.getKoin().get()