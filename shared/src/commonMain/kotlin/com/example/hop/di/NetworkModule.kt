package com.example.hop.di

import com.example.hop.network.HttpClientFactory
import com.example.hop.network.NetworkConstants
import com.example.hop.network.TokenStorage
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        HttpClientFactory.create(
            tokenStorage = get<TokenStorage>(),
            baseUrl = NetworkConstants.PRODUCTION_BASE_URL,
        )
    }
}
