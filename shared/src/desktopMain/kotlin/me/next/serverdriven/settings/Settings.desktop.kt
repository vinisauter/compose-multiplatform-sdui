package me.next.serverdriven.settings

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings

actual val factory: Settings.Factory = PreferencesSettings.Factory()
