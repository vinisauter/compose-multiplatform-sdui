package me.developes.humming.sdui

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "JVM"

    override fun generateUUID(): String {
        return java.util.UUID.randomUUID().toString()
    }
}