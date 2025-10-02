package me.developes.humming.sdui

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "Web"
    override fun generateUUID(): String = js("crypto.randomUUID()") as String
}