package com.example.hop.di

import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.presentation.search.SearchViewModel
import com.example.hop.presentation.trip.TripViewModel
import com.example.hop.presentation.trips.SearchTripsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::SearchTripsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::TripViewModel)
}
