package com.example.hop.di

import com.example.hop.chat.ChatRepository
import com.example.hop.data.repository.dev.DevAggregatesRepository
import com.example.hop.data.repository.dev.DevAuthRepository
import com.example.hop.data.repository.dev.DevBookingRepository
import com.example.hop.data.repository.dev.DevChatRepository
import com.example.hop.data.repository.dev.DevDriverRepository
import com.example.hop.data.repository.dev.DevHomeStatsRepository
import com.example.hop.data.repository.dev.DevSettlementRepository
import com.example.hop.data.repository.dev.DevPlacesRepository
import com.example.hop.data.repository.dev.DevRoutingRepository
import com.example.hop.data.repository.dev.DevSearchAlertsRepository
import com.example.hop.data.repository.dev.DevSearchHistoryRepository
import com.example.hop.data.repository.dev.DevTaxRepository
import com.example.hop.data.repository.dev.DevTripRepository
import com.example.hop.data.repository.dev.DevUserRepository
import com.example.hop.domain.repository.AggregatesRepository
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.domain.repository.HomeStatsRepository
import com.example.hop.domain.repository.SettlementRepository
import com.example.hop.domain.repository.PlacesRepository
import com.example.hop.domain.repository.RoutingRepository
import com.example.hop.domain.repository.SearchAlertsRepository
import com.example.hop.domain.repository.SearchHistoryRepository
import com.example.hop.domain.repository.TaxRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.domain.repository.UserRepository
import org.koin.dsl.module

/**
 * Koin module that replaces all network-dependent repositories with in-memory
 * fake implementations. Load this **instead of** [repositoryModule] + [chatRepositoryModule]
 * when [com.example.hop.BuildConfig.DEV_MODE] is true.
 *
 * The dev user is auto-authenticated with both DRIVER and PASSENGER roles.
 */
val devRepositoryModule = module {
    single<AuthRepository> { DevAuthRepository() }
    single<TripRepository> { DevTripRepository() }
    single<BookingRepository> { DevBookingRepository() }
    single<DriverRepository> { DevDriverRepository() }
    single<TaxRepository> { DevTaxRepository() }
    single<UserRepository> { DevUserRepository() }
    single<ChatRepository> { DevChatRepository() }
    single<SettlementRepository> { DevSettlementRepository() }
    single<SearchHistoryRepository> { DevSearchHistoryRepository() }
    single<SearchAlertsRepository> { DevSearchAlertsRepository() }
    single<PlacesRepository> { DevPlacesRepository() }
    single<HomeStatsRepository> { DevHomeStatsRepository() }
    single<AggregatesRepository> { DevAggregatesRepository() }
    single<RoutingRepository> { DevRoutingRepository() }
}
