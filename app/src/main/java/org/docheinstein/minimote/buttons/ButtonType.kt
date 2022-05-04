package org.docheinstein.minimote.buttons

import android.view.KeyEvent

/** Physical button */
enum class ButtonType(val keyCode: Int, val keyString: String) {
    VolumeUp(KeyEvent.KEYCODE_VOLUME_UP, "VolumeUp"),
    VolumeDown(KeyEvent.KEYCODE_VOLUME_DOWN, "VolumeDown");

    companion object {
        private val keyCodeMap = values().associateBy(ButtonType::keyCode)
        private val keyStringMap = values().associateBy(ButtonType::keyString)

        fun byKeyCode(code: Int): ButtonType? {
            return keyCodeMap.getOrDefault(code, null)
        }
        fun byKeyString(string: String): ButtonType? {
            return keyStringMap.getOrDefault(string, null)
        }
    }
}