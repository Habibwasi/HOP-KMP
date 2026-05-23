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
            install(Auth) {
                // Tell supabase-kt which custom-scheme URI to recognise as a
                // callback so PKCE code exchange works correctly on Android/iOS.
                scheme = "ridly"
                host   = "auth"
            }
        }
    }
}
