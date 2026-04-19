package com.example.hop.di

import com.example.hop.presentation.auth.AuthViewModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun initKoin() = startKoin {
    modules(appModules)
}

/** Typed accessor so Swift can resolve AuthViewModel from the Koin container
 *  without needing a generic [get] call (which is not expressible in Swift). */
fun getAuthViewModel(): AuthViewModel = KoinPlatform.getKoin().get()
