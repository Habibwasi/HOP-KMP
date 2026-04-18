package com.example.hop.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module that binds the platform's [com.example.hop.network.ConnectivityObserver]
 * implementation. Provided via `actual` declarations in androidMain and iosMain.
 */
expect val connectivityModule: Module
