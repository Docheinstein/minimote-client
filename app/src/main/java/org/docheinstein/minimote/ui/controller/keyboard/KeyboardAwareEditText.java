package org.docheinstein.minimote.ui.controller.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

public class KeyboardAwareEditText extends AppCompatEditText {

    private KeyboardHidingListener mListener;

    public KeyboardAwareEditText(Context context) {
        super(context);
    }

    public KeyboardAwareEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardAwareEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
            event.getAction() == KeyEvent.ACTION_UP) {
            onKeyboardHidden();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setListener(KeyboardHidingListener listener) {
        mListener = listener;
    }

    protected void onKeyboardHidden() {
        if (mListener != null)
            mListener.onKeyboardHidden();
    }
}
