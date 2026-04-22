package com.example.hop.di

import com.example.hop.network.HttpClientFactory
import com.example.hop.network.SessionExpiryNotifier
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

fun networkModule(baseUrl: String) = module {
    single { SessionExpiryNotifier() }
    single<HttpClient> {
        HttpClientFactory.create(
            supabase = get<SupabaseClient>(),
            baseUrl = baseUrl,
            sessionExpiryNotifier = get(),
        )
    }
}