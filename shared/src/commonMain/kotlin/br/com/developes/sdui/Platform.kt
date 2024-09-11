package br.com.developes.sdui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun generateUUID(): String
expect fun openUrl(url: String?)