package org.docheinstein.minimotek.orientation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class OrientationEventBus @Inject constructor() {

//    interface OrientationEventListener {
//        fun onOrientationChanged(orientation: Orientation)
//    }

    private val _orientation = MutableStateFlow(Orientation.Portrait)
    val orientation: StateFlow<Orientation>
        get() = _orientation
//    var orientation: Orientation? = null

//    private val listeners = mutableSetOf<OrientationEventListener>()
//
//    fun addOrientationEventListener(listener: OrientationEventListener) {
//        debug("Adding listener to OrientationEventBus")
//        listeners.add(listener)
//    }
//
//    fun removeOrientationEventListener(listener: OrientationEventListener) {
//        debug("Removing listener from OrientationEventBus")
//        listeners.remove(listener)
//    }

    fun publish(newOrientation: Orientation) {
        debug("Orientation was ${orientation.value} and now is $newOrientation")
        _orientation.value = newOrientation
//        if (orientation != newOrientation) {
//            orientation = newOrientation
//            debug("OrientationEventBus will publish event for orientation $orientation")
//            for (l in listeners)
//                l.onOrientationChanged(newOrientation)
//        }
    }
}