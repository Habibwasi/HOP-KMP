package com.example.hop

import android.app.Application
import com.example.hop.di.appModules
import com.example.hop.di.devAppModules
import com.example.hop.network.NetworkConstants
import com.google.android.libraries.places.api.Places
import io.sentry.android.core.SentryAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HopApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialise Places SDK (uses same API key declared in the manifest).
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.MAPS_API_KEY)
        }

        if (BuildConfig.SENTRY_DSN.isNotBlank()) {
            SentryAndroid.init(this) { options ->
                options.dsn = BuildConfig.SENTRY_DSN
                options.tracesSampleRate = 1.0
                options.isEnableUserInteractionTracing = true
            }
        }
//       val baseUrl = NetworkConstants.PRODUCTION_BASE_URL

        val baseUrl = NetworkConstants.PRODUCTION_BASE_URL

        startKoin {
            androidContext(this@HopApplication)
            modules(
                if (BuildConfig.DEV_MODE) devAppModules(baseUrl, BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY, BuildConfig.MAPS_API_KEY)
                else appModules(baseUrl, BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY, BuildConfig.MAPS_API_KEY)
            )
        }
    }
}