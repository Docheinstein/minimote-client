package org.docheinstein.minimote.buttons;

import android.view.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public enum ButtonType {
    VolumeUp(0),
    VolumeDown(1)
    ;

    private static Map<String, ButtonType> STRING_TO_BUTTON_TYPE = new HashMap<>();

    static {
        STRING_TO_BUTTON_TYPE.put("VolumeUp", VolumeUp);
        STRING_TO_BUTTON_TYPE.put("VolumeDown", VolumeDown);
    }

    public static ButtonType fromString(String keyString) {
        return STRING_TO_BUTTON_TYPE.get(keyString);
    }


    public static ButtonType fromAndroidKeyCode(int keycode) {
        switch (keycode) {
            case KeyEvent.KEYCODE_VOLUME_UP: return VolumeUp;
            case KeyEvent.KEYCODE_VOLUME_DOWN: return VolumeDown;
        }

        return null;
    }



    private int mValue;

    ButtonType(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
