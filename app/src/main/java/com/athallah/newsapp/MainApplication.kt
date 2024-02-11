package com.athallah.newsapp

import android.app.Application
import com.athallah.newsapp.data.di.apiModule
import com.athallah.newsapp.data.di.repositoryModule
import com.athallah.newsapp.di.vmModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MainApplication)
            modules(
                listOf(
                    apiModule,
                    vmModule,
                    repositoryModule
                    )
            )
        }
    }
}