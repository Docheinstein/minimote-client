package org.docheinstein.minimote.ui.controller.keyboard;

import android.content.Context;
import android.util.AttributeSet;

import org.docheinstein.minimote.utils.ViewUtils;

public class AutoHideEditText extends KeyboardAwareEditText {

    public AutoHideEditText(Context context) {
        super(context);
    }

    public AutoHideEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoHideEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onKeyboardHidden() {
        super.onKeyboardHidden();
        ViewUtils.hide(this);
    }
}
