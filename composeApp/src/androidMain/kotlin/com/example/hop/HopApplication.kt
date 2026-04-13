package com.example.hop

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.example.hop.di.appModules
import com.example.hop.di.androidNetworkModule

class HopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HopApplication)
            modules(appModules + androidNetworkModule)
        }
    }
}
