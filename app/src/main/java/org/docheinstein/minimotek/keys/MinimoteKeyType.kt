package org.docheinstein.minimotek.keys

import android.view.KeyEvent
import org.docheinstein.minimotek.packet.MinimotePacketType

enum class MinimoteKeyType(val value: Int, val keyCode: Int, val keyString: String) {
    Up(0x00, KeyEvent.KEYCODE_DPAD_UP, "0"),
    Down(0x01, KeyEvent.KEYCODE_DPAD_DOWN, "Down"),
    Left(0x02, KeyEvent.KEYCODE_DPAD_LEFT, "Left"),
    Right(0x03, KeyEvent.KEYCODE_DPAD_RIGHT, "Right"),
    VolumeUp(0x04, KeyEvent.KEYCODE_VOLUME_UP, "VolumeUp"),
    VolumeDown(0x05, KeyEvent.KEYCODE_VOLUME_DOWN, "VolumeDown"),
    VolumeMute(0x06, KeyEvent.KEYCODE_VOLUME_MUTE, "VolumeMute"),
    AltLeft(0x07, KeyEvent.KEYCODE_ALT_LEFT, "AltLeft"),
    AltRight(0x08, KeyEvent.KEYCODE_ALT_RIGHT, "AltRight"),
    ShiftLeft(0x09, KeyEvent.KEYCODE_SHIFT_LEFT, "ShiftLeft"),
    ShiftRight(0x0A, KeyEvent.KEYCODE_SHIFT_RIGHT, "ShiftRight"),
    CtrlLeft(0x0B, KeyEvent.KEYCODE_CTRL_LEFT, "CtrlLeft"),
    CtrlRight(0x0C, KeyEvent.KEYCODE_CTRL_RIGHT, "CtrlRight"),
    MetaLeft(0x0D, KeyEvent.KEYCODE_META_LEFT, "MetaLeft"),
    MetaRight(0x0E, KeyEvent.KEYCODE_META_RIGHT, "MetaRight"),
    AltGr(0x0F, KeyEvent.KEYCODE_SYM, "AltGr"),
    CapsLock(0x10, KeyEvent.KEYCODE_CAPS_LOCK, "CapsLock"),
    Esc(0x11, KeyEvent.KEYCODE_ESCAPE, "Esc"),
    Tab(0x12, KeyEvent.KEYCODE_TAB, "Tab"),
    Space(0x13, KeyEvent.KEYCODE_SPACE, "Space"),
    Enter(0x14, KeyEvent.KEYCODE_ENTER, "Enter"),
    Backspace(0x15, KeyEvent.KEYCODE_DEL, "Backspace"),
    Canc(0x16, KeyEvent.KEYCODE_FORWARD_DEL, "Canc"),
    Print(0x17, KeyEvent.KEYCODE_SYSRQ, "Print"),
    F1(0x18, KeyEvent.KEYCODE_F1, "F1"),
    F2(0x19, KeyEvent.KEYCODE_F2, "F2"),
    F3(0x1A, KeyEvent.KEYCODE_F3, "F3"),
    F4(0x1B, KeyEvent.KEYCODE_F4, "F4"),
    F5(0x1C, KeyEvent.KEYCODE_F5, "F5"),
    F6(0x1D, KeyEvent.KEYCODE_F6, "F6"),
    F7(0x1E, KeyEvent.KEYCODE_F7, "F7"),
    F8(0x1F, KeyEvent.KEYCODE_F8, "F8"),
    F9(0x20, KeyEvent.KEYCODE_F9, "F9"),
    F10(0x21, KeyEvent.KEYCODE_F10, "F10"),
    F11(0x22, KeyEvent.KEYCODE_F11, "F11"),
    F12(0x23, KeyEvent.KEYCODE_F12, "F12"),
    Zero(0x24, KeyEvent.KEYCODE_0, "0"),
    One(0x25, KeyEvent.KEYCODE_1, "1"),
    Two(0x26, KeyEvent.KEYCODE_2, "2"),
    Three(0x27, KeyEvent.KEYCODE_3, "3"),
    Four(0x28, KeyEvent.KEYCODE_4, "4"),
    Five(0x29, KeyEvent.KEYCODE_5, "5"),
    Six(0x2A, KeyEvent.KEYCODE_6, "6"),
    Seven(0x2B, KeyEvent.KEYCODE_7, "7"),
    Eight(0x2C, KeyEvent.KEYCODE_8, "8"),
    Nine(0x2D, KeyEvent.KEYCODE_9, "9"),
    A(0x2E, KeyEvent.KEYCODE_A, "A"),
    B(0x2F, KeyEvent.KEYCODE_B, "B"),
    C(0x30, KeyEvent.KEYCODE_C, "C"),
    D(0x31, KeyEvent.KEYCODE_D, "D"),
    E(0x32, KeyEvent.KEYCODE_E, "E"),
    F(0x33, KeyEvent.KEYCODE_F, "F"),
    G(0x34, KeyEvent.KEYCODE_G, "G"),
    H(0x35, KeyEvent.KEYCODE_H, "H"),
    I(0x36, KeyEvent.KEYCODE_I, "I"),
    J(0x37, KeyEvent.KEYCODE_J, "J"),
    K(0x38, KeyEvent.KEYCODE_K, "K"),
    L(0x39, KeyEvent.KEYCODE_L, "L"),
    M(0x3A, KeyEvent.KEYCODE_M, "M"),
    N(0x3B, KeyEvent.KEYCODE_N, "N"),
    O(0x3C, KeyEvent.KEYCODE_O, "O"),
    P(0x3D, KeyEvent.KEYCODE_P, "P"),
    Q(0x3E, KeyEvent.KEYCODE_Q, "Q"),
    R(0x3F, KeyEvent.KEYCODE_R, "R"),
    S(0x40, KeyEvent.KEYCODE_S, "S"),
    T(0x41, KeyEvent.KEYCODE_T, "T"),
    U(0x42, KeyEvent.KEYCODE_U, "U"),
    V(0x43, KeyEvent.KEYCODE_V, "V"),
    W(0x44, KeyEvent.KEYCODE_W, "W"),
    X(0x45, KeyEvent.KEYCODE_X, "X"),
    Y(0x46, KeyEvent.KEYCODE_Y, "Y"),
    Z(0x47, KeyEvent.KEYCODE_Z, "Z");

    companion object {
        private val valueMap = values().associateBy(MinimoteKeyType::keyCode)
        private val keyCodeMap = values().associateBy(MinimoteKeyType::keyCode)
        private val keyStringMap = values().associateBy(MinimoteKeyType::keyString)

        operator fun get(value: Int): MinimoteKeyType? {
            return byValue(value)
        }

        fun byValue(value: Int): MinimoteKeyType? {
            return valueMap.getOrDefault(value, null)
        }
        fun byKeyCode(code: Int): MinimoteKeyType? {
            return keyCodeMap.getOrDefault(code, null)
        }
        fun byKeyString(string: String): MinimoteKeyType? {
            return keyStringMap.getOrDefault(string, null)
        }
    }
}