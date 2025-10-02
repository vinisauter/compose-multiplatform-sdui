package me.developes.humming.sdui

interface Platform {
    val name: String
    fun generateUUID(): String
}

expect fun getPlatform(): Platform