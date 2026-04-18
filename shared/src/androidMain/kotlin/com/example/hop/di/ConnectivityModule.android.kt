package com.example.hop.di

import com.example.hop.network.ConnectivityObserver
import com.example.hop.network.ConnectivityObserverImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val connectivityModule: Module = module {
    single<ConnectivityObserver> { ConnectivityObserverImpl(androidContext()) }
}
