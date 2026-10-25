package sample.app

import android.os.SystemClock
import me.developes.humming.sdui.common.*

/** No permission or Activity retained. The returned number is monotonic elapsed milliseconds since boot. */
fun androidNativeBridge() = NativeBridge(mapOf("device.uptime" to { input ->
    if (input !is SduiValue.ObjectValue || input.values.isNotEmpty()) NativeResult.Rejected("NATIVE_ARGUMENT")
    else NativeResult.Success(SduiValue.NumberValue(SystemClock.elapsedRealtime().toString()))
}))
