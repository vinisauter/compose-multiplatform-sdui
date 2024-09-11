package br.com.developes.sdui.android

import android.app.Application
import br.com.developes.sdui.AndroidApp

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidApp.init(this)
    }
}