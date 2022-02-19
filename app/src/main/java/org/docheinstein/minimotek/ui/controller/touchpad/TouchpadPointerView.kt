package org.docheinstein.minimotek.ui.controller.touchpad

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.docheinstein.minimotek.util.debug

//class TouchpadPointerView(context: Context) : View(context) {
//    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

class TouchpadPointerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    interface TouchpadListener {
        fun onTouchpadDown(ev: MotionEvent)
        fun onTouchpadUp(ev: MotionEvent)
        fun onTouchpadPointerDown(ev: MotionEvent)
        fun onTouchpadPointerUp(ev: MotionEvent)
        fun onTouchpadMovement(ev: MotionEvent)

    }

    var listener: TouchpadListener? = null

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return super.onTouchEvent(event)
        if (listener == null)
            return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                debug("ACTION_DOWN (X = ${event.x}, Y = ${event.y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadDown(event)
            }
            MotionEvent.ACTION_UP -> {
                debug("ACTION_UP (X = ${event.x}, Y = ${event.y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadUp(event)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                debug("ACTION_POINTER_UP (X = ${event.x}, Y = ${event.y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadPointerUp(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                debug("ACTION_POINTER_DOWN (X = ${event.x}, Y = ${event.y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadPointerDown(event)
            }
            MotionEvent.ACTION_MOVE -> {
                debug("ACTION_MOVE (X = ${event.x}, Y = ${event.y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadMovement(event)
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        debug("Click event")
        return super.performClick()
    }
}