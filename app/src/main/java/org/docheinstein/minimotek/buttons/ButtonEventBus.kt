package org.docheinstein.minimotek.buttons


import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ButtonEventBus @Inject constructor(

) {
    private val _events = MutableSharedFlow<ButtonType>(
        /*
         * Buffer size.
         * Using extraBufferCapacity allow slow subscribers to receive events
         * when they're ready, instead of lost the events; and furthermore it allows
         * the publishers to use tryEmit(), which is not blocking and caches the events,
         * instead of the blocking emit()
         * (another option would be use replay, but it has the side-effect of notifying
         * new subscribers about past events, which is not necessary here)
         */
        extraBufferCapacity = 10
    )
    val events = _events.asSharedFlow()

    fun publish(button: ButtonType): Boolean {
        debug("ButtonEventBus would emit event for button $button")
        if (_events.subscriptionCount.value == 0) {
            debug("No subscribers, nothing to emit")
            return false /* not handled */
        }
        debug("Going to emit for ${_events.subscriptionCount.value} subscribers")

        val handled = _events.tryEmit(button)

        if (handled)
            debug("Event of button $button has been emitted")
        else
            warn("Failed to deliver event: buffer full?")

        return handled
    }
}