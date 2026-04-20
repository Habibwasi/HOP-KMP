package com.example.hop.di

import org.koin.core.module.Module

/**
 * All shared Koin modules. Pass to [org.koin.core.context.startKoin] on each platform,
 * combined with the platform-specific module that provides [com.example.hop.network.TokenStorage].
 *
 * Android example:
 *   startKoin {
 *       androidContext(this@App)
 *       modules(appModules + androidModule)
 *   }
 */
val appModules: List<Module> = listOf(
    networkModule,
    repositoryModule,
    chatRepositoryModule,
    presentationModule,
    connectivityModule,
    tokenStorageModule,
)

/**
 * Dev-mode modules: replaces real repositories with in-memory fakes so the app
 * can be tested without a running backend. Network, presentation, and connectivity
 * modules are still loaded (network module is harmless — no calls are made).
 */
val devAppModules: List<Module> = listOf(
    networkModule,
    devRepositoryModule,
    presentationModule,
    connectivityModule,
    tokenStorageModule,
)
