package com.example.tes

import android.app.Application
import com.example.tes.di.AppContainer

class TesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
