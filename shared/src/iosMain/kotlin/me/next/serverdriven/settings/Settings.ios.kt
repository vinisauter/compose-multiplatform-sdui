package me.next.serverdriven.settings

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings

actual val factory: Settings.Factory = NSUserDefaultsSettings.Factory()
//@OptIn(ExperimentalSettingsImplementation::class)
//actual val factory: Settings.Factory = KeychainSettings.Factory()
