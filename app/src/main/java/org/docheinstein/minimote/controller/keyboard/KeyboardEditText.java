package org.docheinstein.minimote.controller.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class KeyboardEditText extends android.widget.EditText {

    public interface KeyboardHidingListener {
        void onKeyboardHidden();
    }

    private KeyboardHidingListener mListener;

    public KeyboardEditText(Context context) {
        super(context);
    }

    public KeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
            event.getAction() == KeyEvent.ACTION_UP) {
            if (mListener != null)
                mListener.onKeyboardHidden();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setListener(KeyboardHidingListener listener) {
        mListener = listener;
    }
}
