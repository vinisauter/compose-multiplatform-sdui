package me.next.serverdriven.core.library.action.method

import getPlatformName
import me.next.serverdriven.core.library.action.SDAction
import openUrl

fun registerNativeMethods(actionLib: SDAction) {
    actionLib.apply {
        registerMethod("openUrl") { node, states ->
            val url = node.propertyState("url", states)
            openUrl(url)
        }
        registerMethod("getPlatformName") { node, states ->
            val stateName = node.property("state") ?: "platformName"
            states[stateName] = getPlatformName()
        }
    }
}