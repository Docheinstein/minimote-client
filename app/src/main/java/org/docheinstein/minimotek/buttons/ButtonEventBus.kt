package org.docheinstein.minimotek.buttons


import org.docheinstein.minimotek.util.debug
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ButtonEventBus @Inject constructor() {

    interface ButtonEventListener {
        fun onButtonPressed(button: ButtonType): Boolean
    }

    private val listeners = mutableSetOf<ButtonEventListener>()

    fun addButtonEventListener(listener: ButtonEventListener) {
        debug("Adding listener to ButtonEventBus")
        listeners.add(listener)
    }

    fun removeButtonEventListener(listener: ButtonEventListener) {
        debug("Removing listener from ButtonEventBus")
        listeners.remove(listener)
    }

    fun publish(button: ButtonType): Boolean {
        debug("ButtonEventBus would publish event for button $button")
        if (listeners.size == 0) {
            debug("No listeners, nothing to publish")
            return false /* not handled */
        }
        debug("Going to publish for ${listeners.size} listeners")

        var handled = false
        for (l in listeners)
            handled = handled || l.onButtonPressed(button)

        debug("Event of button $button handled = $handled")

        return handled
    }
}