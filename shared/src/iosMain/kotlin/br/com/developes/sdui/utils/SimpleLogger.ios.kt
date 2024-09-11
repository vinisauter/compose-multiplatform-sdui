package br.com.developes.sdui.utils

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
    println("$priority - $tag: $message.${throwable?.let { "\n${it.stackTraceToString()}" } ?: ""}")
}