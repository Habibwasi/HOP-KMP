package com.example.hop.di

import org.koin.core.context.startKoin

fun initKoin() = startKoin {
    modules(appModules)
}
