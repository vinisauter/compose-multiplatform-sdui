package me.next.serverdriven.settings

import com.russhwolf.settings.Settings

expect val factory: Settings.Factory

fun createKeyValueSettings(name: String = "me.next.serverdriven"): Settings = factory.create(name)