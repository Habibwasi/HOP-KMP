package com.example.hop.di

import com.example.hop.network.HttpClientFactory
import com.example.hop.network.NetworkConstants
import com.example.hop.network.SessionExpiryNotifier
import com.example.hop.network.TokenRefreshManager
import com.example.hop.network.TokenStorage
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single { SessionExpiryNotifier() }

    single {
        TokenRefreshManager(
            tokenStorage = get<TokenStorage>(),
            sessionExpiryNotifier = get(),
            baseUrl = NetworkConstants.PRODUCTION_BASE_URL,
        )
    } onClose { it?.close() }

    single<HttpClient> {
        HttpClientFactory.create(
            tokenStorage = get<TokenStorage>(),
            tokenRefreshManager = get(),
            baseUrl = NetworkConstants.PRODUCTION_BASE_URL,
        )
    } onClose { it?.close() }
}
