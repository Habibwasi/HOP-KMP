package com.example.hop

import android.app.Application
import io.sentry.android.core.SentryAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.example.hop.di.appModules
import com.example.hop.di.androidNetworkModule

class HopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.tracesSampleRate = 1.0
            options.isEnableUserInteractionTracing = true
        }
        startKoin {
            androidContext(this@HopApplication)
            modules(appModules + androidNetworkModule)
        }
    }
}
