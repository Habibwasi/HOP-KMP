package com.example.hop.di

import com.example.hop.data.local.TokenStorageImpl
import com.example.hop.network.TokenStorage
import io.github.jan.supabase.SupabaseClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val tokenStorageModule: Module = module {
    single<TokenStorage> { TokenStorageImpl(androidContext(), get<SupabaseClient>()) }
}
