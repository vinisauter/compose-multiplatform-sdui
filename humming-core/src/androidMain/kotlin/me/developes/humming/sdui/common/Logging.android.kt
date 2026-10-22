package me.developes.humming.sdui.common

import android.util.Log

actual val logging: Logging = object : Logging {
    override fun log(message: String) {
        Log.i("HUMMING", "ℹ️ INFO: $message")
    }

    override fun warn(message: String) {
        Log.w("HUMMING", "⚠️ WARN: $message")
    }

    override fun error(message: String) {
        Log.e("HUMMING", "❌ ERROR: $message")
    }
}