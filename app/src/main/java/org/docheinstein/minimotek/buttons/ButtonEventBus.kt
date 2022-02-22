package org.docheinstein.minimotek.buttons


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ButtonEventBus @Inject constructor() {

    interface ButtonEventListener {
        fun onButtonPressed(button: ButtonType): Boolean
    }

    private val _listeners = mutableSetOf<ButtonEventListener>()

    fun addButtonEventListener(listener: ButtonEventListener) {
        debug("Adding listener to ButtonEventBus")
        _listeners.add(listener)
    }

    fun removeButtonEventListener(listener: ButtonEventListener) {
        debug("Removing listener from ButtonEventBus")
        _listeners.remove(listener)
    }

//    private val _events = MutableSharedFlow<ButtonType>(
//        /*
//         * Buffer size.
//         * Using extraBufferCapacity allow slow subscribers to receive events
//         * when they're ready, instead of lost the events; and furthermore it allows
//         * the publishers to use tryEmit(), which is not blocking and caches the events,
//         * instead of the blocking emit()
//         * (another option would be use replay, but it has the side-effect of notifying
//         * new subscribers about past events, which is not necessary here)
//         */
//        extraBufferCapacity = 10
//    )
//    val events = _events.asSharedFlow()

    fun publish(button: ButtonType): Boolean {
        debug("ButtonEventBus would publish event for button $button")
        if (_listeners.size == 0) {
            debug("No listeners, nothing to publish")
            return false /* not handled */
        }
        debug("Going to publish for ${_listeners.size} listeners")

        var handled = false
        for (l in _listeners)
            handled = handled || l.onButtonPressed(button)

        debug("Event of button $button handled = $handled")

        return handled
    }
}