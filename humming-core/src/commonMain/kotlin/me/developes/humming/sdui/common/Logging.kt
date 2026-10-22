package me.developes.humming.sdui.common

interface Logging {
    fun log(message: String)
    fun warn(message: String)
    fun error(message: String)
}

expect val logging: Logging