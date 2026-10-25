package sample.app

import android.app.Activity
import android.app.Instrumentation
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import kotlinx.coroutines.runBlocking
import me.developes.humming.sdui.common.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Dependency-free device probe; asserts real framework calls, not a desktop substitute. */
class NativeBridgeInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    override fun onStart() {
        val report = Bundle()
        try {
            runBlocking {
                val bridge = androidNativeBridge()
                val before = SystemClock.elapsedRealtime()
                val result = bridge.invoke(NativeRequest("device.uptime"))
                check(result is NativeResult.Success)
                val uptime = (result.value as SduiValue.NumberValue).source.toLong()
                check(uptime in before..SystemClock.elapsedRealtime())
                check(bridge.invoke(NativeRequest("device.uptime", SduiValue.Null)) == NativeResult.Rejected("NATIVE_ARGUMENT"))
                check(bridge.invoke(NativeRequest("device.unknown")) == NativeResult.Unavailable)
                check(bridge.invoke(NativeRequest("device.uptime", version = 2)) == NativeResult.Rejected("NATIVE_VERSION"))
                report.putString("native.result", "PASS")
                report.putLong("native.uptimeMillis", uptime)
                report.putString("native.cases", "real-uptime,argument-rejection,unavailable,version-rejection")
            }
            val application = targetContext.applicationContext as Application
            val created = AtomicInteger()
            val destroyed = AtomicInteger()
            val recreated = CountDownLatch(1)
            val observer = object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, state: Bundle?) {
                    if (activity is AppActivity && created.incrementAndGet() == 2) recreated.countDown()
                }
                override fun onActivityDestroyed(activity: Activity) { if (activity is AppActivity) destroyed.incrementAndGet() }
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityResumed(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
            }
            application.registerActivityLifecycleCallbacks(observer)
            try {
                val activity = startActivitySync(Intent(targetContext, AppActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("nativeExample", true))
                waitForIdleSync()
                runOnMainSync { activity.recreate() }
                check(recreated.await(10, TimeUnit.SECONDS)) { "Host Activity was not recreated" }
                waitForIdleSync()
                check(created.get() == 2 && destroyed.get() >= 1)
                report.putString("native.hostRecreation", "PASS")
            } finally { application.unregisterActivityLifecycleCallbacks(observer) }
            finish(Activity.RESULT_OK, report)
        } catch (failure: Throwable) {
            report.putString("native.result", "FAIL")
            report.putString("native.failure", failure.stackTraceToString())
            finish(Activity.RESULT_CANCELED, report)
        }
    }
}
