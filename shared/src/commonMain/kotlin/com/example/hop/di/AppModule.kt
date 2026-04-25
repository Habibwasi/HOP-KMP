package com.example.hop.di

import com.example.hop.network.NetworkConstants
import org.koin.core.module.Module

fun appModules(
    baseUrl: String = NetworkConstants.PRODUCTION_BASE_URL,
    supabaseUrl: String,
    supabaseAnonKey: String,
): List<Module> = listOf(
    tokenStorageModule,
    supabaseModule(supabaseUrl, supabaseAnonKey),
    networkModule(baseUrl),
    repositoryModule,
    chatRepositoryModule,
    presentationModule,
    connectivityModule,
)

fun devAppModules(
    baseUrl: String = NetworkConstants.PRODUCTION_BASE_URL,
    supabaseUrl: String,
    supabaseAnonKey: String,
): List<Module> = listOf(
    tokenStorageModule,
    supabaseModule(supabaseUrl, supabaseAnonKey),
    networkModule(baseUrl),
    devRepositoryModule,
    presentationModule,
    connectivityModule,
)