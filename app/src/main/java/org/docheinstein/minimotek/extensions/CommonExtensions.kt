package org.docheinstein.minimotek.util

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import org.docheinstein.minimotek.BuildConfig

// LOGGING

private fun formatLogMessage(message: String): String {
    if (!BuildConfig.DEBUG)
        return message
    val th = Thread.currentThread()
    val thStr = if (th.threadGroup != null)
        "[${th.name},${th.threadGroup!!.name}]"
    else
        "[${th.name}]"
    return "$thStr $message"
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

fun Any.warn(message: String) {
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

// EXCEPTIONS

fun Exception.asMessage(): String {
    return message ?: toString()
}