package me.next.serverdriven.utils.logger

import android.util.Log

/** Low-level logging call.
 *
 * @param priority enum: The priority/type of this log message Value is ASSERT,
 * ERROR, WARN, INFO, DEBUG, or VERBOSE
 * @param tag String: Used to identify the source of a log message. It usually
 * identifies the class or activity where the log call occurs. This value may be null.
 * @param throwable Throwable: An exception to log This value may be null.
 * @param message String: The message you would like logged. This value cannot be null.
 */
actual fun SimpleLogger.log(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String
) {
    when (priority) {
        LogLevel.VERBOSE -> Log.v(tag, message, throwable)
        LogLevel.DEBUG -> Log.d(tag, message, throwable)
        LogLevel.INFO -> Log.i(tag, message, throwable)
        LogLevel.WARNING -> Log.w(tag, message, throwable)
        LogLevel.ERROR -> Log.e(tag, message, throwable)
        LogLevel.ASSERT -> Log.wtf(tag, message, throwable)
    }
}