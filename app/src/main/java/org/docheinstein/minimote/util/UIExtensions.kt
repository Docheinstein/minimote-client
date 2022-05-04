package org.docheinstein.minimote.util

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*

/* EditText */

fun EditText.addAfterTextChangedListener(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            cb.invoke(editable.toString())
        }
    })
}

/* Spinner */

fun Spinner.setOnItemActuallySelectedListener(cb: (Int) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            cb(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}

@Suppress("UNCHECKED_CAST")
fun Spinner.setSelection(value: String) {
    setSelection((adapter as ArrayAdapter<String>).getPosition(value))
}

/* SeekBar */

fun SeekBar.setOnSeekbarProgressListener(cb: (progress: Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            cb(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}