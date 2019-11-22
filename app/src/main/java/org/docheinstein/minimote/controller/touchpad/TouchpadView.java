package org.docheinstein.minimote.controller.touchpad;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class TouchpadView extends View {

    public interface TouchpadListener {
        void onTouchpadDown(MotionEvent ev);
        void onTouchpadUp(MotionEvent ev);
        void onTouchpadMovement(MotionEvent ev);
        void onTouchpadPointerUp(MotionEvent ev);
        void onTouchpadPointerDown(MotionEvent ev);
    }

    private static final String TAG = "TouchpadView";

    private TouchpadListener mListener;

    public TouchpadView(Context context) {
        super(context);
    }

    public TouchpadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchpadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchpadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTouchpadListener(TouchpadListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "Touch event: (X: " + event.getX() + ", Y: " + event.getY());

        if (mListener == null) {
            Log.w(TAG, "Null listener, nothing to notify");
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onTouchpadDown(event);
                break;
            case MotionEvent.ACTION_UP:
                mListener.onTouchpadUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mListener.onTouchpadMovement(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mListener.onTouchpadPointerUp(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mListener.onTouchpadPointerDown(event);
                break;
        }

        return true;
    }
}
