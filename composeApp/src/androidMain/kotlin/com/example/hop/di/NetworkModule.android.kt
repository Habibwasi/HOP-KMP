package com.example.hop.di

import android.content.Context
import com.example.hop.network.TokenStorage
import com.example.hop.network.TokenStorageImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidNetworkModule = module {
    single<TokenStorage> { TokenStorageImpl(androidContext()) }
}
