package org.docheinstein.minimotek.ui.controller.keyboard

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn

open class KeyboardEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatEditText(context, attrs, defStyle) {


    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null)
            return super.onKeyPreIme(keyCode, event)

        debug("onKeyPreIme: (keyCode = $keyCode, action = ${event.action})")

        if (event.keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP) {
            onKeyboardHidden()
        }

        return super.onKeyPreIme(keyCode, event)
    }

    protected fun onKeyboardHidden() {
        debug("Keyboard has been hidden, hiding EditText")
        isVisible = false
    }

    fun toggleKeyboard(activity: Activity) {
        if (isVisible)
            closeKeyboard(activity)
        else
            openKeyboard(activity)
    }

    fun openKeyboard(activity: Activity) {
        debug("Opening keyboard")

        // Show this view
        isVisible = true

        // Require focus
        if (!requestFocus()) {
            warn("KeyboardEditText failed to acquire focus")
            return
        }

        // Show keyboard
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun closeKeyboard(activity: Activity) {
        debug("Closing keyboard")

        // hide this view
        isVisible = false

        // hide keyboard
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}