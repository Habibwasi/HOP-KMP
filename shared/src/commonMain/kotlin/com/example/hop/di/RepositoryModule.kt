package com.example.hop.di

import com.example.hop.data.repository.AuthRepositoryImpl
import com.example.hop.data.repository.BookingRepositoryImpl
import com.example.hop.data.repository.DriverRepositoryImpl
import com.example.hop.data.repository.TripRepositoryImpl
import com.example.hop.domain.repository.AuthRepository
import com.example.hop.domain.repository.BookingRepository
import com.example.hop.domain.repository.DriverRepository
import com.example.hop.domain.repository.TripRepository
import com.example.hop.network.TokenStorage
import io.ktor.client.HttpClient
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get<HttpClient>(),
            tokenStorage = get<TokenStorage>(),
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
    single<DriverRepository> {
        DriverRepositoryImpl(
            httpClient = get<HttpClient>(),
        )
    }
}
