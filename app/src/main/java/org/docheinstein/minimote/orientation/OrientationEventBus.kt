package org.docheinstein.minimote.orientation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.docheinstein.minimote.util.debug
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus of orientation events.
 * Listeners can collect from the StateFlow [OrientationEventBus.orientation] to being
 * notified about events and can query its value for the last known orientation.
 */

@Singleton
class OrientationEventBus @Inject constructor() {

    private val _orientation = MutableStateFlow(Orientation.Portrait)
    val orientation: StateFlow<Orientation>
        get() = _orientation

    fun publish(newOrientation: Orientation) {
        debug("OrientationEventBus notified about orientation $newOrientation " +
                "(was ${orientation.value}), publishing event")
        _orientation.value = newOrientation
    }
}