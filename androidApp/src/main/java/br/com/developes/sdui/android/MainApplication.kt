package br.com.developes.sdui.android

import android.app.Application
import br.com.developes.sdui.AndroidApp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)
        AndroidApp.init(this)
    }
}