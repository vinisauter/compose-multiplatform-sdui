package com.myapplication

import AndroidApp
import android.app.Application

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidApp.init(this)
    }
}