package com.example.hop.di

import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.presentation.profile.OtherProfileViewModel
import com.example.hop.presentation.profile.OwnProfileViewModel
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.bookingsuccess.BookingSuccessViewModel
import com.example.hop.presentation.cancellationconfirmation.CancellationConfirmationViewModel
import com.example.hop.presentation.mytrips.MyTripsPassengerViewModel
import com.example.hop.presentation.notifications.NotificationsViewModel
import com.example.hop.presentation.search.SearchViewModel
import com.example.hop.presentation.chat.ChatViewModel
import com.example.hop.presentation.tax.TaxViewModel
import com.example.hop.presentation.trip.TripViewModel
import com.example.hop.presentation.tripdetail.TripDetailViewModel
import com.example.hop.presentation.tripdetailactive.TripDetailActiveViewModel
import com.example.hop.presentation.trips.SearchTripsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::DriverViewModel)
    viewModelOf(::BookingViewModel)
    viewModelOf(::BookingSuccessViewModel)
    viewModelOf(::CancellationConfirmationViewModel)
    viewModelOf(::MyTripsPassengerViewModel)
    viewModelOf(::SearchTripsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::TripViewModel)
    viewModelOf(::TripDetailViewModel)
    viewModelOf(::TripDetailActiveViewModel)
    viewModelOf(::TaxViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::OwnProfileViewModel)
    viewModelOf(::OtherProfileViewModel)
    viewModelOf(::NotificationsViewModel)
}
