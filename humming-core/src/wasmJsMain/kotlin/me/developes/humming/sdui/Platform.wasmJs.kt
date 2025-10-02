package me.developes.humming.sdui

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "WasmJs"

    override fun generateUUID(): String {
       return Uuid.random().toString()
    }
}