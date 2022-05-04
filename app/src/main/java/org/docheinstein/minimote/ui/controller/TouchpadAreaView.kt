package org.docheinstein.minimote.ui.controller

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.docheinstein.minimote.util.debug
import kotlin.math.roundToInt

/**
 * View that detects touch events and dispatches them to a [TouchpadListener].
 */
class TouchpadAreaView @JvmOverloads constructor(
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || listener == null)
            return super.onTouchEvent(event)

        val x = event.x.roundToInt()
        val y = event.y.roundToInt()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                debug("ACTION_DOWN (X = ${x}, Y = ${y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadDown(event)
            }
            MotionEvent.ACTION_UP -> {
                debug("ACTION_UP (X = ${x}, Y = ${y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadUp(event)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                debug("ACTION_POINTER_UP (X = ${x}, Y = ${y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadPointerUp(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                debug("ACTION_POINTER_DOWN (X = ${x}, Y = ${y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadPointerDown(event)
            }
            MotionEvent.ACTION_MOVE -> {
                debug("ACTION_MOVE (X = ${x}, Y = ${y}, pointers = ${event.pointerCount})")
                listener?.onTouchpadMovement(event)
            }
        }
        return true
    }
}