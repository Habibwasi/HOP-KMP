package com.example.hop.di

import com.example.hop.network.HttpClientFactory
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

fun networkModule(baseUrl: String) = module {
    single<HttpClient> {
        HttpClientFactory.create(
            supabase = get<SupabaseClient>(),
            baseUrl = baseUrl,
        )
    }
}