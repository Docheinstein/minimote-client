package org.docheinstein.minimote.keys;

import android.view.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public enum MinimoteKeyType {
    Up(0x00),
    Down(0x01),
    Left(0x02),
    Right(0x03),
    VolumeUp(0x04),
    VolumeDown(0x05),
    VolumeMute(0x06),
    AltLeft(0x07),
    AltRight(0x08),
    ShiftLeft(0x09),
    ShiftRight(0x0A),
    CtrlLeft(0x0B),
    CtrlRight(0x0C),
    MetaLeft(0x0D),
    MetaRight(0x0E),
    AltGr(0x0F),
    CapsLock(0x10),
    Esc(0x11),
    Tab(0x12),
    Space(0x13),
    Enter(0x14),
    Backspace(0x15),
    Canc(0x16),
    Print(0x17),
    F1(0x18),
    F2(0x19),
    F3(0x1A),
    F4(0x1B),
    F5(0x1C),
    F6(0x1D),
    F7(0x1E),
    F8(0x1F),
    F9(0x20),
    F10(0x21),
    F11(0x22),
    F12(0x23),
    Zero(0x24),
    One(0x25),
    Two(0x26),
    Three(0x27),
    Four(0x28),
    Five(0x29),
    Six(0x2A),
    Seven(0x2B),
    Eight(0x2C),
    Nine(0x2D),
    A(0x2E),
    B(0x2F),
    C(0x30),
    D(0x31),
    E(0x32),
    F(0x33),
    G(0x34),
    H(0x35),
    I(0x36),
    J(0x37),
    K(0x38),
    L(0x39),
    M(0x3A),
    N(0x3B),
    O(0x3C),
    P(0x3D),
    Q(0x3E),
    R(0x3F),
    S(0x40),
    T(0x41),
    U(0x42),
    V(0x43),
    W(0x44),
    X(0x45),
    Y(0x46),
    Z(0x47),
    ;

    private static Map<String, MinimoteKeyType> STRING_TO_KEYTYPE = new HashMap<>();

    static {
        STRING_TO_KEYTYPE.put("Up", Up);
        STRING_TO_KEYTYPE.put("Down", Down);
        STRING_TO_KEYTYPE.put("Left", Left);
        STRING_TO_KEYTYPE.put("Right", Right);
        STRING_TO_KEYTYPE.put("VolumeUp", VolumeUp);
        STRING_TO_KEYTYPE.put("VolumeDown", VolumeDown);
        STRING_TO_KEYTYPE.put("VolumeMute", VolumeMute);
        STRING_TO_KEYTYPE.put("AltLeft", AltLeft);
        STRING_TO_KEYTYPE.put("AltRight", AltRight);
        STRING_TO_KEYTYPE.put("ShiftLeft", ShiftLeft);
        STRING_TO_KEYTYPE.put("ShiftRight", ShiftRight);
        STRING_TO_KEYTYPE.put("CtrlLeft", CtrlLeft);
        STRING_TO_KEYTYPE.put("CtrlRight", CtrlRight);
        STRING_TO_KEYTYPE.put("MetaLeft", MetaLeft);
        STRING_TO_KEYTYPE.put("MetaRight", MetaRight);
        STRING_TO_KEYTYPE.put("AltGr", AltGr);
        STRING_TO_KEYTYPE.put("CapsLock", CapsLock);
        STRING_TO_KEYTYPE.put("Esc", Esc);
        STRING_TO_KEYTYPE.put("Tab", Tab);
        STRING_TO_KEYTYPE.put("Space", Space);
        STRING_TO_KEYTYPE.put("Enter", Enter);
        STRING_TO_KEYTYPE.put("Backspace", Backspace);
        STRING_TO_KEYTYPE.put("Canc", Canc);
        STRING_TO_KEYTYPE.put("Print", Print);
        STRING_TO_KEYTYPE.put("F1", F1);
        STRING_TO_KEYTYPE.put("F2", F2);
        STRING_TO_KEYTYPE.put("F3", F3);
        STRING_TO_KEYTYPE.put("F4", F4);
        STRING_TO_KEYTYPE.put("F5", F5);
        STRING_TO_KEYTYPE.put("F6", F6);
        STRING_TO_KEYTYPE.put("F7", F7);
        STRING_TO_KEYTYPE.put("F8", F8);
        STRING_TO_KEYTYPE.put("F9", F9);
        STRING_TO_KEYTYPE.put("F10", F10);
        STRING_TO_KEYTYPE.put("F11", F11);
        STRING_TO_KEYTYPE.put("F12", F12);
        STRING_TO_KEYTYPE.put("Zero", Zero);
        STRING_TO_KEYTYPE.put("One", One);
        STRING_TO_KEYTYPE.put("Two", Two);
        STRING_TO_KEYTYPE.put("Three", Three);
        STRING_TO_KEYTYPE.put("Four", Four);
        STRING_TO_KEYTYPE.put("Five", Five);
        STRING_TO_KEYTYPE.put("Six", Six);
        STRING_TO_KEYTYPE.put("Seven", Seven);
        STRING_TO_KEYTYPE.put("Eight", Eight);
        STRING_TO_KEYTYPE.put("Nine", Nine);
        STRING_TO_KEYTYPE.put("0", Zero);
        STRING_TO_KEYTYPE.put("1", One);
        STRING_TO_KEYTYPE.put("2", Two);
        STRING_TO_KEYTYPE.put("3", Three);
        STRING_TO_KEYTYPE.put("4", Four);
        STRING_TO_KEYTYPE.put("5", Five);
        STRING_TO_KEYTYPE.put("6", Six);
        STRING_TO_KEYTYPE.put("7", Seven);
        STRING_TO_KEYTYPE.put("8", Eight);
        STRING_TO_KEYTYPE.put("9", Nine);
        STRING_TO_KEYTYPE.put("A", A);
        STRING_TO_KEYTYPE.put("B", B);
        STRING_TO_KEYTYPE.put("C", C);
        STRING_TO_KEYTYPE.put("D", D);
        STRING_TO_KEYTYPE.put("E", E);
        STRING_TO_KEYTYPE.put("F", F);
        STRING_TO_KEYTYPE.put("G", G);
        STRING_TO_KEYTYPE.put("H", H);
        STRING_TO_KEYTYPE.put("I", I);
        STRING_TO_KEYTYPE.put("J", J);
        STRING_TO_KEYTYPE.put("K", K);
        STRING_TO_KEYTYPE.put("L", L);
        STRING_TO_KEYTYPE.put("M", M);
        STRING_TO_KEYTYPE.put("N", N);
        STRING_TO_KEYTYPE.put("O", O);
        STRING_TO_KEYTYPE.put("P", P);
        STRING_TO_KEYTYPE.put("Q", Q);
        STRING_TO_KEYTYPE.put("R", R);
        STRING_TO_KEYTYPE.put("S", S);
        STRING_TO_KEYTYPE.put("T", T);
        STRING_TO_KEYTYPE.put("U", U);
        STRING_TO_KEYTYPE.put("V", V);
        STRING_TO_KEYTYPE.put("W", W);
        STRING_TO_KEYTYPE.put("X", X);
        STRING_TO_KEYTYPE.put("Y", Y);
        STRING_TO_KEYTYPE.put("Z", Z);
    }

    public static MinimoteKeyType fromString(String keyString) {
        return STRING_TO_KEYTYPE.get(keyString);
    }

    public static MinimoteKeyType fromAndroidKeyCode(int keycode) {
        switch (keycode) {
            case KeyEvent.KEYCODE_DPAD_UP: return Up;
            case KeyEvent.KEYCODE_DPAD_DOWN: return Down;
            case KeyEvent.KEYCODE_DPAD_LEFT: return Left;
            case KeyEvent.KEYCODE_DPAD_RIGHT: return Right;
            case KeyEvent.KEYCODE_VOLUME_UP: return VolumeUp;
            case KeyEvent.KEYCODE_VOLUME_DOWN: return VolumeDown;
            case KeyEvent.KEYCODE_VOLUME_MUTE: return VolumeMute;
            case KeyEvent.KEYCODE_ALT_LEFT: return AltLeft;
            case KeyEvent.KEYCODE_ALT_RIGHT: return AltRight;
            case KeyEvent.KEYCODE_SHIFT_LEFT: return ShiftLeft;
            case KeyEvent.KEYCODE_SHIFT_RIGHT: return ShiftRight;
            case KeyEvent.KEYCODE_CTRL_LEFT: return CtrlLeft;
            case KeyEvent.KEYCODE_CTRL_RIGHT: return CtrlRight;
            case KeyEvent.KEYCODE_META_LEFT: return MetaLeft;
            case KeyEvent.KEYCODE_META_RIGHT: return MetaRight;
            case KeyEvent.KEYCODE_SYM: return AltGr;
            case KeyEvent.KEYCODE_CAPS_LOCK: return CapsLock;
            case KeyEvent.KEYCODE_ESCAPE: return Esc;
            case KeyEvent.KEYCODE_TAB: return Tab;
            case KeyEvent.KEYCODE_SPACE: return Space;
            case KeyEvent.KEYCODE_ENTER: return Enter;
            case KeyEvent.KEYCODE_DEL: return Backspace;
            case KeyEvent.KEYCODE_FORWARD_DEL: return Canc;
            case KeyEvent.KEYCODE_SYSRQ: return Print;
            case KeyEvent.KEYCODE_F1: return F1;
            case KeyEvent.KEYCODE_F2: return F2;
            case KeyEvent.KEYCODE_F3: return F3;
            case KeyEvent.KEYCODE_F4: return F4;
            case KeyEvent.KEYCODE_F5: return F5;
            case KeyEvent.KEYCODE_F6: return F6;
            case KeyEvent.KEYCODE_F7: return F7;
            case KeyEvent.KEYCODE_F8: return F8;
            case KeyEvent.KEYCODE_F9: return F9;
            case KeyEvent.KEYCODE_F10: return F10;
            case KeyEvent.KEYCODE_F11: return F11;
            case KeyEvent.KEYCODE_F12: return F12;
            case KeyEvent.KEYCODE_0: return Zero;
            case KeyEvent.KEYCODE_1: return One;
            case KeyEvent.KEYCODE_2: return Two;
            case KeyEvent.KEYCODE_3: return Three;
            case KeyEvent.KEYCODE_4: return Four;
            case KeyEvent.KEYCODE_5: return Five;
            case KeyEvent.KEYCODE_6: return Six;
            case KeyEvent.KEYCODE_7: return Seven;
            case KeyEvent.KEYCODE_8: return Eight;
            case KeyEvent.KEYCODE_9: return Nine;
            case KeyEvent.KEYCODE_A: return A;
            case KeyEvent.KEYCODE_B: return B;
            case KeyEvent.KEYCODE_C: return C;
            case KeyEvent.KEYCODE_D: return D;
            case KeyEvent.KEYCODE_E: return E;
            case KeyEvent.KEYCODE_F: return F;
            case KeyEvent.KEYCODE_G: return G;
            case KeyEvent.KEYCODE_H: return H;
            case KeyEvent.KEYCODE_I: return I;
            case KeyEvent.KEYCODE_J: return J;
            case KeyEvent.KEYCODE_K: return K;
            case KeyEvent.KEYCODE_L: return L;
            case KeyEvent.KEYCODE_M: return M;
            case KeyEvent.KEYCODE_N: return N;
            case KeyEvent.KEYCODE_O: return O;
            case KeyEvent.KEYCODE_P: return P;
            case KeyEvent.KEYCODE_Q: return Q;
            case KeyEvent.KEYCODE_R: return R;
            case KeyEvent.KEYCODE_S: return S;
            case KeyEvent.KEYCODE_T: return T;
            case KeyEvent.KEYCODE_U: return U;
            case KeyEvent.KEYCODE_V: return V;
            case KeyEvent.KEYCODE_W: return W;
            case KeyEvent.KEYCODE_X: return X;
            case KeyEvent.KEYCODE_Y: return Y;
            case KeyEvent.KEYCODE_Z: return Z;
        }

        return null;
    }

    private int mValue;
    
    MinimoteKeyType(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

}
