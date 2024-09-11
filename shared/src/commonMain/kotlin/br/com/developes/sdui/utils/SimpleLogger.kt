package br.com.developes.sdui.utils

class SimpleLogger(val appTag: String) {

    /** Send a VERBOSE log message and log the exception.
     *
     * @param message String: The message you would like logged. This value cannot be null.
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     */
    fun v(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.VERBOSE, tag ?: appTag, throwable, message)
    }

    /** Send a VERBOSE log message and log the exception.
     *
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     * @param message Lambda: The message you would like logged. This value cannot be null.
     */
    fun v(throwable: Throwable? = null, tag: String? = null, message: () -> String) {
        log(LogLevel.VERBOSE, tag ?: appTag, throwable, message())
    }

    /** Send a INFO log message and log the exception.
     *
     * @param message String: The message you would like logged. This value cannot be null.
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     */
    fun i(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.INFO, tag ?: appTag, throwable, message)
    }

    /** Send a INFO log message and log the exception.
     *
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     * @param message Lambda: The message you would like logged. This value cannot be null.
     */
    fun i(throwable: Throwable? = null, tag: String? = null, message: () -> String) {
        log(LogLevel.INFO, tag ?: appTag, throwable, message())
    }

    /** Send a DEBUG log message and log the exception.
     *
     * @param message String: The message you would like logged. This value cannot be null.
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     */
    fun d(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.DEBUG, tag ?: appTag, throwable, message)
    }

    /** Send a DEBUG log message and log the exception.
     *
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     * @param message Lambda: The message you would like logged. This value cannot be null.
     */
    fun d(throwable: Throwable? = null, tag: String? = null, message: () -> String) {
        log(LogLevel.DEBUG, tag ?: appTag, throwable, message())
    }

    /** Send a WARN log message and log the exception.
     *
     * @param message String: The message you would like logged. This value cannot be null.
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     */
    fun w(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.WARNING, tag ?: appTag, throwable, message)
    }

    /** Send a WARN log message and log the exception.
     *
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     * @param message Lambda: The message you would like logged. This value cannot be null.
     */
    fun w(throwable: Throwable? = null, tag: String? = null, message: () -> String) {
        log(LogLevel.WARNING, tag ?: appTag, throwable, message())
    }

    /** Send a ERROR log message and log the exception.
     *
     * @param message String: The message you would like logged. This value cannot be null.
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     */
    fun e(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.ERROR, tag ?: appTag, throwable, message)
    }

    /** Send a ERROR log message and log the exception.
     *
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     * @param message Lambda: The message you would like logged. This value cannot be null.
     */
    fun e(throwable: Throwable? = null, tag: String? = null, message: () -> String) {
        log(LogLevel.ERROR, tag ?: appTag, throwable, message())
    }

    /** What a Terrible Failure: Report an exception that should never happen.
     *
     * @param message String: The message you would like logged. This value cannot be null.
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     */
    fun wtf(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.ASSERT, tag ?: appTag, throwable, message)
    }

    /** What a Terrible Failure: Report an exception that should never happen.
     *
     * @param throwable Throwable: An exception to log This value may be null.
     * @param tag String: Used to identify the source of a log message. It usually
     * identifies the class or activity where the log call occurs. This value may be null.
     * @param message Lambda: The message you would like logged. This value cannot be null.
     */
    fun wtf(throwable: Throwable? = null, tag: String? = null, message: () -> String) {
        log(LogLevel.ASSERT, tag ?: appTag, throwable, message())
    }
}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    ASSERT,
}

/** Low-level logging call.
 *
 * @param priority enum: The priority/type of this log message Value is ASSERT,
 * ERROR, WARN, INFO, DEBUG, or VERBOSE
 * @param tag String: Used to identify the source of a log message. It usually
 * identifies the class or activity where the log call occurs. This value may be null.
 * @param throwable Throwable: An exception to log This value may be null.
 * @param message String: The message you would like logged. This value cannot be null.
 */
expect fun SimpleLogger.log(
    priority: LogLevel,
    tag: String? = null,
    throwable: Throwable? = null,
    message: String
)