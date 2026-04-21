package com.example.hop

import android.app.Application
import com.example.hop.di.appModules
import com.example.hop.di.devAppModules
import com.example.hop.network.NetworkConstants
import io.sentry.android.core.SentryAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HopApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.SENTRY_DSN.isNotBlank()) {
            SentryAndroid.init(this) { options ->
                options.dsn = BuildConfig.SENTRY_DSN
                options.tracesSampleRate = 1.0
                options.isEnableUserInteractionTracing = true
            }
        }

        val baseUrl = if (BuildConfig.DEBUG)
            NetworkConstants.LOCAL_ANDROID_BASE_URL
        else
            NetworkConstants.PRODUCTION_BASE_URL

        startKoin {
            androidContext(this@HopApplication)
            modules(
                if (BuildConfig.DEV_MODE) devAppModules(baseUrl)
                else appModules(baseUrl)
            )
        }
    }
}