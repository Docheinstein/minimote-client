package org.docheinstein.minimotek.util

import android.util.Log
import org.docheinstein.minimotek.BuildConfig

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun Any.info(message: String) {
    Log.i(TAG, message)
}

fun Any.error(message: String) {
    Log.e(TAG, message)
}

fun Any.warn(message: String) {
    Log.w(TAG, message)
}

fun Any.debug(message: String) {
    if (BuildConfig.DEBUG)
        Log.d(TAG, message)
}