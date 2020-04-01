package org.docheinstein.minimote.buttons;

import android.view.KeyEvent;

public interface ButtonsListener {
    boolean onButtonPressed(int keycode, KeyEvent event);
}
