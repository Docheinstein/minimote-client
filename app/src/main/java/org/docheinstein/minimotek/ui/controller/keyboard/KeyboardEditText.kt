package org.docheinstein.minimotek.ui.controller.keyboard

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn


class KeyboardEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatEditText(context, attrs, defStyle),
    TextWatcher, /* for soft keyboard chars and backspace */
    View.OnKeyListener /* for physical keyboards and special chars on soft keyboards */{

    interface KeyboardListener {
        fun onKeyboardText(s: CharSequence, start: Int, before: Int, count: Int)
        fun onKeyboardKey(keyCode: Int, event: KeyEvent): Boolean
        fun onKeyboardShown()
        fun onKeyboardHidden()
    }

    var isKeyboardOpen = false
        private set

    var listener: KeyboardListener? = null

    init {
        debug("KeyboardEditText.init() ${hashCode()}")
        isVisible = false

        // View.OnKeyListener must be set, but
        // TextWatcher events are already received
        setOnKeyListener(this)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null)
            return super.onKeyPreIme(keyCode, event)

        debug("onKeyPreIme: (keyCode = $keyCode, action = ${event.action})")

        if (event.keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP) {
            onKeyboardHiddenByUser()
        }

        return super.onKeyPreIme(keyCode, event)
    }

    private fun onKeyboardHiddenByUser() {
        debug("Keyboard has been hidden, hiding EditText")

        isKeyboardOpen = false

        // Hide this view
        isVisible = false

        // Notify listener (eventually)
        listener?.onKeyboardHidden()
    }

    fun setKeyboardOpen(activity: Activity, yes: Boolean) {
        if (yes)
            openKeyboard(activity)
        else
            closeKeyboard(activity)
    }

    fun toggleKeyboard(activity: Activity) {
        if (isKeyboardOpen)
            closeKeyboard(activity)
        else
            openKeyboard(activity)
    }

    fun openKeyboard(activity: Activity) {
        if (isKeyboardOpen) {
            debug("Keyboard already opened, nothing to do")
            return
        }

        debug("Opening keyboard")

        isKeyboardOpen = true

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

        // Notify listener (eventually)
        listener?.onKeyboardShown()
    }

    fun closeKeyboard(activity: Activity) {
        if (!isKeyboardOpen) {
            debug("Keyboard already closed, nothing to do")
            return
        }

        debug("Closing keyboard")
        isKeyboardOpen = false

        // Hide this view
        isVisible = false

        // Hide keyboard
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        // Notify listener (eventually)
        listener?.onKeyboardHidden()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}

    override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) {
        if (s == null)
            return
        debug("KeyboardEditText.onTextChanged() (str = $s)")
        listener?.onKeyboardText(s, start, before, count)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null || listener == null)
            return false
        debug("KeyboardEditText.onKey() (keyCode = $keyCode)")
        return listener!!.onKeyboardKey(keyCode, event)
    }
}