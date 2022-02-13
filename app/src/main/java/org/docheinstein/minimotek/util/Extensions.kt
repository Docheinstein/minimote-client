package org.docheinstein.minimotek.util

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import org.docheinstein.minimotek.BuildConfig

// LOGGING

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

// UI

fun EditText.addAfterTextChangedListener(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            cb.invoke(editable.toString())
        }
    })
}