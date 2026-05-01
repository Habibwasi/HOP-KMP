package com.example.hop.di

import com.example.hop.data.repository.AggregatesRepositoryImpl
import com.example.hop.data.repository.BookingRepositoryImpl
import com.example.hop.data.repository.DriverRepositoryImpl
import com.example.hop.data.repository.HomeStatsRepositoryImpl
import com.example.hop.data.repository.PlacesRepositoryImpl
import com.example.hop.data.repository.SearchHistoryRepositoryImpl
import com.example.hop.data.repository.SupabaseAuthRepositoryImpl
import com.example.hop.data.repository.TaxRepositoryImpl
import com.example.hop.data.repository.TripRepositoryImpl
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.domain.repository.AggregatesRepository
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.domain.repository.HomeStatsRepository
import com.example.hop.domain.repository.PlacesRepository
import com.example.hop.domain.repository.SearchHistoryRepository
import com.example.hop.data.repository.PaymentRepositoryImpl
import com.example.hop.domain.repository.PaymentRepository
import com.example.hop.data.repository.UserRepositoryImpl
import com.example.hop.domain.repository.TaxRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> {
        SupabaseAuthRepositoryImpl(
            supabase = get<SupabaseClient>(),
            httpClient = get<HttpClient>(),
        )
    }
    single<TripRepository> {
        TripRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<BookingRepository> {
        BookingRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<PaymentRepository> {
        PaymentRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<DriverRepository> {
        DriverRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<TaxRepository> {
        TaxRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<UserRepository> {
        UserRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<PlacesRepository> {
        PlacesRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<HomeStatsRepository> {
        HomeStatsRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
    single<AggregatesRepository> {
        AggregatesRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
}
