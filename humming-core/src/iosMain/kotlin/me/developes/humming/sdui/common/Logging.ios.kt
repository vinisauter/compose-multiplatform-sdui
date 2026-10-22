package me.developes.humming.sdui.common

actual val logging: Logging = object : Logging {
    override fun log(message: String) {
        println("ℹ️ INFO: $message")
    }

    override fun warn(message: String) {
        println("⚠️ WARN: $message")
    }

    override fun error(message: String) {
        println("❌ ERROR: $message")
    }
}