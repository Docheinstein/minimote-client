package org.docheinstein.minimote.buttons;

import android.view.KeyEvent;

public interface ButtonsCatcher {
    void addButtonsListener(ButtonsListener l);
    void removeButtonsListener(ButtonsListener l);
}
