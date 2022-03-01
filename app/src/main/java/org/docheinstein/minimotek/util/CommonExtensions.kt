package org.docheinstein.minimotek.util

import android.util.Log
import org.docheinstein.minimotek.BuildConfig


/*
 * LOGGING.
 * Defines error(), warn(), info(), debug(), verbose()
 * for any class, using the class' name as TAG.
 */

private fun formatLogMessage(message: String): String {
    return if (!BuildConfig.DEBUG)
        message
    else
        "[${Thread.currentThread().name}] $message"
}

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun Any.info(message: String) {
    Log.i(TAG, formatLogMessage(message))
}

fun Any.error(message: String, throwable: Throwable? = null) {
    if (throwable != null)
        Log.e(TAG, formatLogMessage(message), throwable)
    else
        Log.e(TAG, formatLogMessage(message))
}

fun Any.warn(message: String, throwable: Throwable? = null) {
    if (throwable != null)
        Log.w(TAG, formatLogMessage(message), throwable)
    else
        Log.w(TAG, formatLogMessage(message))
}

fun Any.debug(message: String) {
    if (BuildConfig.DEBUG)
        Log.d(TAG, formatLogMessage(message))
}

fun Any.verbose(message: String) {
    if (BuildConfig.DEBUG)
        Log.v(TAG, formatLogMessage(message))
}

/*
 * EXCEPTIONS.
 */

fun Exception.asMessage(): String {
    return message ?: toString()
}