package com.example.gogolookinterview

import android.app.Application

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
        val context get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}