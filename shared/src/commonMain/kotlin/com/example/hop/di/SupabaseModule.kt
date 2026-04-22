package com.example.hop.di

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import org.koin.dsl.module

fun supabaseModule(supabaseUrl: String, supabaseAnonKey: String) = module {
    single {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey,
        ) {
            install(Auth)
        }
    }
}
