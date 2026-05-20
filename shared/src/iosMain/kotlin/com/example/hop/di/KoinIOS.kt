package com.example.hop.di

import com.example.hop.presentation.auth.AuthViewModel
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.presentation.bookingsuccess.BookingSuccessViewModel
import com.example.hop.presentation.cancellationconfirmation.CancellationConfirmationViewModel
import com.example.hop.presentation.chat.ChatViewModel
import com.example.hop.presentation.chatlist.ChatListViewModel
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.home.HomeStatsViewModel
import com.example.hop.presentation.home.SavedPlacesViewModel
import com.example.hop.presentation.mytrips.MyTripsPassengerViewModel
import com.example.hop.presentation.search.SearchViewModel
import com.example.hop.presentation.notifications.NotificationsViewModel
import com.example.hop.presentation.profile.OtherProfileViewModel
import com.example.hop.presentation.profile.OwnProfileViewModel
import com.example.hop.presentation.settings.SettingsViewModel
import com.example.hop.presentation.settlement.SettlementViewModel
import com.example.hop.presentation.tax.TaxViewModel
import com.example.hop.presentation.trip.TripViewModel
import com.example.hop.presentation.tripdetail.TripDetailViewModel
import com.example.hop.presentation.tripdetailactive.TripDetailActiveViewModel
import com.example.hop.presentation.trips.SearchTripsViewModel
import com.example.hop.network.TokenStorage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun initKoin(supabaseUrl: String, supabaseAnonKey: String, mapsApiKey: String = "") = startKoin {
    modules(appModules(supabaseUrl = supabaseUrl, supabaseAnonKey = supabaseAnonKey, mapsApiKey = mapsApiKey))
}

fun getAuthViewModel(): AuthViewModel = KoinPlatform.getKoin().get()

fun getSearchTripsViewModel(): SearchTripsViewModel = KoinPlatform.getKoin().get()

fun getTripViewModel(): TripViewModel = KoinPlatform.getKoin().get()

fun getTripDetailViewModel(): TripDetailViewModel = KoinPlatform.getKoin().get()

fun getTripDetailActiveViewModel(): TripDetailActiveViewModel = KoinPlatform.getKoin().get()

fun getBookingViewModel(): BookingViewModel = KoinPlatform.getKoin().get()

fun getBookingSuccessViewModel(): BookingSuccessViewModel = KoinPlatform.getKoin().get()

fun getCancellationConfirmationViewModel(): CancellationConfirmationViewModel = KoinPlatform.getKoin().get()

fun getMyTripsPassengerViewModel(): MyTripsPassengerViewModel = KoinPlatform.getKoin().get()

fun getDriverViewModel(): DriverViewModel = KoinPlatform.getKoin().get()

fun getNotificationsViewModel(): NotificationsViewModel = KoinPlatform.getKoin().get()

fun getHomeStatsViewModel(): HomeStatsViewModel = KoinPlatform.getKoin().get()

fun getSettingsViewModel(): SettingsViewModel = KoinPlatform.getKoin().get()

fun getOwnProfileViewModel(): OwnProfileViewModel = KoinPlatform.getKoin().get()

fun getOtherProfileViewModel(): OtherProfileViewModel = KoinPlatform.getKoin().get()

fun getChatViewModel(): ChatViewModel = KoinPlatform.getKoin().get()

fun getChatListViewModel(): ChatListViewModel = KoinPlatform.getKoin().get()

fun getTaxViewModel(): TaxViewModel = KoinPlatform.getKoin().get()

fun getSavedPlacesViewModel(): SavedPlacesViewModel = KoinPlatform.getKoin().get()

fun getSearchViewModel(): SearchViewModel = KoinPlatform.getKoin().get()

fun getSettlementViewModel(): SettlementViewModel = KoinPlatform.getKoin().get()

/** Returns the active Supabase session access token. Used by ChatView to authenticate the WebSocket. */
fun getAccessToken(): String? = KoinPlatform.getKoin().get<SupabaseClient>().auth.currentSessionOrNull()?.accessToken