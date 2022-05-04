package org.docheinstein.minimote.ui.controller

import android.view.MotionEvent
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.docheinstein.minimote.*
import org.docheinstein.minimote.buttons.ButtonEventBus
import org.docheinstein.minimote.buttons.ButtonType
import org.docheinstein.minimote.connection.MinimoteConnection
import org.docheinstein.minimote.database.hotkey.Hotkey
import org.docheinstein.minimote.database.hotkey.sw.SwHotkeyRepository
import org.docheinstein.minimote.database.hotkey.hw.HwHotkey
import org.docheinstein.minimote.database.hotkey.hw.HwHotkeyRepository
import org.docheinstein.minimote.database.hotkey.sw.SwHotkey
import org.docheinstein.minimote.di.IODispatcher
import org.docheinstein.minimote.di.IOGlobalScope
import org.docheinstein.minimote.keys.MinimoteKeyType
import org.docheinstein.minimote.orientation.Orientation
import org.docheinstein.minimote.orientation.OrientationEventBus
import org.docheinstein.minimote.packet.MinimotePacket
import org.docheinstein.minimote.packet.MinimotePacketFactory
import org.docheinstein.minimote.settings.SettingsManager
import org.docheinstein.minimote.util.debug
import org.docheinstein.minimote.util.verbose
import org.docheinstein.minimote.util.warn
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt
import java.lang.System.currentTimeMillis

/**
 * Middle layer between the [ControllerFragment] and the server.
 * It's responsible to forward calls from the UI to the
 * [MinimoteConnection] established with the server.
 */
