package com.example.hop.di

import com.example.hop.network.NetworkConstants
import org.koin.core.module.Module

fun appModules(baseUrl: String = NetworkConstants.PRODUCTION_BASE_URL): List<Module> = listOf(
    networkModule(baseUrl),
    repositoryModule,
    chatRepositoryModule,
    presentationModule,
    connectivityModule,
    tokenStorageModule,
)

fun devAppModules(baseUrl: String = NetworkConstants.PRODUCTION_BASE_URL): List<Module> = listOf(
    networkModule(baseUrl),
    devRepositoryModule,
    presentationModule,
    connectivityModule,
    tokenStorageModule,
)