package org.docheinstein.minimotek.ui.controller

import android.view.MotionEvent
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.docheinstein.minimotek.*
import org.docheinstein.minimotek.buttons.ButtonEventBus
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.connection.MinimoteConnection
import org.docheinstein.minimotek.database.hotkey.Hotkey
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkeyRepository
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkey
import org.docheinstein.minimotek.database.hotkey.hw.HwHotkeyRepository
import org.docheinstein.minimotek.database.hotkey.sw.SwHotkey
import org.docheinstein.minimotek.di.IODispatcher
import org.docheinstein.minimotek.di.IOGlobalScope
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.orientation.Orientation
import org.docheinstein.minimotek.orientation.OrientationEventBus
import org.docheinstein.minimotek.packet.MinimotePacket
import org.docheinstein.minimotek.packet.MinimotePacketFactory
import org.docheinstein.minimotek.settings.SettingsManager
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

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
        private const val SERVER_ADDRESS_STATE_KEY = "address"
        private const val SERVER_PORT_STATE_KEY = "port"
    }
    // Connection

    val serverAddress: String = savedStateHandle[SERVER_ADDRESS_STATE_KEY]!!
    val serverPort: Int = savedStateHandle[SERVER_PORT_STATE_KEY]!!

    private val _connectionState = MutableLiveData(ConnectionState.Connecting)
    val connectionState: LiveData<ConnectionState>
        get() = _connectionState
    
    val isConnected
        get() = _connectionState.value == ConnectionState.Connected

    private val connection = MinimoteConnection(serverAddress, serverPort)
    private var lastConnectionCheckTime: Long = 0
    
    private var currentTouchEventId = 0

    // Movement handling
    private var lastMovementSampleTime: Long = 0

    // Click handling
    private var lastClickTouchEventId = 0
    private var lastDownX = 0
    private var lastDownY = 0

    // Scroll handling
    private var lastScrollTime: Long = 0
    private var lastScrollY = 0

    // Widgets
    // TODO call these fields isXxxShown
    private val _touchpadButtons = MutableLiveData(true)
    val touchpadButtons: LiveData<Boolean>
        get() = _touchpadButtons

    private val _keyboard = MutableLiveData(false)
    val keyboard: LiveData<Boolean>
        get() = _keyboard

    private val _hotkeys = MutableLiveData(false)
    val hotkeys: LiveData<Boolean>
        get() = _hotkeys


    private val portraitHotkeys = swHotkeyRepository.portraitHotkeys.asLiveData()
    private val landscapeHotkeys = swHotkeyRepository.landscapeHotkeys.asLiveData()
    val currentOrientationHotkeys: LiveData<List<SwHotkey>>
        get() {
            debug("currentOrientationHotkeys required for ${orientationEventBus.orientation.value}")
            if (orientationEventBus.orientation.value == Orientation.Portrait)
                return portraitHotkeys
            else
                return landscapeHotkeys
        }

    // Hardware hotkeys
    // We have to keep those cached and up to date since we have
    // to say whether we want to handle the event or note synchronously
    // and therefore there's no time to fetch the mapping from the db
    private val buttonsMapping = mutableMapOf<ButtonType, HwHotkey>()

    init {
        debug("ControllerViewModel.init()")


        // Subscribe button events
        buttonEventBus.addButtonEventListener(this)

        // Fetch the physical hotkeys
        viewModelScope.launch {
            hwHotkeyRepository.loadAll().collect { hwHotkeys ->
                debug("Updating cached hw hotkeys")
                buttonsMapping.clear()
                for (h in hwHotkeys) {
                    buttonsMapping[h.button] = h
                }
            }
        }

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
            lastConnectionCheckTime = getTimeMillis()
            _connectionState.postValue(ConnectionState.Connected)

            _keyboard.postValue(settingsManager.getAutomaticallyOpenKeyboard())
            _touchpadButtons.postValue(settingsManager.getAutomaticallyShowTouchpadButtons())
            _hotkeys.postValue(settingsManager.getAutomaticallyShowHotkeys())
        }