@HiltViewModel
class ControllerViewModel @Inject constructor(
    @IOGlobalScope private val ioScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val hwHotkeyRepository: HwHotkeyRepository,
    private val swHotkeyRepository: SwHotkeyRepository,
    private val buttonEventBus: ButtonEventBus,
    private val orientationEventBus: OrientationEventBus,
    private val settingsManager: SettingsManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ButtonEventBus.ButtonEventListener {


    enum class ConnectionState {
        Connecting,
        Connected,
        Disconnected
    }

    companion object {
        const val SERVER_ADDRESS_STATE_KEY = "address"
        const val SERVER_PORT_STATE_KEY = "port"
    }

    // Connection
    val serverAddress: String = savedStateHandle[SERVER_ADDRESS_STATE_KEY]!!
    val serverPort: Int = savedStateHandle[SERVER_PORT_STATE_KEY]!!

    private val connection = MinimoteConnection(serverAddress, serverPort)

    private val _connectionState = MutableLiveData(ConnectionState.Connecting)
    val connectionState: LiveData<ConnectionState>
        get() = _connectionState
    
    val isConnected: Boolean
        get() = _connectionState.value == ConnectionState.Connected

    private var lastConnectionCheckTime: Long = 0

    // Movement
    private var lastMovementSampleTime: Long = 0

    // Click
    private var lastClickTouchEventId = 0
    private var lastDownTime: Long = 0
    private var lastDownX = 0
    private var lastDownY = 0

    // Scroll
    private var lastScrollTouchEventId = 0
    private var lastScrollTime: Long = 0
    private var lastScrollY = 0

    /* Each time the touchpad is pressed the touch event id is increased,
     * this is needed both for recognize and gather touchpad movements belonging to the
     * same streak of movements and for recognize double/triple click properly
     * without confusing them with other click types */
    private var currentTouchEventId = 0
    private var currentTouchEventFirstPointer = 0

    // Widgets
    private val _isTouchpadButtonsWidgetEnabled = MutableLiveData(false)
    val isTouchpadButtonsWidgetEnabled: LiveData<Boolean>
        get() = _isTouchpadButtonsWidgetEnabled

    private val _isKeyboardWidgetEnabled = MutableLiveData(false)
    val isKeyboardWidgetEnabled: LiveData<Boolean>
        get() = _isKeyboardWidgetEnabled

    private val _isHotkeysWidgetEnabled = MutableLiveData(false)
    val isHotkeysWidgetEnabled: LiveData<Boolean>
        get() = _isHotkeysWidgetEnabled


    // Software hotkeys (for both orientations)
    private val portraitHotkeys = swHotkeyRepository.observeAll(Orientation.Portrait).asLiveData()
    private val landscapeHotkeys = swHotkeyRepository.observeAll(Orientation.Landscape).asLiveData()
    val currentOrientationHotkeys: LiveData<List<SwHotkey>>
        get() {
            debug("currentOrientationHotkeys required for ${orientationEventBus.orientation.value}")
            return if (orientationEventBus.orientation.value == Orientation.Portrait)
                portraitHotkeys
            else
                landscapeHotkeys
        }

    // Hardware hotkeys
    // We have to keep those cached and up to date since we have
    // to say whether we want to handle the event or not (letting Android handling it)
    // synchronously, and therefore there's no time to fetch the mapping from
    // the db when the physical button is pressed
    private val buttonsMapping = mutableMapOf<ButtonType, HwHotkey>()

    init {
        verbose("ControllerViewModel.init() for address=$serverAddress, port=$serverPort")

        // Subscribe button events
        buttonEventBus.addButtonEventListener(this)

        // Fetch the hardware hotkeys
        viewModelScope.launch(ioDispatcher) {
            hwHotkeyRepository.observeAll().collect { hwHotkeys ->
                debug("Hardware hotkeys fetched, updating cached ones")
                buttonsMapping.clear()
                for (h in hwHotkeys)
                    buttonsMapping[h.button] = h
            }
        }

        // Try to establish the connection with the server
        viewModelScope.launch(ioDispatcher) {
            if (!connection.connect()) {
                debug("MinimoteConnection.connect() failed")
                _connectionState.postValue(ConnectionState.Disconnected)
                return@launch
            }

            if (!connection.ensureConnection()) {
                debug("MinimoteConnection.ensureConnection() failed")
                _connectionState.postValue(ConnectionState.Disconnected)
                return@launch
            }

            // Connection is actually on
            lastConnectionCheckTime = currentTimeMillis()
            _connectionState.postValue(ConnectionState.Connected)

            // Eventually enable the widgets to be enabled automatically
            _isKeyboardWidgetEnabled.postValue(settingsManager.getAutomaticallyOpenKeyboard())
            _isTouchpadButtonsWidgetEnabled.postValue(settingsManager.getAutomaticallyShowTouchpadButtons())
            _isHotkeysWidgetEnabled.postValue(settingsManager.getAutomaticallyShowHotkeys())
        }
    }

    override fun onCleared() {
        verbose("ControllerViewModel.onCleared()")

        // Do a clean disconnection from the server
        // The disconnection coroutine must be run into the global ioScope instead of the
        // viewModelScope because the viewModelScope is gonna die right now
        ioScope.launch {
            connection.disconnect()
        }

        // Unsubscribe button events
        buttonEventBus.removeButtonEventListener(this)
    }

    // ---- MOUSE EVENTS ----

    fun touchpadDown(ev: MotionEvent) {
        if (!isConnected)
            return

        // First finger DOWN on the touchpad

        // Keep track of the pointer id (finger)
        currentTouchEventFirstPointer = ev.getPointerId(0)

        // Increase the touch event counter
        increaseTouchEventId()

        // Keep track of the coordinates of this first DOWN, in order the recognize
        // clicks by checking, on UP, whether the coordinates are nearly the same as these ones
        lastDownX = ev.x.roundToInt()
        lastDownY = ev.y.roundToInt()

        // Keep track of the DOWN time
        lastDownTime = ev.eventTime
    }

    fun touchpadUp(ev: MotionEvent) {
        if (!isConnected)
            return

        // No more than a click could be delivered for a touch event
        if (lastClickTouchEventId == currentTouchEventId)
            return

        // Click only if
        // - the event coordinates are near the coordinates of the first DOWN event;
        // this is necessary for distinguish clicks from movements
        // (for which a DOWN and a UP are delivered anyway)
        // - the time between the down and the up is reasonably within a threshold
        if (abs(ev.x.roundToInt() - lastDownX) > CLICK_AREA ||
            abs(ev.y.roundToInt() - lastDownY) > CLICK_AREA ||
            ev.eventTime - lastDownTime > CLICK_TIME)
            return

        // Event handled, remember it in order to ignore further clicks for this touch event
        lastClickTouchEventId = currentTouchEventId

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newLeftClick())
        }
    }

    fun touchpadPointerDown(ev: MotionEvent) {
        if (!isConnected)
            return

        // Second/third finger DOWN on the touchpad

        if (ev.pointerCount == 2) {
            // Beginning of a scroll
            lastScrollTime = ev.eventTime
            lastScrollY = ev.y.roundToInt()
        }
    }

    fun touchpadPointerUp(ev: MotionEvent) {
        if (!isConnected)
            return

        // No more than a click could be delivered for a touch event
        if (lastClickTouchEventId == currentTouchEventId)
            return

        // Click only if
        // - the event coordinates are near the coordinates of the first DOWN event;
        // this is necessary for distinguish clicks from movements
        // (for which a DOWN and a UP are delivered anyway)
        // - the time between the down and the up is reasonably within a threshold
        if (abs(ev.x.roundToInt() - lastDownX) > CLICK_AREA ||
            abs(ev.y.roundToInt() - lastDownY) > CLICK_AREA ||
            ev.eventTime - lastDownTime > CLICK_TIME)
            return

        if (ev.pointerCount <= 1)
            return // maniacal control

        // Check whether the second or the third finger is lifted,
        // in order to distinguish right click from middle click

        var clickPacket: MinimotePacket? = null
        if (ev.pointerCount == 2)
            clickPacket = MinimotePacketFactory.newRightClick()
        else if (ev.pointerCount == 3)
            clickPacket = MinimotePacketFactory.newMiddleClick()

        if (clickPacket == null)
            return

        // Event handled, remember it in order to ignore further clicks for this touch event
        lastClickTouchEventId = currentTouchEventId

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(clickPacket)
        }
    }

    fun touchpadMovement(ev: MotionEvent) {
        if (!isConnected)
            return

        // A touchpad movement might be either a mouse movement or a scroll

        if (ev.pointerCount == 2) {
            // Scroll

            // Do not exceed the sample rate
            if (ev.eventTime - lastScrollTime < SCROLL_MIN_TIME_BETWEEN_SAMPLES)
                return

            // Do not scroll on each movement, instead scroll only after a certain
            // number of points (SCROLL_DELTA_FOR_TICK) from the last sample
            val deltaScroll = lastScrollY - ev.y.roundToInt()

            debug("Scroll delta = $deltaScroll")

            var scrollPacket: MinimotePacket? = null
            if (deltaScroll > SCROLL_DELTA_FOR_TICK)
                scrollPacket = MinimotePacketFactory.newScrollUp()
            else if (deltaScroll < -SCROLL_DELTA_FOR_TICK)
                scrollPacket = MinimotePacketFactory.newScrollDown()

            if (scrollPacket == null)
                return

            lastScrollTouchEventId = currentTouchEventId
            lastScrollTime = ev.eventTime
            lastScrollY = ev.y.roundToInt()

            viewModelScope.launch(ioDispatcher) {
                checkConnectionAndSendUdp(scrollPacket)
            }
        } else if (ev.pointerCount == 1) {
            // Do not consider the movement events dispatched after a scroll already handled for this
            // touch event id (this happens when, after a scroll, the fingers are not lifted
            // perfectly at the same time)
            if (lastScrollTouchEventId == currentTouchEventId)
                return

            // Do not consider movement events for fingers different from
            // the first one that went done
            if (currentTouchEventFirstPointer != ev.getPointerId(0))
                return

            // Do not exceed the sample rate
            if (ev.eventTime - lastMovementSampleTime < MOVEMENT_MIN_TIME_BETWEEN_SAMPLES)
                return

            lastMovementSampleTime = ev.eventTime

            // IMPORTANT,
            // compute outside of the coroutine, otherwise these might change
            // (for some obscure reason)
            val x = ev.x.roundToInt()
            val y = ev.y.roundToInt()

            viewModelScope.launch(ioDispatcher) {
                checkConnectionAndSendUdp(MinimotePacketFactory.newMove(currentTouchEventId, x, y))
            }
        }
    }

    fun leftDown() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newLeftDown())
        }
    }

    fun leftUp() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newLeftUp())
        }
    }

    fun rightDown() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newRightDown())
        }
    }

    fun rightUp() {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newRightUp())
        }
    }

    // ---- KEYBOARD EVENTS ----

    fun write(c: Char) {
        if (!isConnected)
            return

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newWrite(c))
        }
    }

    fun keyClick(keyCode: Int) {
        if (!isConnected)
            return

        val key = MinimoteKeyType.byKeyCode(keyCode)
        if (key == null) {
            warn("No key for keyCode $keyCode")
            return
        }
        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newKeyClick(key))
        }
    }

    fun keyDown(keyCode: Int): Boolean {
        if (!isConnected)
            return false

        // Check whether the button associated with keyCode is mapped to a physical hotkey
        // (this might happen, for example, if VolumeUp is pressed with keyboard open)
        val button = ButtonType.byKeyCode(keyCode)
        if (button != null && buttonsMapping.contains(button))
            // Button with a mapping pressed: publish the event
            // (so that this ViewModel will handle it, since we
            // are also listeners of the event bus)
            return buttonEventBus.publish(button)

        // Standard case: other key pressed
        val key = MinimoteKeyType.byKeyCode(keyCode)
        if (key == null) {
            warn("No key for keyCode $keyCode")
            return false // key not handled
        }

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newKeyDown(key))
        }

        return true // key handled
    }

    fun keyUp(keyCode: Int): Boolean {
        if (!isConnected)
            return false

        // Check whether the button associated with keyCode is mapped to a physical hotkey
        // (this might happen, for example, if VolumeUp is pressed with keyboard open)
        val button = ButtonType.byKeyCode(keyCode)
        if (button != null && buttonsMapping.contains(button))
            // Button with a mapping pressed: do not publish
            // (we have already published it in keyDown)
            return true

        // Standard case: other key pressed
        val key = MinimoteKeyType.byKeyCode(keyCode)
        if (key == null) {
            warn("No key for keyCode $keyCode")
            return false // key not handled
        }

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendTcp(MinimotePacketFactory.newKeyUp(key))
        }

        return true // key handled
    }

    // ---- PHYSICAL BUTTON EVENTS  ----

    override fun onButtonPressed(button: ButtonType): Boolean {
        debug("ControllerViewModel notified about press of button $button")

        val hwHotkey = buttonsMapping[button]
        if (hwHotkey == null) {
            debug("No mapping for button $hwHotkey")
            return false // not handled
        }

        debug("Retrieved hotkey for button: $hwHotkey")

        hotkey(hwHotkey)

        return true // handled
    }

    fun hotkey(hwHotkey: HwHotkey) = hotkey(hwHotkey.toHotkey())
    fun hotkey(swHotkey: SwHotkey) = hotkey(swHotkey.toHotkey())

    fun hotkey(hotkey: Hotkey) {
        viewModelScope.launch(ioDispatcher) {
            // Build a sequence given by the modifiers plus the base key
            val keys = mutableListOf<MinimoteKeyType>()

            // Modifiers
            if (hotkey.shift)
                keys.add(MinimoteKeyType.ShiftLeft)
            if (hotkey.ctrl)
                keys.add(MinimoteKeyType.CtrlLeft)
            if (hotkey.alt)
                keys.add(MinimoteKeyType.AltLeft)
            if (hotkey.altgr)
                keys.add(MinimoteKeyType.AltGr)
            if (hotkey.meta)
                keys.add(MinimoteKeyType.MetaLeft)

            // Base key
            keys.add(hotkey.key)

            checkConnectionAndSendTcp(MinimotePacketFactory.newHotkey(keys))
        }
    }

    // ---- WIDGETS ----

    fun openKeyboard() {
        _isKeyboardWidgetEnabled.value = true
    }

    fun closeKeyboard() {
        _isKeyboardWidgetEnabled.value = false
    }

    fun toggleKeyboard() {
        _isKeyboardWidgetEnabled.value = !(_isKeyboardWidgetEnabled.value ?: false)
    }

    fun showTouchpadButtons() {
        _isTouchpadButtonsWidgetEnabled.value = true
    }

    fun hideTouchpadButtons() {
        _isTouchpadButtonsWidgetEnabled.value = false
    }

    fun toggleTouchpadButtons() {
        _isTouchpadButtonsWidgetEnabled.value = !(_isTouchpadButtonsWidgetEnabled.value ?: false)
    }

    fun showHotkeys() {
        _isHotkeysWidgetEnabled.value = true
    }

    fun hideHotkeys() {
        _isHotkeysWidgetEnabled.value = false
    }

    fun toggleHotkeys() {
        _isHotkeysWidgetEnabled.value = !(_isHotkeysWidgetEnabled.value ?: false)
    }

    private suspend fun checkConnectionAndSendUdp(packet: MinimotePacket): Boolean {
        return isConnectionStillAlive() && connection.sendUdp(packet)
    }

    private suspend fun checkConnectionAndSendTcp(packet: MinimotePacket): Boolean {
        return isConnectionStillAlive() && connection.sendTcp(packet)
    }

    private suspend fun isConnectionStillAlive(): Boolean {
        // Once every CONNECTION_KEEP_ALIVE_TIME (~10 seconds) actually check whether the
        // connection is up (this is necessary because all the mouse events are sent through UDP
        // and do no require a server response, so it might happen, without a keep alive, that
        // we would send packets to a zombie connection)
        if (currentTimeMillis() - lastConnectionCheckTime > CONNECTION_KEEP_ALIVE_TIME) {
            debug("Checking whether connection is still alive...")
            if (!connection.ensureConnection()) {
                _connectionState.postValue(ConnectionState.Disconnected)
                return false
            }
        }

        // Connection is ok
        lastConnectionCheckTime = currentTimeMillis()
        return true
    }

    private fun increaseTouchEventId() {
        // Increase the touch event module 256, since the minimote MOVE packet
        // allows a MOVEMENT_ID of 8 bytes
        currentTouchEventId = (currentTouchEventId + 1) % MAX_MOVEMENT_ID
        debug("New touch event id = $currentTouchEventId")
    }
}
