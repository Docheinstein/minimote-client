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
        replay = 10 /* buffer size (needed for make non blocking call to tryEmit) */
    )
    val events = _events.asSharedFlow()

    fun publish(button: ButtonType): Boolean {
        debug("ButtonEventBus is going to emit event for button: $button")
        val handled = _events.tryEmit(button)

        if (handled)
            debug("Event of button $button has been emitted")
        else
            warn("Failed to deliver event: buffer full?")

        return handled
    }
}