//        buttonEventBus.events
//            .onEach { btn ->
//                debug("ControllerViewModel notified about button press")
//                delay(5000)
//            }
//        viewModelScope.launch {
//            debug("Going to collect buttonEventBus events")
//            buttonEventBus.events.collect { btn ->
//                debug("Collected button press")
//                withContext(ioDispatcher) {
//                    handleButtonPressed(btn)
//                }
//                debug("Button handled in ControllerViewModel")
//            }
//            debug("Finished to collect buttonEventBus events")
//        }
    }

    override fun onCleared() {
        debug("ControllerViewModel.onCleared")
        ioScope.launch {
            connection.disconnect()
        }
        // Unsubscribe button events
        buttonEventBus.removeButtonEventListener(this)
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

    fun touchpadDown(ev: MotionEvent) {
        if (!isConnected)
            return
        increaseMovementId()
        lastDownX = ev.x.roundToInt()
        lastDownY = ev.y.roundToInt()
    }

    fun touchpadUp(ev: MotionEvent) {
        if (!isConnected)
            return

        if (lastClickTouchEventId == currentTouchEventId)
        // prevent duplicate click
            return

        // check click area
        if (abs(ev.x.roundToInt() - lastDownX) > CLICK_AREA ||
            abs(ev.y.roundToInt() - lastDownY) > CLICK_AREA)
            return

        lastClickTouchEventId = currentTouchEventId

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(MinimotePacketFactory.newLeftClick())
        }
    }

    fun touchpadPointerDown(ev: MotionEvent) {
        if (!isConnected)
            return

        increaseMovementId()
        lastScrollTime = ev.eventTime
        lastScrollY = ev.y.roundToInt()
    }

    fun touchpadPointerUp(ev: MotionEvent) {
        if (!isConnected)
            return

        if (lastClickTouchEventId == currentTouchEventId)
        // prevent duplicate click
            return

        if (ev.pointerCount <= 1)
            return

        var clickPacket: MinimotePacket? = null
        if (ev.pointerCount == 2)
            clickPacket = MinimotePacketFactory.newRightClick()
        else if (ev.pointerCount == 3)
            clickPacket = MinimotePacketFactory.newMiddleClick()

        if (clickPacket == null)
            return

        lastClickTouchEventId = currentTouchEventId

        viewModelScope.launch(ioDispatcher) {
            checkConnectionAndSendUdp(clickPacket)
        }
    }

    fun touchpadMovement(ev: MotionEvent) {
        if (!isConnected)
            return
        if (ev.pointerCount > 1) {
            if (ev.eventTime - lastScrollTime < SCROLL_MIN_TIME_BETWEEN_SAMPLES)
            // do not exceed the sample rate
                return

            val deltaScroll = lastScrollY - ev.y.roundToInt()

            debug("Scroll delta = $deltaScroll")

            var scrollPacket: MinimotePacket? = null
            if (deltaScroll > SCROLL_DELTA_FOR_TICK)
                scrollPacket = MinimotePacketFactory.newScrollUp()
            else if (deltaScroll < -SCROLL_DELTA_FOR_TICK)
                scrollPacket = MinimotePacketFactory.newScrollDown()

            if (scrollPacket == null)
                return

            lastScrollTime = ev.eventTime
            lastScrollY = ev.y.roundToInt()

            viewModelScope.launch(ioDispatcher) {
                checkConnectionAndSendUdp(scrollPacket)
            }
        } else {
            if (ev.eventTime - lastMovementSampleTime < MOVEMENT_MIN_TIME_BETWEEN_SAMPLES)
            // do not exceed the sample rate
                return

            lastMovementSampleTime = ev.eventTime

            // IMPORTANT
            // compute outside of coroutine, otherwise these
            // may change, for some obscure reason
            val x = ev.x.roundToInt()
            val y = ev.y.roundToInt()

            viewModelScope.launch(ioDispatcher) {
                checkConnectionAndSendUdp(MinimotePacketFactory.newMove(currentTouchEventId, x, y))
            }
        }
    }

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

        // Intercept hotkeys (e.g. VolumeUp with keyboard open)
        val button = ButtonType.byKeyCode(keyCode)
        if (button != null && buttonsMapping.contains(button))
        // key handled, publish the event
        // (so that this ViewModel will handle it since we are also listeners of the bus)
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

        // Intercept hotkeys (e.g. VolumeUp with keyboard open)
        val button = ButtonType.byKeyCode(keyCode)
        if (button != null && buttonsMapping.contains(button))
        // key handled, but do not publish since
        // we have already published it in keyDown
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

    fun openKeyboard() {
        _keyboard.value = true
    }

    fun closeKeyboard() {
        _keyboard.value = false
    }

    fun toggleKeyboard() {
        _keyboard.value = !(_keyboard.value ?: false)
    }

    fun showTouchpadButtons() {
        _touchpadButtons.value = true
    }

    fun hideTouchpadButtons() {
        _touchpadButtons.value = false
    }

    fun toggleTouchpadButtons() {
        _touchpadButtons.value = !(_touchpadButtons.value ?: false)
    }

    fun showHotkeys() {
        _hotkeys.value = true
    }

    fun hideHotkeys() {
        _hotkeys.value = false
    }

    fun toggleHotkeys() {
        _hotkeys.value = !(_hotkeys.value ?: false)
    }


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

    private suspend fun checkConnectionAndSendUdp(packet: MinimotePacket): Boolean {
        return isConnectionStillAlive() && connection.sendUdp(packet)
    }

    private suspend fun checkConnectionAndSendTcp(packet: MinimotePacket): Boolean {
        return isConnectionStillAlive() && connection.sendTcp(packet)
    }

    private suspend fun isConnectionStillAlive(): Boolean {
        if (getTimeMillis() - lastConnectionCheckTime > CONNECTION_KEEP_ALIVE_TIME) {
            debug("Checking whether connection is still alive...")
            // too much time passed from last ping, check connection is still alive
            if (!connection.ensureConnection()) {
                _connectionState.postValue(ConnectionState.Disconnected)
                return false
            }
        }
        lastConnectionCheckTime = getTimeMillis()
        return true
    }

    private fun increaseMovementId() {
        currentTouchEventId = (currentTouchEventId + 1) % MAX_MOVEMENT_ID
        debug("New MID = $currentTouchEventId")
    }
}
