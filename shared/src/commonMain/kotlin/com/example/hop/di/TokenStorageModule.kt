package com.example.hop.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module that binds the platform's [com.example.hop.network.TokenStorage]
 * implementation. Provided via `actual` declarations in androidMain and iosMain.
 */
expect val tokenStorageModule: Module
