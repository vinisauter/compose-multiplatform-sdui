package me.developes.humming.sdui

import platform.Foundation.NSUUID

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "iOS"

    override fun generateUUID(): String {
        return NSUUID().UUIDString()
    }
}
