package org.docheinstein.minimotek.buttons


import org.docheinstein.minimotek.util.debug
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus of buttons events (physical buttons: e.g. VolumeUp/VolumeDown).
 * Listeners should use [addButtonEventListener] to being notified about events
 * and [removeButtonEventListener] to stop being notified.
 * An event can be published to the listeners with [publish].
 */
@Singleton
class ButtonEventBus @Inject constructor() {

    /*
     * This event bus is implemented with the old school observer pattern instead of using
     * the new and fancy SharedFlow because we need to dispatch the event synchronously
     * in ordered to the figure out whether we are consuming the event or not
     * (letting Android handling it in case no listener handles it), and SharedFlow only
     * has emit/tryEmit which delivers the event asynchronously.
     */

    interface ButtonEventListener {
        fun onButtonPressed(button: ButtonType): Boolean // true to consume event
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
        debug("ButtonEventBus notified about event for button $button")

        if (listeners.size == 0)
            return false // not handled

        debug("Going to publish for event ${listeners.size} listeners")

        var handled = false
        for (l in listeners)
            handled = handled || l.onButtonPressed(button)

        debug("Event of button $button handled = $handled")

        return handled
    }
}