package me.next.serverdriven.settings

import AndroidApp
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual val factory: Settings.Factory = SharedPreferencesSettings.Factory(AndroidApp.INSTANCE)