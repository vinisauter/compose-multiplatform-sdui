package br.com.developes.sdui.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.developes.sdui.MainView
import br.com.developes.sdui.events.lifecycle.AndroidLifecycleEventObserver
import br.com.developes.sdui.events.lifecycle.LifecycleTracker

class MainActivity : ComponentActivity() {
    private val lifecycleTracker = LifecycleTracker()
    private val observer = AndroidLifecycleEventObserver(lifecycleTracker)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(observer)
        setContent {
            MainView(lifecycleTracker)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(observer)
    }
}